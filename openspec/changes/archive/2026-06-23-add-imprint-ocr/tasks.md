# Tasks — add-imprint-ocr

## 1. Dependência e offline guard
- [ ] Adicionar `mlkitTextRecognition` em `gradle/libs.versions.toml` e a lib
      `com.google.mlkit:text-recognition` (_bundled_) no version catalog
- [ ] Declarar `implementation(libs.mlkit.text.recognition)` em `app/build.gradle.kts`
- [ ] Adicionar `<uses-permission android:name="android.permission.INTERNET" tools:node="remove" />`
      em `app/src/main/AndroidManifest.xml`
- [ ] Verificar o manifesto **mergeado** (build/intermediates) e confirmar que `INTERNET`
      **não** entra no APK

## 2. Leitura de imprint (OCR on-device)
- [ ] Criar interface `ImprintReader { suspend fun read(imagePath: String): String? }`
- [ ] Criar `MlKitImprintReader` (`@Singleton`, `@ApplicationContext`): roda ML Kit Latin
      _bundled_, normaliza o texto, devolve `null` em falha/vazio
- [ ] Criar helper puro `ImprintMatch`: `normalize(raw): String?` e
      `similarity(a, b): Float` (Jaccard de tokens + edit distance, `[0, 1]`, determinístico)

## 3. Features e scoring
- [ ] Adicionar `imprintText: String?` em `PhotoFeatures` e `FeatureSet` (equals/hashCode)
- [ ] `TfliteFeatureExtractor` passa a preencher `imprintText` via `ImprintReader`
- [ ] Adicionar `W_IMPRINT` em `RecognitionParams`
- [ ] Adicionar `RecognitionScorer.textSimilarity()` e somá-la em `score()` (peso `W_IMPRINT`)

## 4. Persistência e migração
- [ ] Adicionar coluna `imprint_text` (`String?`) em `MedicationPhotoEntity`
- [ ] Adicionar `imprintText: String?` em `MedicationPhoto` (domain) e nos mapeadores
      `toDomain`/`toEntity`
- [ ] Subir `MeusRemediosDatabase` para `version = 2` e criar `MIGRATION_1_2`
      (`ALTER TABLE medication_photos ADD COLUMN imprint_text TEXT`)
- [ ] Registrar `addMigrations(MIGRATION_1_2)` no `DatabaseModule`
- [ ] Propagar `imprintText` em `AddMedicationPhotoUseCase` e `RecognizeMedicationUseCase`

## 5. Wiring
- [ ] Vincular `ImprintReader` → `MlKitImprintReader` em `MediaModule`

## 6. Testes
- [ ] `ImprintMatch.normalize` (maiúsculas, descarte de não-alfanuméricos, vazio → `null`)
- [ ] `ImprintMatch.similarity` (igual=1, disjunto=0, tolerância a erro de OCR, simetria)
- [ ] `RecognitionScorer` com imprint (presente em ambos soma `w4`; ausente é ignorado)
- [ ] Fallback do `TfliteFeatureExtractor` com `ImprintReader` fake devolvendo `null`
- [ ] `./gradlew testDebugUnitTest assembleDebug` verde

## 7. Validação
- [ ] Confirmar 100% offline: `INTERNET` ausente no manifesto mergeado; modelo _bundled_
- [ ] `openspec validate add-imprint-ocr --strict`

## 8. Docs e arquivamento
- [ ] Atualizar `CHANGELOG.md` (Não lançado)
- [ ] Atualizar `docs/technical/tech-2-modelo-dados.md` (campo `imprintText`, schema v2)
- [ ] Atualizar `docs/technical/tech-3-engine-reconhecimento.md` (fórmula com `w4`/OCR)
- [ ] Atualizar `docs/product/prd-3-reconhecimento.md` (sinal de imprint)
- [ ] Atualizar `docs/openspec-plan.md` (F4.4 concluída)
- [ ] `openspec archive add-imprint-ocr --yes`
- [ ] Commit
