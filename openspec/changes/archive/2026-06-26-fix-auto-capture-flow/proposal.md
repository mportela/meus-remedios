## Why

A auto-captura por câmera está quebrada: quando habilitada, o usuário aponta a câmera
para o comprimido cadastrado e **nada acontece** — só a captura manual reconhece. A spec
`visual-recognition` exige que, ao detectar um match confiante no frame ao vivo, o sistema
"capture a imagem automaticamente ... e **inicie a análise**", mas a implementação atual
apenas emite flash branco e vibração háptica, sem nunca capturar a foto nem disparar o
reconhecimento. O fluxo prometido nunca chega a um resultado.

## What Changes

- **Encadear captura + análise na detecção:** ao detectar `score ≥ THRESHOLD_CONFIDENT`
  no frame ao vivo, disparar a captura real via `CameraXPreviewController.capturePhoto()`
  e encadear `onCaptured()` → `analyze()`, fazendo a tela transitar para `ANALYZING` e
  exibir o resultado — exatamente como a captura manual. Hoje `capturePhoto()` é código
  morto, nunca invocado.
- **Corrigir orientação do frame ao vivo:** `ImageProxy.toBitmap()` não aplica a rotação
  do sensor, entregando ao embedder um bitmap rotacionado que degrada a similaridade e
  impede a detecção de disparar. Passar a aplicar `imageProxy.imageInfo.rotationDegrees`
  antes do embedding.
- **Alinhar o pipeline de features do preview ao do cadastro:** o frame ao vivo monta um
  `FeatureSet` com `aspectRatio = 1f` fixo e ignora cor/imprint, divergindo do pipeline
  completo usado no cadastro/captura manual e tornando o `THRESHOLD_CONFIDENT` mal
  calibrado para esse caminho. Padronizar a detecção do preview para usar **apenas
  embedding** (sem `aspectRatio` espúrio), com limiar próprio coerente, evitando misturar
  componentes incompatíveis no score.
- **Robustez de ciclo de vida:** garantir cooldown e flag de processamento consistentes
  após a captura automática, e reset correto ao voltar para `IDLE`.
- **Permissão de câmera (descoberto na validação em device):** o preview ao vivo nunca
  aparecia porque o app **nunca declarava nem solicitava a permissão `CAMERA`** — a captura
  manual mascarava isso por usar Intent externo. Declarar `CAMERA` no manifesto e solicitá-la
  em runtime quando a auto-captura está ativa; degradar para o fluxo manual se negada.
- **Preview com altura zero (descoberto na validação):** o `PreviewView` usava
  `Modifier.weight(1f)` dentro de uma `Column` com `verticalScroll`, onde a altura é
  não-limitada e o `weight` colapsa para 0 (`PreviewView size: 954x0` no logcat). Dar ao
  preview altura concreta (aspect ratio).
- **Foto capturada salva girada 90° (descoberto na validação):** o `ImageCapture` gravava o
  JPEG em orientação de sensor (1280×960, deitada) **sem tag EXIF**, enquanto as fotos
  cadastradas pela câmera nativa ficam em pé (960×1280). Como o `FeatureExtractor` decodifica
  os pixels crus (ignora EXIF), a foto de consulta entrava girada 90°, derrubando a
  similaridade de embedding e de forma — o resultado caía em **Ambíguo** (`top1≈0.73 < 0.85`)
  mesmo o gate de embedding do preview tendo disparado a ~0.87. Capturar em memória e **assar
  a rotação do sensor nos pixels** (`imageInfo.rotationDegrees`) antes de gravar o JPEG,
  deixando a consulta em pé, consistente com o cadastro.

## Capabilities

### New Capabilities
<!-- Nenhuma capability nova; é correção de comportamento já especificado. -->

### Modified Capabilities
- `visual-recognition`: refinar o requisito "Auto-captura controlada por configuração"
  para tornar explícito que a detecção confiante DEVE capturar a imagem e iniciar a
  análise (transição para resultado), e que o frame ao vivo deve ser orientado
  corretamente e pontuado por um critério próprio do preview (embedding) — não pelo
  score multimodal calibrado para o pipeline completo.

## Impact

- **Código:**
  - `ui/recognition/RecognitionViewModel.kt` — `onFrameReady()` passa a sinalizar uma
    intenção de auto-captura para a UI; novo encadeamento captura → `onCaptured()` →
    `analyze()`; ajuste do critério de score do preview.
  - `ui/recognition/RecognitionScreen.kt` — coletor de `autoCaptureEvents` passa a
    acionar a captura real (`capturePhoto` no `CameraXPreviewController`) além do háptico;
    solicitação de permissão `CAMERA` em runtime com fallback manual; preview com altura
    concreta (`aspectRatio`).
  - `ui/recognition/CameraXPreviewController.kt` — `capturePhoto()` deixa de ser código
    morto e passa a capturar em memória, assar a rotação do sensor nos pixels e gravar o
    JPEG já em pé; analyzer do preview também aplica rotação ao frame.
  - `AndroidManifest.xml` — declara `android.permission.CAMERA` (exigida pelo preview ao
    vivo; continua sem `INTERNET`, 100% offline).
- **Permissões:** adiciona `CAMERA` (não-rede). Mantém a restrição de offline (sem
  `INTERNET`/rede).
- **Testes:** `RecognitionViewModelTest` — cobre `prepareAutoCapture` → `onCaptured` →
  RESULT, `onAutoCaptureFailed`, e o gate embedding-only com cooldown.
- **Sem impacto** em schema Room ou outras capabilities.
