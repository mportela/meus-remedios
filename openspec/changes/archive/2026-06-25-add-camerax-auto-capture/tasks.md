## 1. Dependências CameraX

- [x] 1.1 Adicionar versão `camerax = "1.3.4"` em `gradle/libs.versions.toml` e as libs
      `androidx-camera-camera2`, `androidx-camera-lifecycle`, `androidx-camera-view`.
- [x] 1.2 Declarar as três dependências em `app/build.gradle.kts` (`implementation`).

## 2. CameraXPreviewController

- [x] 2.1 Criar `ui/recognition/CameraXPreviewController.kt`: encapsula `ProcessCameraProvider`,
      `Preview`, `ImageCapture` e `ImageAnalysis` (5 fps, `STRATEGY_KEEP_ONLY_LATEST`).
- [x] 2.2 Expor `fun start(lifecycleOwner, previewView, onFrameReady: (Bitmap) -> Unit, onCaptured: (Uri) -> Unit)`.
- [x] 2.3 Expor `fun triggerCapture()` que usa `ImageCapture.takePicture()` e salva via `MedicationImageStore`.
- [x] 2.4 Expor `fun triggerFocus(x, y)` com `FocusMeteringAction`; chamar `onFocusStable()` no callback.

## 3. RecognitionViewModel — auto-captura

- [x] 3.1 Injetar `SettingsRepository` no `RecognitionViewModel`; coletar `autoCapture` via
      `Flow<AppSettings>` e expor `autoCaptureEnabled: Boolean` no `RecognitionUiState`.
- [x] 3.2 Adicionar `fun onFrameReady(bitmap: Bitmap)`: extrai features com `FeatureExtractor`
      em `Dispatchers.Default`; se `top1 score ≥ THRESHOLD_CONFIDENT` e cooldown passou →
      emite evento de captura automática.
- [x] 3.3 Implementar cooldown de 2 s com `lastAutoCaptureMs` (usando `SystemClock.elapsedRealtime()`).
- [x] 3.4 Adicionar `MedicationFormEvent`-equivalente: `AutoCaptureTriggered` event via `Channel`.

## 4. RecognitionScreen — UI condicional

- [x] 4.1 Quando `autoCaptureEnabled = true`: renderizar `AndroidView { PreviewView }` e
      instanciar `CameraXPreviewController`; remover botão de captura manual do layout.
- [x] 4.2 Quando `autoCaptureEnabled = false`: manter fluxo atual (botão + `TakePicture` Intent).
- [x] 4.3 Implementar overlay de flash branco: `Box` com `AnimatedVisibility` +
      `tween(300ms)` acionado pelo evento `AutoCaptureTriggered`.
- [x] 4.4 Implementar vibração háptica: `LocalView.current.performHapticFeedback(HapticFeedbackConstants.CONFIRM)`
      ou `Vibrator.vibrate(VibrationEffect.createOneShot(50, DEFAULT_AMPLITUDE))`.

## 5. Testes

- [ ] 5.1 `RecognitionViewModelTest`: `onFrameReady` com embedding de score alto → evento
      `AutoCaptureTriggered` emitido; com score baixo → nenhum evento.
- [ ] 5.2 `RecognitionViewModelTest`: segunda chamada dentro de 2 s → bloqueada pelo cooldown.
- [ ] 5.3 `RecognitionScreenTest`: com `autoCaptureEnabled = false` → botão de captura visível;
      com `true` → botão ausente.

## 6. Finalização

- [ ] 6.1 Rodar `./gradlew testDebugUnitTest ktlintCheck` e confirmar BUILD SUCCESSFUL.
- [ ] 6.2 Atualizar `CHANGELOG.md` (seção "Não lançado" → "Adicionado").
- [ ] 6.3 Marcar F12 como `✅ feito` em `docs/openspec-plan.md`.
- [ ] 6.4 `openspec archive "add-camerax-auto-capture" --yes` e git commit + push.
