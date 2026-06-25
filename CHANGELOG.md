# Changelog

Todas as mudanças notáveis deste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [Não lançado]

### Decisões de arquitetura
- **F4.2 cancelada — segmentação de comprimido descartada**: a calibração empírica da F4.5
  confirmou que a limitação do reconhecimento está na **identidade do embedding** (MobileNetV3
  genérico não discrimina pills visualmente idênticos), não no ruído de fundo. O usuário
  fotografa o comprimido isolado, tornando segmentação desnecessária para o caso de uso real.
  ML Kit Subject Segmentation violaria a restrição offline (exige download). O caminho real
  para melhorar a discriminação é fine-tuning do modelo — registrado como TODO técnico.
  F4.6 (`migrate-existing-photo-features`) atualizado para excluir segmentação do escopo.

### Adicionado
- **F7 — Configurações do app** (change `add-app-settings-and-retention`):
  tela de Configurações como 4º tab da `NavigationBar`. Preferências disponíveis:
  retenção de histórico (30/60/90/180/365 dias, default 90), auto-captura da câmera,
  lembretes globais e antecedência do aviso (0/1/5/10/15/30 min). Seção "Sobre" com
  versão do app, disclaimer e confirmação offline. `SaveSettingsUseCase` persiste
  as preferências e dispara `RescheduleAllAlarmsUseCase` automaticamente quando
  configurações de lembrete mudam. `RetentionWorker` (WorkManager, diário, via
  `EntryPointAccessors`) remove `IntakeLog` mais antigos que o período configurado.
  Novos testes: `SaveSettingsUseCaseTest` e `CleanupOldIntakesUseCaseTest`.
- **F6 — Lembretes de medicamentos** (change `add-scheduling-reminders`):
  alarmes exatos (`setExactAndAllowWhileIdle`) por dose ativa do dia, respeitando
  `remindersGlobal` e `remindersEnabled` por medicamento. Notificação com 3 ações:
  **"Tomei"** (registra dose como TAKEN diretamente e descarta a notificação),
  **"Confirmar comprimido"** (abre a tela Confirmar e descarta a notificação),
  **"Lembrar em 2 min"** (reagenda o alarme por 2 minutos). Reagendamento automático
  após boot (`BootReceiver`) e à meia-noite (alarme diário). Ao salvar ou excluir
  medicamentos, `SaveMedicationUseCase` aciona `RescheduleAllAlarmsUseCase`.
  Fluxo de permissão: `POST_NOTIFICATIONS` solicitado na primeira abertura (Android 13+);
  se negado permanentemente, dialog com link para configurações. Se `SCHEDULE_EXACT_ALARM`
  indisponível, aviso com link para configurações do sistema. Novos componentes:
  `AlarmReceiver`, `BootReceiver`, `NotificationActionReceiver`, `NotificationActionHandler`,
  `NotificationHelper`, `NotificationChannels`, `ScheduleAlarmsForTodayUseCase`,
  `RescheduleAllAlarmsUseCase`. Canal de notificação `REMINDERS` (importância HIGH) criado
  no startup. Deep link via `MainActivity.onNewIntent` para navegar à aba Confirmar.
- **F5 — Registro de tomadas** (change `add-intake-tracking`):
  registro de doses como TAKEN ou SKIPPED direto na tela Hoje, com toggle idempotente
  (mesmo status → desfaz; status diferente → atualiza; ausente → insere). Cada `DoseCard`
  exibe botões "Tomei" e "Pular" para doses PENDING/LATE, ou "Desfazer" para TAKEN/SKIPPED.
  O sumário do dia ganhou coluna "Puladas". A tela de reconhecimento confiante passa a exibir
  botão "Tomei" que registra a tomada diretamente: 1 dose pendente → marca direto;
  N > 1 doses → exibe seletor de horário; 0 doses → registra tomada ad-hoc (sem horário).
  Novos use cases: `MarkIntakeTakenUseCase`, `MarkIntakeSkippedUseCase`,
  `GetPendingDosesTodayForMedicationUseCase`. Nova query DAO:
  `getByMedicationScheduleDate`. Campo `scheduledAt: Instant` adicionado a `ScheduledDose`.
- **F4.6 — Migração automática de features ausentes** (change `migrate-existing-photo-features`):
  fotos cadastradas antes da F4.3/F4.4 tinham `embedding = null` e `imprintText = null`,
  degradando o reconhecimento para apenas cor+forma. Ao iniciar o app, `MigratePhotoFeaturesUseCase`
  detecta fotos sem embedding, reextrai features via `TfliteFeatureExtractor` e persiste
  `embedding + imprintText` atualizados — silenciosamente, em `Dispatchers.IO`, sem bloquear UI.
  Idempotente: fotos já migradas (embedding não nulo) são puladas. Falhas individuais são isoladas:
  arquivo ausente ou erro de modelo logam e continuam para a próxima foto; a foto permanece
  sem embedding e é retentada no próximo startup. Sem nova dependência: reutiliza `TfliteFeatureExtractor`
  (F4.3) e `MlKitImprintReader` (F4.4) já injetados via Hilt. Novos métodos DAO: `getWithoutEmbedding()`
  e `update()`, propagados para `MedicationPhotoRepository`.
