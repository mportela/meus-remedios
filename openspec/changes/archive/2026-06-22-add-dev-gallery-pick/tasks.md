# Tasks: add-dev-gallery-pick

## 1. Flag de desenvolvimento
- [x] 1.1 Habilitar `buildFeatures.buildConfig = true` em `app/build.gradle.kts`
- [x] 1.2 Definir `BuildConfig.DEV_TOOLS_ENABLED` (true em `debug`, false em `release`)

## 2. ViewModel
- [x] 2.1 Adicionar `onGalleryPicked(uri)` materializando via `imageStore.stage(uri)`
- [x] 2.2 Reaproveitar `analyze()`/`queryPaths` (suporta combinar com a câmera)

## 3. UI (ui/recognition)
- [x] 3.1 Botão secundário "Usar foto da galeria (teste)" condicionado à flag
- [x] 3.2 Disponibilizar a opção na tela inicial e no fluxo de 2ª foto
- [x] 3.3 Launcher `PickVisualMedia` + strings pt-BR

## 4. Testes
- [x] 4.1 Teste de ViewModel: `onGalleryPicked` leva a RESULT com match esperado
- [x] 4.2 `make build` (debug) e verificação de `release` sem o caminho de galeria

## 5. Documentação
- [x] 5.1 Seção no `docs/README.md` (flag + imagens de teste via `make push-photos`)
- [x] 5.2 Atualizar `CHANGELOG.md` (seção "Não lançado")
