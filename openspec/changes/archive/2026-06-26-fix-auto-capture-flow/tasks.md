## 1. Captura + análise na detecção (correção principal)

- [x] 1.1 Em `RecognitionViewModel`, adicionar `prepareAutoCapture(onReady: (File) -> Unit)` que cria o alvo via `imageStore.createCameraTarget()`, guarda `pendingTempPath` e entrega `File(tempPath)`
- [x] 1.2 Em `RecognitionScreen`, alterar o coletor de `autoCaptureEvents` para: háptico + `viewModel.prepareAutoCapture { file -> launch { runCatching { cameraController.capturePhoto(file); viewModel.onCaptured() } } }`, aguardando a captura concluir antes de `onCaptured()`
- [x] 1.3 Garantir que `onFrameReady` set cooldown e flash no momento da detecção (antes do await) e que a guarda `phase == IDLE` impeça reentrância durante a captura/análise
- [x] 1.4 Tratar falha de `capturePhoto` (runCatching): limpar flash, permanecer em `IDLE`, manter botão manual disponível (`onAutoCaptureFailed`)

## 2. Orientação do frame ao vivo

- [x] 2.1 Em `CameraXPreviewController`, no analyzer, aplicar `imageProxy.imageInfo.rotationDegrees` ao bitmap (Matrix.postRotate) antes de chamar `onFrameReady`
- [x] 2.2 Fechar o `imageProxy` corretamente após extrair/rotacionar o bitmap (sem vazamento)

## 3. Critério de score do preview

- [x] 3.1 Adicionar `RecognitionParams.PREVIEW_EMBEDDING_THRESHOLD` (documentado), inicial ~0.85
- [x] 3.2 Em `onFrameReady`, montar o `FeatureSet` da query com `aspectRatio = null` (remover o `1f` espúrio), comparando contra cada cadastro usando apenas embedding e o novo limiar
- [x] 3.3 Remover `triggerFocus` morto ou documentar claramente que o foco contínuo do CameraX é suficiente (limpeza)

## 4. Permissão de câmera e visibilidade do preview (descoberto na validação em device)

- [x] 4.1 Declarar `<uses-permission android:name="android.permission.CAMERA" />` no `AndroidManifest.xml`
- [x] 4.2 Em `RecognitionScreen`, solicitar `CAMERA` em runtime quando `autoCapture` ativo; gate `AutoCaptureContent` em `hasCameraPermission`; fallback para `IdleContent` (manual) se negada
- [x] 4.3 Corrigir altura do `PreviewView`: trocar `weight(1f)` (colapsa a 0 em `verticalScroll`) por `aspectRatio(3f/4f)`
- [x] 4.4 Corrigir orientação da foto capturada: `capturePhoto` passa a capturar em memória (`OnImageCapturedCallback`), assar `imageInfo.rotationDegrees` nos pixels e gravar o JPEG já em pé (antes salvava deitado 1280×960 sem EXIF → score caía a ~0.73 / Ambíguo)

## 5. Testes

- [x] 5.1 `RecognitionViewModelTest`: `prepareAutoCapture` entrega arquivo e `onCaptured` transita para `RESULT`
- [x] 5.2 `RecognitionViewModelTest`: cooldown impede segunda auto-captura dentro de 2s; score abaixo do limiar não dispara (testes existentes sob o novo gate embedding-only)
- [x] 5.3 Gate embedding-only com `PREVIEW_EMBEDDING_THRESHOLD` coberto pelos testes de `onFrameReady` (confiante liga flash + evento; oposto não dispara)
- [x] 5.4 `onAutoCaptureFailed` limpa flash e mantém IDLE

## 6. Documentação e fechamento

- [x] 6.1 Atualizar `CHANGELOG.md` (seção "Não lançado") com a correção da auto-captura
- [x] 6.2 Rodar `make fmt && make check` e `./gradlew test` (183 testes, 0 falhas)
- [x] 6.3 Validar preview em emulador: permissão `CAMERA` concedida → `AutoCaptureContent` renderiza; logcat confirma `PreviewView 1280x960` + `STREAMING` (antes `954x0`)
- [x] 6.4 Validar fluxo completo (emulador + câmera do iPhone via Continuity): apontar para comprimido cadastrado → auto-captura → resultado **Confiante**. Confirmado via logs: antes `aspect=1.333 / score≈0.73 / Ambiguous`; após assar rotação, foto em pé → Confiante
- [ ] 6.5 Marcar a change em `docs/openspec-plan.md` (✅ feito) e arquivar com `openspec archive`
