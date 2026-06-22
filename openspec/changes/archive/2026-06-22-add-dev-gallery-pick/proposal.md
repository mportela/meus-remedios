# Proposal: add-dev-gallery-pick

## Why

O fluxo principal de reconhecimento (F4) só aceita imagem da **câmera**. Para depurar o
engine — entender por que um comprimido cai em "não tenho certeza" e validar se uma imagem
de teste dá match com algo cadastrado — é trabalhoso fotografar pela webcam do emulador a
cada tentativa. Falta uma forma rápida e repetível de alimentar o reconhecimento com
**imagens de teste** já existentes na galeria do dispositivo.

Esta change adiciona uma **opção de desenvolvimento** que, quando habilitada, permite
**escolher a foto da galeria** ao reconhecer um remédio, em vez de obrigar a captura pela
câmera. A opção fica atrás de uma flag de build desligável, para não vazar para produção e
poder ser removida quando o reconhecimento estiver estável.

## What Changes

- **Flag de build (`BuildConfig.DEV_TOOLS_ENABLED`):** booleano por build type — `true` em
  `debug`, `false` em `release`. Centraliza recursos de desenvolvimento; começa cobrindo a
  seleção por galeria no reconhecimento.
- **UI (`ui/recognition`):** quando `DEV_TOOLS_ENABLED`, exibe um botão secundário "Usar foto
  da galeria (teste)" na tela inicial e no fluxo de 2ª foto; em release o botão não aparece.
- **ViewModel:** novo `onGalleryPicked(uri)` que materializa a imagem via
  `MedicationImageStore.stage(uri)` e dispara a mesma análise da câmera (reuso total do
  engine e do fluxo de resultado).
- **Documentação:** seção no `docs/README.md` explicando a flag e como enviar imagens de
  teste (`make push-photos`) para depurar o reconhecimento.

## Capabilities

- **visual-recognition** (modificado): a captura da consulta passa a aceitar, sob flag de
  desenvolvimento, uma imagem escolhida da galeria como fonte alternativa à câmera.

## Impact

- Sem novas dependências; `ActivityResultContracts.PickVisualMedia` já é usado no cadastro.
- 100% offline mantido: a imagem é apenas copiada para área privada e analisada on-device.
- `release` continua exclusivamente por câmera (flag `false`); nenhum risco para usuários.
- Sem alteração de schema; reuso de `stage(uri)` já existente.

## Documentation

- Atualizar `docs/README.md` (flag de dev + fluxo de imagens de teste).
- Atualizar `CHANGELOG.md` (seção "Não lançado").
