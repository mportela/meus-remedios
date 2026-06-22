# Changelog

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Não lançado]

### Adicionado
- `Makefile` helper com atalhos de build/teste e execução local no emulador
  (`make run`, `make reopen`, `make emulator`, `make logcat`, `make screenshot`,
  `make ime-fix`, entre outros).
- Seção "Desenvolvimento local" no [`docs/README.md`](docs/README.md) e referências ao
  `Makefile` em [`AGENTS.md`](AGENTS.md) e
  [`.github/copilot-instructions.md`](.github/copilot-instructions.md).
- `README.md` na raiz com visão geral, início rápido e referências à documentação.
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
- **F2 — Catálogo de medicamentos** (change `add-medication-catalog`):
  cadastro, edição, exclusão, listagem e busca de remédios com horários.
  - Use cases de catálogo (`Observe`, `Search`, `Get`, `Save`, `Delete`) com
    validações de regra de negócio (nome obrigatório; término ≥ início).
  - UI Jetpack Compose acessível: lista com busca e estado vazio, formulário de
    cadastro/edição (período, datas, horários com dias da semana, toggle
    "avise-me") e exclusão com confirmação.
  - Navegação via Navigation Compose (`MeusRemediosNavHost`) substituindo a tela
    inicial placeholder.
  - ViewModels MVVM (`StateFlow`) e testes determinísticos de use cases e
    ViewModels com fakes de repositório.
- **F2 — Fotos dos medicamentos** (change `add-medication-photos`):
  captura por câmera e seleção da galeria de fotos de frente/verso, com
  armazenamento privado e extração de features para o reconhecimento futuro.
  - Camada de mídia (`data/media`): `MedicationImageStore`/`FileMedicationImageStore`
    salvando arquivos em área privada do app (temporários em `cacheDir`,
    definitivos em `filesDir/medication_photos/<id>`); `FileProvider` configurado
    para a câmera (`@xml/file_paths`).
  - Extração de features on-device (`data/ml`): cor dominante em espaço Lab
    (`LabColor`, conversão sRGB→XYZ→Lab D65), proporção e downsample da imagem
    (`DefaultFeatureExtractor`/`PhotoFeatures`); embedding reservado para a F4.
  - Use cases de fotos (`AddMedicationPhoto`, `RemoveMedicationPhoto`,
    `ObserveMedicationPhotos`) e módulo Hilt `MediaModule`.
  - Formulário de medicamento integrando fotos: miniaturas, adição via
    câmera/galeria com escolha de lado (frente/verso) e remoção, com semântica de
    acessibilidade.
  - Testes determinísticos de conversão de cor (`LabColorTest`) e dos use cases de
    fotos com fakes de store/extrator/repositório.
