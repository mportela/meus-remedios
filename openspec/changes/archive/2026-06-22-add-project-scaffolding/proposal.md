## Why

O projeto **Meus Remédios** ainda não possui código: existe apenas documentação
(PRDs, specs técnicas e o plano OpenSpec). Antes de qualquer feature, é preciso
estabelecer a fundação Android — estrutura de módulos, build com Gradle, stack
(Compose/Hilt/Room/CameraX/TFLite), configuração de testes e as restrições
inegociáveis (100% offline, privacidade) — para que as fases seguintes (F1–F9)
tenham um esqueleto compilável, testável e consistente onde construir.

## What Changes

- Inicializar o projeto Gradle Android (módulo único `app`) com Kotlin + Jetpack
  Compose (Material 3), `minSdk 24`, version catalog e configuração de build.
- Configurar a stack base de dependências: Hilt, Room, CameraX, TensorFlow Lite,
  Coroutines/Flow, WorkManager, AlarmManager (declaradas/preparadas no catálogo).
- Criar a estrutura de pacotes da arquitetura MVVM em camadas:
  `ui/<feature>`, `domain/model`, `domain/usecase`, `data/local`,
  `data/repository`, `data/ml`, `data/media`, `notifications/`, `di/`.
- Configurar o `Application` com Hilt e uma `MainActivity` Compose mínima
  (tela placeholder acessível em pt-BR).
- Aplicar as restrições inegociáveis no nível do projeto: **sem permissão
  `INTERNET`** no `AndroidManifest`; backup/exfiltração de dados desabilitados.
- Configurar a base de testes (JUnit, MockK, Turbine, coroutines-test,
  Robolectric, Compose UI test) e os comandos `./gradlew test`,
  `connectedCheck`, `assembleDebug`.
- Adicionar tooling de qualidade básico (`.gitignore`, formatação/lint) e
  scaffolding de CI (workflow que roda build + testes).

## Capabilities

### New Capabilities
- `project-foundation`: requisitos transversais da fundação do projeto — estrutura
  de módulos/pacotes, build compilável, stack base, e enforcement das restrições
  de offline/privacidade no nível do projeto. É uma capability de plataforma que
  habilita todas as features funcionais subsequentes.

### Modified Capabilities
<!-- Nenhuma: este é o primeiro change; não há specs existentes a modificar. -->

## Impact

- **Novo código**: arquivos de build (`settings.gradle.kts`, `build.gradle.kts`
  raiz e do `app`, `gradle/libs.versions.toml`, wrapper), `AndroidManifest.xml`,
  `MeusRemediosApplication`, `MainActivity`, tema Compose base, estrutura de
  pacotes vazia por camada.
- **Dependências**: introdução da stack Android completa (compileSdk, plugins
  Kotlin/Compose/Hilt/KSP).
- **Infra**: workflow de CI (build + testes); sem qualquer dependência de rede em
  runtime.
- **Documentação**: atualização do `CHANGELOG.md` (seção "Não lançado").
