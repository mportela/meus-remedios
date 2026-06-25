## Why

A configuração `autoCapture` já existe nas settings e é persistida em Room, mas não tem
efeito algum na câmera — o botão de captura manual é o único caminho disponível na tela de
reconhecimento. Para usuários idosos, precisar pressionar um botão enquanto mantém o
comprimido posicionado é um ponto de fricção real. A auto-captura reduz esse esforço: o app
captura automaticamente quando detecta o comprimido em foco **e** com confiança suficiente
para iniciar o reconhecimento.

## What Changes

- **Migração de câmera:** `RecognitionScreen` passa a usar CameraX com preview ao vivo
  (`PreviewView`) quando `autoCapture = true`; quando `false`, mantém o fluxo atual
  (câmera nativa via Intent `TakePicture`).
- **`CameraXPreviewController`:** nova classe em `ui/recognition/` que encapsula
  `ProcessCameraProvider`, `Preview`, `ImageCapture` e `ImageAnalysis`; expõe
  `capturePhoto()` e callback de foco estável.
- **Trigger híbrido:** foco estável (`FocusMeteringAction`) aciona extração de uma frame
  via `ImageAnalysis`; TFLite processa o frame em `Dispatchers.Default`; se
  `top1 score ≥ THRESHOLD_CONFIDENT`, captura automática é disparada.
- **Cooldown de 2 s** após qualquer captura automática (evita disparos em cascata).
- **Feedback:** flash branco (overlay `AnimatedVisibility`) + vibração háptica
  (`VibrationEffect.createOneShot(50 ms)`).
- **`RecognitionViewModel`:** lê `AppSettings.autoCapture` via `SettingsRepository`;
  expõe `autoCaptureEnabled` no `RecognitionUiState`.
- **Novas dependências CameraX:** `camera-camera2`, `camera-lifecycle`, `camera-view`
  (todas on-device; sem rede).

## Capabilities

### New Capabilities

_Nenhuma capability nova — a feature expande o fluxo de reconhecimento existente._

### Modified Capabilities

- `visual-recognition`: adiciona requisito de captura automática disparada por confiança
  do modelo e foco estável da câmera, respeitando a configuração `autoCapture` do usuário.
- `app-settings`: a preferência `autoCapture` passa a ter efeito real no comportamento
  da câmera (anteriormente era salva mas ignorada).

## Impact

- **`ui/recognition/RecognitionScreen.kt`:** UI condicional — `PreviewView` (CameraX) vs.
  botão + Intent.
- **`ui/recognition/RecognitionViewModel.kt`:** observa `autoCapture`; processa frames;
  gerencia cooldown.
- **`ui/recognition/CameraXPreviewController.kt`:** nova classe.
- **`gradle/libs.versions.toml` + `app/build.gradle.kts`:** adicionar libs CameraX.
- **Permissão `CAMERA`:** já declarada no manifest (usada pelo Intent atual).
- **Offline:** CameraX é 100% on-device; nenhuma chamada de rede.
- **`RecognitionViewModelTest`:** novos testes para auto-captura e cooldown.
- **`RecognitionScreenTest`:** novo teste de layout condicional.
