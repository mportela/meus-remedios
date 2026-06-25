## Context

A tela de reconhecimento usa `ActivityResultContracts.TakePicture()` — a câmera nativa do
Android via Intent. Esse modelo não expõe preview ao vivo, callbacks de foco nem acesso a
frames individuais, tornando impossível qualquer forma de captura automática sem interação
do usuário. A setting `autoCapture` existe e é persistida em Room desde F7, mas é
ignorada em runtime.

CameraX resolve todos esses pontos: `ImageAnalysis` entrega frames contínuos,
`FocusMeteringAction` notifica foco estável, e `ImageCapture` tira a foto sem Intent.

## Goals / Non-Goals

**Goals:**
- Implementar auto-captura usando CameraX quando `autoCapture = true`
- Preservar 100% o fluxo atual (câmera nativa) quando `autoCapture = false`
- Trigger híbrido: foco estável → frame → TFLite → captura se score ≥ threshold
- Cooldown de 2 s entre capturas automáticas
- Feedback: flash branco + vibração háptica

**Non-Goals:**
- Remover o botão de captura manual (continua disponível mesmo com `autoCapture = true`)
- Substituir a câmera do formulário de cadastro de fotos (escopo: apenas `RecognitionScreen`)
- Fine-tuning do modelo TFLite (já descartado em F4.2/F4.7)
- Suporte a múltiplas câmeras / câmera frontal

## Decisions

**D1 — CameraX como path alternativo (não substituto total)**
`autoCapture = false` → `TakePicture` Intent (sem mudança); `autoCapture = true` →
CameraX com `PreviewView`. Isso minimiza o risco de regressão: o path existente não é
tocado.

**D2 — `CameraXPreviewController` como classe separada (não dentro do ViewModel)**
CameraX requer `LifecycleOwner` e `Context` — objetos Android que não devem entrar no
ViewModel (impossibilita testes JVM). O controller fica na camada `ui/recognition/` e
recebe callbacks via lambda para reportar eventos (foco estável, captura concluída) ao
ViewModel.

**D3 — Frame rate do `ImageAnalysis` limitado a 5 fps**
Suficiente para detectar foco estável após `FocusMeteringAction`. Reduz consumo de CPU/
bateria e evita sobrecarga do TFLite. `STRATEGY_KEEP_ONLY_LATEST` descarta frames
atrasados.

**D4 — Extração TFLite em `Dispatchers.Default`, não no callback do `ImageAnalysis`**
O callback do `ImageAnalysis` roda numa thread interna do CameraX (Executor). Processar
TFLite lá bloquearia o pipeline de câmera. A imagem é convertida para bitmap no callback
e o restante roda em coroutine em `Dispatchers.Default` via `viewModelScope`.

**D5 — Flash branco via overlay `AnimatedVisibility` em Compose**
Evita API de câmera (`Camera.parameters.flashMode`) — que não funciona em todos os
dispositivos para flash de UI. O overlay branco semitransparente (alpha 0.7→0) com
`tween(300ms)` simula o efeito visual esperado.

**D6 — Cooldown gerenciado no ViewModel com `SystemClock.elapsedRealtime()`**
O ViewModel mantém `lastAutoCaptureMs: Long`. Ao receber um frame com score suficiente,
verifica `elapsedRealtime() - lastAutoCaptureMs > 2000` antes de disparar. Testável com
`TestCoroutineScheduler` via `Clock`.

## Risks / Trade-offs

- **[Risco] Preview ocupa mais memória que Intent** → Mitigação: limitar `ImageAnalysis`
  a 5 fps e resolução de entrada do TFLite a 224×224 (já é o tamanho de input do modelo).
- **[Risco] Dispositivos com câmera sem suporte a `FocusMeteringAction`** → Mitigação:
  timeout de 3 s: se foco não estabilizar, o frame é ignorado e o usuário captura manualmente.
- **[Risco] `PreviewView` em Compose via `AndroidView` pode causar recomposições** →
  Mitigação: `AndroidView` com `factory` estável; `update` vazio.
- **[Trade-off] Dois caminhos de câmera no mesmo ViewModel** → Aceito: a complexidade
  é isolada em `CameraXPreviewController`; o ViewModel só recebe eventos (`onFocusStable`,
  `onFrameReady`, `onCaptured`).