- **F4.5 — Calibração final do engine de reconhecimento** (change `recalibrate-recognition`):
  calibração empírica com embeddings reais extraídos de 6 fotos de 3 comprimidos visualmente
  parecidos (pior caso deliberado). `THRESHOLD_CONFIDENT` recalibrado de `0.90` (provisório F4.1,
  sem embedding) para **`0.85`** no regime com embedding TFLite + OCR ativos. Principal achado:
  a proteção contra falso positivo vem de `MARGIN = 0.08` com cadastro de 2 lados — os dois
  registros do comprimido errado competem entre si, mantendo margem < 0.08 e forçando AMBÍGUO;
  o threshold controla apenas o piso mínimo de qualidade. Golden set permanente com
  `RealPillFixtures` (1024-dim embeddings MobileNetV3 reais, hardcoded em JVM) cobrindo todos
  os modos de componente. Todos os comentários "provisório/F4.1/Será recalibrado" removidos de
  `RecognitionParams`. Extração via `RealPillFeatureExtractorTest` (androidTest) — re-executar
  com `make connected` se o modelo TFLite mudar.
- **F4.4 — OCR de imprint on-device** (change `add-imprint-ocr`): leitura do texto gravado
  no comprimido via **ML Kit Text Recognition Latin _bundled_** (modelo embarcado no APK, sem
  rede, sem Play Services em runtime). Novo campo `imprintText` em `MedicationPhoto`,
  `MedicationPhotoEntity` (coluna `imprint_text TEXT`) e `PhotoFeatures`/`FeatureSet`.
  `ImprintReader` / `MlKitImprintReader` (@Singleton) + helper puro `ImprintMatch` (normalização
  e similaridade Jaccard+edição). `RecognitionScorer` ganhou `textSimilarity()` e o novo peso
  `W_IMPRINT = 0.2` entra no score apenas quando **ambas** as fotos têm imprint.
  Migração Room v1→v2 (`MIGRATION_1_2`) adiciona a coluna de forma aditiva e é registrada em
  `DatabaseModule`. Guard offline: `INTERNET` removida via `tools:node="remove"` no manifest
  e confirmada ausente no manifesto mergeado. Fallback gracioso: OCR falho/vazio → `null` →
  componente ignorado; reconhecimento segue com embedding + cor + forma.
- **F4.3 — Embedding TFLite on-device** (change `add-tflite-embedding`): ativa o núcleo de
  inteligência do reconhecimento. Embarca `mobilenet_v3_small.tflite` (MobileNetV3-Small,
  entrada `224×224×3`, saída `1024`, float32, ~4,1 MB) em `app/src/main/assets/` e implementa
  um `FeatureExtractor` real (`TfliteEmbedder` + `TfliteFeatureExtractor`) que roda a
  inferência 100% on-device e preenche `embedding` no cadastro e na consulta, com vetor
  L2-normalizado (`EmbeddingMath`). Carregamento preguiçoso/thread-safe e **fallback gracioso**
  (falha de carga/inferência → embedding ausente, reconhecimento segue com cor + forma).
  Ativa o peso `W_EMBEDDING` (0.6) já reservado. `noCompress` para `.tflite`. Continua 100%
  offline (sem permissão `INTERNET`). Quantização **int8** e orçamento de APK ficam para a F4.7;
  a fase de **segmentação (F4.2)** foi adiada.
- **F4.1 — Endurecimento dos limiares de reconhecimento** (change
  `harden-recognition-thresholds`): correção de segurança provisória contra falso positivo
  enquanto o embedding TFLite não está plugado. `THRESHOLD_CONFIDENT` elevado de `0.82` para
  `0.90`, rejeitando o controle negativo real (`frente-druse`, score 0.822) sem perder o
  match legítimo (0.958). Testes de controle determinísticos permanentes (positivo + negativo)
  usando as features medidas das imagens reais como fixtures, executáveis em CI sem depender
  dos arquivos. Reforça a decisão conservadora na spec `visual-recognition`. Recalibração
  definitiva planejada para F4.5.
- Replanejamento do **reconhecimento inteligente (TF)** em
  [`docs/openspec-plan.md`](docs/openspec-plan.md): fases faltantes F4.1–F4.7
  (`harden-recognition-thresholds`, `add-pill-segmentation`, `add-tflite-embedding`,
  `add-imprint-ocr`, `recalibrate-recognition`, `migrate-existing-photo-features`,
  `enforce-apk-size-budget`), nova capability `pill-imprint-ocr`, fórmula de score com
  inscrição (OCR) e decisões de arquitetura (MobileNetV3 + ML Kit Text Recognition
  bundled + segmentação), tudo on-device/offline. Modelos embarcados **quantizados int8**
  com orçamento de tamanho de APK (gate de CI na F4.7). Documenta o falso positivo do
  controle negativo (`frente-druse`, score 0.822 vs. limiar 0.82) que motiva o
  endurecimento imediato.
