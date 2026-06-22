## Context

A entidade `MedicationPhoto` (F1) já prevê `filePath`, `side`, `embedding`,
`dominantColorLab` e `aspectRatio`; o `MedicationPhotoRepository` já persiste
fotos. Falta o fluxo de **capturar/selecionar imagem → armazenar arquivo privado
→ extrair features → persistir**, e sua integração no formulário (F2). Esta change
completa o PRD-1 (RF-1.4/1.5/1.8) seguindo o pipeline de
[TECH-3](../../../docs/technical/tech-3-engine-reconhecimento.md), porém apenas na
parte de **cadastro** (cor Lab + forma); o embedding e a decisão de
reconhecimento são da F4.

## Goals / Non-Goals

**Goals:**
- Armazenamento de imagens em área privada do app (`data/media`), com cópia de
  URIs selecionadas e remoção de arquivos.
- Extração determinística de cor dominante em Lab e aspect ratio (`data/ml`),
  testável fora da UI.
- Use cases de foto (adicionar/remover/observar) orquestrando mídia + extração +
  repositório.
- UI no formulário: adicionar por câmera/galeria, escolher lado, miniaturas e
  remoção.
- Captura de câmera via `FileProvider`, sem novas permissões e sem rede.

**Non-Goals:**
- Embedding TFLite e segmentação/ROI avançada — F4.
- Tela de captura CameraX dedicada (preview ao vivo) — pertence ao reconhecimento.
- Edição de imagem (recorte manual) além do recorte central implícito.

## Decisions

- **Captura por contratos do `ActivityResult`** (`TakePicture` + photo picker
  `PickVisualMedia`) em vez de CameraX nesta fase: mais simples, acessível e sem
  exigir a permissão `CAMERA` do app (o app de câmera do sistema cuida disso),
  preservando o caráter offline. CameraX fica para a captura de reconhecimento
  (F4).
- **`FileProvider`** para fornecer um `Uri` de saída à câmera, gravando em arquivo
  de cache privado; após confirmação, o arquivo é movido para o diretório
  definitivo `files/medication_photos/`.
- **Pipeline de adição como use case** `AddMedicationPhotoUseCase` orquestrando
  três interfaces injetáveis (`MedicationImageStore`, `FeatureExtractor`,
  `MedicationPhotoRepository`), mantendo a lógica testável com fakes; caminhos de
  arquivo são `String`, sem `Uri` no use case.
- **Cor em Lab determinística**: conversão sRGB→XYZ→Lab (D65) pura em `data/ml`
  (`LabColor`), operando sobre pixels `Int`; cor dominante por média robusta
  (descarta extremos). Testável em JVM sem Android.
- **`embedding` nulo no cadastro**: o `FeatureExtractor` preenche cor e forma e
  deixa `embedding = null`; a F4 fará o preenchimento ao introduzir o modelo
  TFLite. Documentado no contrato.
- **Fotos pendentes no formulário**: como a FK exige `medicationId`, o formulário
  acumula fotos staged (em cache) e, **após** salvar o medicamento com sucesso,
  persiste cada foto pendente e remove as marcadas para exclusão. Assim funciona
  tanto para novo quanto para edição.

## Risks / Trade-offs

- **Consistência arquivo↔banco**: se o processo morrer entre gravar arquivo e
  persistir registro, pode sobrar arquivo órfão. Mitigação: gravar o registro por
  último; uma limpeza de órfãos pode ser adicionada na fase de retenção (F7).
- **Memória de Bitmaps**: decodificação com downsample (inSampleSize) para
  miniatura/feature, evitando carregar imagens grandes inteiras.
- **Sem permissão CAMERA**: depende de o dispositivo ter um app de câmera que
  atenda ao intent — aceitável para o público-alvo; a galeria é alternativa.

## Migration Plan

Sem migração de schema (a entidade já existe na v1). Entrega incremental: mídia e
extração podem ser implementadas e testadas isoladamente; a UI as integra ao
formulário existente.

## Open Questions

- Limite de fotos por lado: nesta change não há limite rígido; a UI agrupa por
  lado. Um limite pode ser definido com base em UX futura.
