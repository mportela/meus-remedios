## Why

O catálogo (F2/`add-medication-catalog`) já permite cadastrar remédios e horários,
mas ainda **sem fotos**. As fotos do comprimido são o insumo do reconhecimento
visual (F4): sem elas, o remédio é listável/agendável, mas não reconhecível
(RN-1.4). Esta change entrega a captura/seleção de fotos (frente e verso), o
armazenamento **privado** dos arquivos e a **extração e persistência de features**
determinísticas (cor dominante em Lab e proporção), completando o PRD-1
(RF-1.4/1.5/1.8). O embedding TFLite fica como gancho a ser preenchido pelo
engine de reconhecimento (F4).

## What Changes

- Adicionar camada de mídia (`data/media`): armazenamento de imagens em diretório
  privado do app, com cópia de URIs selecionadas, persistência definitiva por
  medicamento e remoção de arquivos.
- Adicionar extração de features (`data/ml`): conversão sRGB→Lab determinística,
  cor dominante em Lab e proporção (aspect ratio) a partir do arquivo de imagem;
  contrato `FeatureExtractor` com `embedding` opcional (preenchido na F4).
- Adicionar use cases de fotos: adicionar (copiar arquivo + extrair features +
  persistir), remover (apagar arquivo + registro) e observar fotos de um
  medicamento.
- Integrar fotos ao formulário de cadastro/edição: adicionar por **câmera** ou
  **galeria**, escolher o lado (frente/verso), exibir miniaturas e remover.
- Configurar `FileProvider` para a captura de câmera (saída em arquivo privado),
  sem novas permissões e sem rede.
- Cobrir com testes a matemática de cor (Lab) e os use cases de foto (com fakes).

## Capabilities

### New Capabilities
- `medication-photos`: captura/seleção, armazenamento privado e extração de
  features das fotos do comprimido, habilitando o reconhecimento visual futuro.

### Modified Capabilities
<!-- Nenhuma. -->

## Impact

- **Novo código**: `data/media` (armazenamento de imagens), `data/ml` (cor Lab +
  extração de features), `domain/usecase` (use cases de foto), integração na UI
  `ui/medications/form`, módulo Hilt de mídia.
- **Manifest**: adiciona `FileProvider` e `res/xml/file_paths.xml`; nenhuma
  permissão nova (câmera via app do sistema; galeria via photo picker).
- **Sem rede**: arquivos em armazenamento privado; extração 100% on-device.
- **Fora de escopo**: o engine de reconhecimento e o preenchimento do embedding
  (F4/`visual-recognition`); pré-processamento avançado/segmentação.
- **Documentação**: atualização do `CHANGELOG.md` (seção "Não lançado").
