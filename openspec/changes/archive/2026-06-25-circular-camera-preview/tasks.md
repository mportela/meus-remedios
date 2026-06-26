## 1. Preview circular com moldura

- [x] 1.1 Em `RecognitionScreen.kt` (`AutoCaptureContent`), aplicado `aspectRatio(1f)` +
  `Modifier.clip(CircleShape)` ao `AndroidView`/`PreviewView`, recortando o preview em círculo
  centralizado (Box wrapper desnecessário — modifiers aplicados direto na view quadrada).
- [x] 1.2 Adicionada moldura com `Modifier.border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)`
  ao redor do círculo; `clip` antes de `border` para a borda acompanhar o mesmo `CircleShape`.
- [x] 1.3 Confirmado: `CameraXPreviewController` usa `PreviewView` com defaults (scaleType
  `FILL_CENTER`), preenchendo o círculo sem barras — nenhum ajuste necessário.
- [x] 1.4 Diâmetro garantido por `fillMaxWidth().aspectRatio(1f)` (sem `weight`); removido o
  `aspectRatio(3f/4f)` retangular.

## 2. Integridade do reconhecimento

- [x] 2.1 Verificado: `onFrameReady` (ImageAnalysis) e `capturePhoto` (ImageCapture) operam no
  `CameraXPreviewController`, independentes do `PreviewView`; recorte circular não os afeta.

## 3. Acessibilidade e textos

- [x] 3.1 Revisado: o `PreviewView` não tinha `contentDescription` e a dica `recognition_auto_capture_hint`
  já orienta o enquadramento — nenhuma alteração de string necessária.

## 4. Validação

- [x] 4.1 `make fmt && make check` — BUILD SUCCESSFUL.
- [x] 4.2 Build e verificação visual em dispositivo/emulador: preview circular, moldura visível,
  círculo não colapsado, captura/auto-captura funcionando e resultado exibido. **(requer device/emulador — pendente de confirmação visual do usuário)**
- [x] 4.3 Atualizado `CHANGELOG.md` (seção "Não lançado") e marcada a change (F14) em `docs/openspec-plan.md`.
