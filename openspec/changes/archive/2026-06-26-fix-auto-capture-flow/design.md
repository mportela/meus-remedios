## Context

A auto-captura (`autoCapture` em configurações) substitui o botão manual por um preview
ao vivo do CameraX. `CameraXPreviewController` faz bind de `Preview` + `ImageCapture` +
`ImageAnalysis`; cada frame (~5 fps, 224×224 RGBA) vai para `RecognitionViewModel.onFrameReady()`,
que calcula um embedding TFLite e o compara contra as fotos cadastradas.

Estado atual (bug): quando `onFrameReady()` encontra `score ≥ THRESHOLD_CONFIDENT`, ele
apenas (a) emite `autoCaptureEvents` — coletado na tela só para vibração háptica — e (b)
liga `showCaptureFlash`. **Nunca chama captura nem análise.** `CameraXPreviewController.capturePhoto()`
existe mas é código morto. A captura manual funciona porque segue outro caminho
(`cameraLauncher` Intent → `onCaptured()` → `analyze()`).

Restrições: 100% offline; o ViewModel **não** referencia o `CameraXPreviewController`
(comunicação só por callbacks/eventos — decisão de design existente); reconhecimento
conservador (limiar de confiança).

## Goals / Non-Goals

**Goals:**
- Ao detectar match confiante no preview, capturar a foto real e disparar o mesmo
  `analyze()` da captura manual, levando a tela a `ANALYZING` → `RESULT`.
- Corrigir a orientação do frame ao vivo antes do embedding.
- Tornar o critério de score do preview coerente (embedding-only), sem `aspectRatio`
  espúrio que distorce o limiar multimodal.
- Respeitar cooldown e evitar disparos concorrentes.

**Non-Goals:**
- Recalibrar `THRESHOLD_CONFIDENT`/`MARGIN` do pipeline multimodal completo (fora de escopo).
- Detecção de "foco estável" como gate adicional (o texto antigo da spec mencionava foco;
  o código nunca implementou — mantemos detecção por score + cooldown).
- Mudar o caminho de captura manual.

## Decisions

### 1. Quem orquestra a captura: a tela (Composable), via evento
Como o ViewModel não tem referência ao `CameraXPreviewController` (design existente
mantido), o coletor de `autoCaptureEvents` em `RecognitionScreen` passa a:
1. obter um arquivo de saída do ViewModel,
2. chamar `cameraController.capturePhoto(file)` (suspend) e aguardar,
3. chamar `viewModel.onCaptured()` para encadear `analyze()`,
4. emitir o feedback háptico.

**Alternativa considerada:** dar ao ViewModel a referência do controller e capturar lá.
Rejeitada — quebra o isolamento ViewModel↔CameraX já estabelecido e dificulta testes.

### 2. Reaproveitar o alvo de captura existente
`MedicationImageStore.createCameraTarget()` já cria o arquivo temporário e devolve
`tempPath`. O ViewModel ganha um método `prepareAutoCapture(onReady: (File) -> Unit)` que
cria o alvo, guarda `pendingTempPath` (igual a `prepareCapture`) e entrega `File(tempPath)`
à tela. Assim `onCaptured()` continua lendo `pendingTempPath` sem alterações no fluxo de
análise.

**Alternativa considerada:** reusar `prepareCapture(onReady: (Uri) -> Unit)` e converter
Uri→File. Rejeitada — `capturePhoto` espera `File`; expor `File` direto é mais limpo e
evita parsear o content URI do FileProvider.

### 3. Captura full-res via `ImageCapture`, não o frame de análise
A análise (`RecognizeMedicationUseCase`) opera sobre caminhos de arquivo e roda o pipeline
multimodal completo (embedding + cor + forma + imprint). O frame de 224×224 do
`ImageAnalysis` só serve para o gate de detecção. Portanto a captura usa o use case
`ImageCapture` já bindado (`capturePhoto`), produzindo um JPEG full-res consistente com a
captura manual e o cadastro.

### 4. Orientação do frame ao vivo
No analyzer do `CameraXPreviewController`, aplicar `imageProxy.imageInfo.rotationDegrees`
ao bitmap (via `Matrix.postRotate`) antes de `onFrameReady`, ou configurar
`targetRotation` no `ImageAnalysis.Builder`. Preferimos rotacionar o bitmap no analyzer
para garantir que o embedder receba o comprimido na mesma orientação das fotos cadastradas.

### 5. Score do preview: embedding-only com limiar dedicado
Em `onFrameReady`, o `FeatureSet` da query passa a usar `aspectRatio = null` (em vez de
`1f`) e `colorLab = null`, `imprintText = null` — assim `RecognitionScorer.score` usa
**apenas** o embedding (`totalWeight = W_EMBEDDING`) e o resultado é a própria
similaridade de embedding mapeada em [0,1]. Introduzir `RecognitionParams.PREVIEW_EMBEDDING_THRESHOLD`
(documentado, coberto por teste) para o gate do preview, desacoplando-o do
`THRESHOLD_CONFIDENT` multimodal. Valor inicial alinhado ao regime mesmo-pill (~0.85),
ajustável.

**Alternativa considerada:** manter `aspectRatio = 1f`. Rejeitada — injeta um componente
de forma falso (peso 0.1) que ora infla, ora deprime o score, tornando o gate
imprevisível.

## Risks / Trade-offs

- **[Captura dispara durante recomposição]** Ao mudar para `ANALYZING`, o `PreviewView`
  sai da composição. → A captura é iniciada e aguardada **antes** de `onCaptured()`; o
  `cameraController` é `remember`-ado e só faz `shutdown()` no dispose da tela inteira, de
  modo que o `ImageCapture` em voo conclui normalmente.
- **[Falha na captura automática]** `capturePhoto` pode lançar (ex.: `ImageCapture` não
  inicializado). → Encapsular em `runCatching`; em falha, limpar flash/cooldown e
  permanecer em `IDLE` (botão manual segue disponível). Considerar `phase = ERROR` apenas
  se recorrente.
- **[Disparos múltiplos]** Frames a ~5 fps poderiam disparar várias capturas. → `cooldown`
  de 2s já existente + guarda `phase == IDLE` em `onFrameReady` + `Channel.CONFLATED`
  evitam reentrância; setar cooldown no momento da detecção (antes do await).
- **[Limiar do preview mal calibrado]** Pode capturar cedo demais ou de menos. → Constante
  dedicada e ajustável + teste de golden; sem impacto na decisão final, que ainda passa
  pelo `analyze()` conservador (pode resultar em ambíguo/sem match e a tela informa).

## Migration Plan

Mudança puramente comportamental no app; sem migração de dados, schema ou permissões.
Rollback = reverter o commit. Validar em device real com `autoCapture` ligado.

## Open Questions

- Valor exato de `PREVIEW_EMBEDDING_THRESHOLD` — começar em ~0.85 e ajustar com teste em
  device? (assumido sim)
- Em falha de captura automática repetida, ir para `ERROR` ou apenas manter o preview?
  (assumido: manter preview; sem novo estado de erro nesta change)