- TODO de **fine-tuning futuro** de modelo de comprimidos no
  [`docs/README.md`](docs/README.md): treinar com as fotos reais do cadastro de devices
  controlados (imagem→nome do remédio como rótulo), mantendo o app 100% offline.
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
- **F3 — Consulta e relatórios** (change `add-reporting-and-browsing`):
  telas de consulta para o dia a dia, com navegação por abas (Hoje · Meus remédios).
  - Tela **Hoje**: relatório do dia com doses derivadas dos horários cadastrados,
    agrupadas por período (manhã/tarde/noite), resumo de tomados/pendentes/atrasados
    e timeline navegável por dia da semana. Status sem registro é derivado por
    horário (pendente/atrasado); a marcação real de tomadas chega na F5.
  - Tela de **detalhe do medicamento**: dados, fotos, horários e histórico recente
    limitado pelo período de retenção configurado; ação de editar e fluxo
    lista→detalhe→edição.
  - Domínio: modelos `DayPeriod`, `DoseStatus`, `ScheduledDose`, `DailyReport`,
    `MedicationDetail`; use cases `ObserveDailyReportUseCase` e
    `ObserveMedicationDetailUseCase` (determinísticos via `Clock` injetável).
  - Dados/DI: `ScheduleRepository.observeAll()` para reatividade do relatório e
    `TimeModule` provendo `java.time.Clock`.
  - Navegação por `NavigationBar` (Hoje · Meus remédios) e testes determinísticos de
    derivação de status, retenção e ViewModels.
- **F4 — Reconhecimento visual** (change `add-visual-recognition`):
  confirmação visual on-device de qual remédio cadastrado é o comprimido em mãos,
  comparando a foto da câmera apenas com as fotos do próprio usuário.
  - Engine de scoring determinístico (`data/ml`): `RecognitionScorer` combina
    embedding (cosseno), cor (ΔE no espaço Lab) e forma (razão de aspecto) com pesos
    em `RecognitionParams`; o score é normalizado pelos componentes disponíveis
    (sem embedding nesta fase, usa cor+forma). `RecognitionEngine` aplica decisão
    conservadora por limiar e margem (confiante / ambíguo / sem correspondência).
  - Domínio: `RecognitionCandidate` e `RecognitionOutcome` (`Confident`, `Ambiguous`,
    `NoMatch`, `NoPhotosRegistered`); use case `RecognizeMedicationUseCase` que agrega
    o melhor score por medicamento e suporta combinar até duas fotos (frente/verso).
  - UI **Confirmar** (nova aba inicial): captura por câmera via `TakePicture` +
    FileProvider (sem permissão de câmera/internet), resultado em letras grandes,
    fluxo de 2ª foto em caso de dúvida e lista de candidatos; acessível para idosos.
  - Testes determinísticos de scoring (vetores sintéticos), decisão por
    limiar/margem, agregação por medicamento e do `RecognitionViewModel`.
- **Ferramenta de desenvolvimento — escolher foto da galeria no reconhecimento**
  (change `add-dev-gallery-pick`):
  - Flag de build `BuildConfig.DEV_TOOLS_ENABLED` (true em `debug`, false em
    `release`) para habilitar recursos de depuração sem vazar para produção.
  - Na tela **Confirmar**, em builds de debug, botão "Usar foto da galeria (teste)"
    (tela inicial e fluxo de 2ª foto) que materializa a imagem via `stage(uri)` e
    executa o mesmo engine de reconhecimento da câmera; em release, apenas câmera.
  - `docs/README.md` documenta a flag e o fluxo de depuração com `make push-photos`.

### Corrigido
- **Performance no cadastro — inicialização de ML fora da main thread**: o
  `MlKitImprintReader` inicializava `TextRecognition.getClient()` de forma eager no
  construtor, bloqueando a main thread na primeira injeção (Hilt) ao navegar para "Novo
  remédio". Corrigido com `by lazy` no `recognizer` e `withContext(Dispatchers.IO)` em
  `read()`, garantindo que a inicialização do client ML Kit e a leitura de arquivo nunca
  ocorram na main thread. Adicionado warm-up do `TfliteEmbedder` em
  `MeusRemediosApplication.onCreate()` via `Dispatchers.Default`, eliminando a carga de
  `libtensorflowlite.so` no momento do primeiro cadastro. Efeito observado: redução de
  "Davey!" de até 2644 ms e "Skipped frames" de até 175 durante a abertura do formulário.
