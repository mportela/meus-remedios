# Design — add-imprint-ocr

## Decisões

### Motor de OCR: ML Kit Text Recognition v2 _bundled_
- **Escolha:** `com.google.mlkit:text-recognition` (Latin, _bundled_). Modelo embarcado no
  APK → funciona 100% offline, sem Play Services em runtime, sem download.
- **Rejeitado:** `com.google.android.gms:play-services-mlkit-text-recognition` (baixa o
  modelo do Play Services → viola offline) e Tesseract (precisão inferior em inscrições
  pequenas; reavaliação registrada como decisão futura no plano).
- **Risco offline:** o ML Kit declara `android.permission.INTERNET` no manifesto da lib
  (telemetria opcional). Mitigação: `<uses-permission android:name="android.permission.INTERNET"
  tools:node="remove" />` no manifesto do app + verificação do manifesto **mergeado**
  (`processDebugMainManifest` / `AndroidManifest.xml` do build) como gate.

### Onde o OCR roda: camada de extração "inteligente"
- `DefaultFeatureExtractor` permanece puro (cor/forma, JVM-testável).
- `TfliteFeatureExtractor` (já decora o base com embedding) ganha também a leitura de imprint
  via uma dependência `ImprintReader` injetada — mantendo o OCR isolado atrás de uma
  interface fakeável em testes.

### Abstração e pureza
- `ImprintReader { suspend fun read(imagePath: String): String? }` — interface; impl
  `MlKitImprintReader` usa ML Kit e devolve `null` em falha/vazio.
- `ImprintMatch` (object puro): `normalize(raw): String?` (maiúsculas, `[A-Z0-9]`+espaço,
  colapsa espaços, descarta vazio) e `similarity(a, b): Float` (Jaccard de tokens com reforço
  por _edit distance_ normalizada para tolerar pequenos erros de OCR), em `[0, 1]`,
  determinístico e JVM-testável.

### Scoring
- `FeatureSet` e `PhotoFeatures` ganham `imprintText: String?`.
- `RecognitionScorer.textSimilarity(a, b): Float?` → `null` quando algum lado é ausente/vazio
  (componente não entra no score); caso contrário `ImprintMatch.similarity`.
- `score()` soma `W_IMPRINT * imprintSim` ao numerador e `W_IMPRINT` ao denominador quando
  presente — preservando a normalização `Σ(w_i·sim_i)/Σ(w_i)`.
- **Calibração:** `W_IMPRINT` recebe um valor inicial razoável; o reequilíbrio fino de pesos
  e limiares (com embedding + imprint juntos) é a F4.5. Nada de mudar `THRESHOLD_CONFIDENT`
  aqui.

### Persistência e migração
- Nova coluna `imprint_text TEXT` (nullable) em `medication_photos`.
- `MeusRemediosDatabase` v1→v2 com `MIGRATION_1_2` aditiva (`ALTER TABLE ... ADD COLUMN`),
  registrada no `Room.databaseBuilder(...).addMigrations(MIGRATION_1_2)`. Sem perda de dados.
- Fotos cadastradas **antes** desta change ficam com `imprintText = null` (o reprocessamento
  retroativo é a F4.6 `migrate-existing-photo-features`).

## Riscos e mitigações
- **APK maior:** o modelo Latin _bundled_ acrescenta alguns MB; aceitável agora, orçamento na
  F4.7.
- **OCR ruidoso:** comprimidos sem imprint ou com imprint ilegível → `null`, sem prejudicar o
  score (degrada para embedding+cor+forma).
- **Falsos negativos por OCR parcial:** a similaridade combina Jaccard + edit distance para
  tolerar leitura imperfeita.
