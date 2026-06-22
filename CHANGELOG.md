# Changelog

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Não lançado]

### Adicionado
- Documentação de produto: ONE PAGER e PRDs 1–6 (cadastro, consulta/relatórios,
  reconhecimento, lembretes, registro de tomadas, configurações).
- Documentação técnica: arquitetura, modelo de dados, engine de reconhecimento,
  acessibilidade, estratégia de testes, NFRs/privacidade/offline e glossário.
- Plano OpenSpec SDD com capabilities e mapeamento de fases F0–F9.
- Índice de documentação (`docs/README.md`).
- `CHANGELOG.md`, `AGENTS.md` e `.github/copilot-instructions.md`.
- Inicialização do OpenSpec (`openspec/`).
- **F0 — Scaffolding do projeto Android** (change `add-project-scaffolding`):
  projeto Gradle (Kotlin DSL) com módulo único `app`, `minSdk 24`, version catalog
  (`gradle/libs.versions.toml`) e Gradle wrapper 8.9.
  - Stack base: Jetpack Compose (Material 3), Hilt, Room, CameraX, TensorFlow Lite,
    Coroutines, WorkManager.
  - Estrutura de pacotes MVVM em camadas (`ui`, `domain`, `data`, `notifications`,
    `di`); `Application` Hilt e `MainActivity` Compose com tela placeholder em pt-BR.
  - Restrições de plataforma: sem permissão `INTERNET`; `allowBackup=false` e regras
    de backup/extração de dados desabilitando cópia (privacidade).
  - Tema acessível (alto contraste, tipografia ampliada) e ícone adaptativo.
  - Base de testes (JUnit, MockK, Turbine, coroutines-test, Robolectric, Compose UI
    test) com smoke test JVM e teste de UI da tela inicial.
  - Workflow de CI (`.github/workflows/ci.yml`) executando `test` e `assembleDebug`.
- **F1 — Camada de dados local** (change `add-local-data-layer`):
  persistência offline com Room (versão 1, `exportSchema=false`).
  - Modelos de domínio (`Medication`, `MedicationPhoto`, `ScheduleTime`,
    `IntakeLog`, `AppSettings`) e enums (`PeriodType`, `PhotoSide`, `IntakeStatus`).
  - Entidades Room com índices e chaves estrangeiras (`CASCADE`/`SET NULL`),
    `Converters` (datas ISO, `FloatArray`↔BLOB, enums) e mapeadores entidade↔domínio.
  - DAOs reativos (`Flow`) para medicamentos, fotos, horários, registros de tomada
    e configurações (singleton com valores padrão).
  - Repositórios expondo modelos de domínio e módulos Hilt (`DatabaseModule`,
    `RepositoryModule`).
  - Testes de converters, DAOs (Room in-memory via Robolectric, cobrindo CRUD,
    cascade, consultas e fluxo reativo com Turbine) e repositórios.
