# Design: add-dev-gallery-pick

## Goals
- Depurar o reconhecimento com imagens de teste repetíveis, sem depender da câmera.
- Manter a opção restrita a builds de desenvolvimento, removível sem refatorar o fluxo.

## Non-Goals
- Permitir seleção por galeria em produção (continua exclusivamente por câmera).
- Alterar o engine de scoring ou a decisão (apenas a fonte da imagem muda).

## Decisions

### Flag de build em vez de configuração em runtime
Optamos por `BuildConfig.DEV_TOOLS_ENABLED` (booleano por build type) em vez de uma toggle em
tela de configurações. Motivos: garante que o caminho de galeria não exista no APK de
`release` (segurança/clareza), é trivial de remover e não acopla a feature de debug ao
domínio de Settings (F7). `buildFeatures.buildConfig = true` habilita o campo.

### Reuso de `stage(uri)` e do fluxo de análise
A imagem da galeria é copiada para a área privada via `MedicationImageStore.stage(uri)` (mesmo
mecanismo do cadastro). O caminho resultante entra na mesma lista `queryPaths` usada pela
câmera, então `analyze()` e todo o fluxo de resultado/2ª foto são reaproveitados sem
duplicação. Isso também preserva o suporte a combinar duas imagens (câmera + galeria, ou duas
da galeria).

### `PickVisualMedia` (Android Photo Picker)
Reusa `ActivityResultContracts.PickVisualMedia` (já usado no cadastro), que não exige
permissão de armazenamento e funciona offline.

## Risks
- **Vazar para produção:** mitigado pela flag `false` em `release` e checagem na UI.
- **Esquecer de desligar:** documentado no README; a flag é o único ponto de controle.
