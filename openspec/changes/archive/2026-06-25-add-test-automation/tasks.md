## 1. Dependências e configuração de build

- [x] 1.1 Adicionar `hiltTesting` e `hiltCompilerTest` em `gradle/libs.versions.toml` (versão alinhada ao `hilt = "2.52"`)
- [x] 1.2 Adicionar `testImplementation(libs.hilt.android.testing)` e `kspTest(libs.hilt.compiler)` em `app/build.gradle.kts`
- [x] 1.3 Adicionar `testImplementation(libs.androidx.ui.test.junit4)` e `testImplementation(libs.androidx.ui.test.manifest)` para Compose/Robolectric (src/test)
- [x] 1.4 Aplicar plugin `org.jlleitschuh.gradle.ktlint` versão 12.x em `app/build.gradle.kts`
- [x] 1.5 Adicionar entrada `ktlint` em `gradle/libs.versions.toml` (plugin id + versão)

## 2. Formatação do código existente

- [x] 2.1 Rodar `./gradlew ktlintFormat` para auto-formatar todo o código existente sem regressões
- [x] 2.2 Verificar `./gradlew ktlintCheck` passa com 0 violações após a formatação

## 3. Módulos Hilt para testes

- [x] 3.1 Criar `app/src/test/java/com/meusremedios/di/FakeRepositoryModule.kt` com `@TestInstallIn(SingletonComponent::class)` desinstalando `RepositoryModule`, provendo `FakeMedicationRepository`, `FakeScheduleRepository`, `FakeIntakeLogRepository`, `FakeMedicationPhotoRepository` e `FakeSettingsRepository`
- [x] 3.2 Criar `app/src/test/java/com/meusremedios/di/FakeMlModule.kt` com `@TestInstallIn` desinstalando `DatabaseModule` e provendo Room in-memory + fake `FeatureExtractor` (retorna embedding nulo)

## 4. Testes de UI Compose/Robolectric

- [x] 4.1 Criar `app/src/test/java/com/meusremedios/ui/medications/list/MedicationListScreenTest.kt`: `@HiltAndroidTest` + `@Config(application = HiltTestApplication::class)`, assertar que lista renderiza com 2 remédios fake
- [x] 4.2 Criar `app/src/test/java/com/meusremedios/ui/medications/form/MedicationFormScreenTest.kt`: preencher campo "Nome" e clicar em salvar, assertar que fake repo recebeu o remédio
- [x] 4.3 Criar `app/src/test/java/com/meusremedios/ui/today/TodayScreenTest.kt`: injetar `FakeIntakeLogRepository` com dose pendente, assertar que `DoseCard` é exibido
- [x] 4.4 Criar `app/src/test/java/com/meusremedios/ui/recognition/RecognitionScreenTest.kt`: fake `RecognitionUseCase` retorna resultado confiante, assertar que tela exibe nome do remédio

## 5. Teste de restrição offline

- [x] 5.1 Criar `app/src/test/java/com/meusremedios/OfflineConstraintTest.kt`: Robolectric lê manifest via `PackageManager`, assertar ausência de `android.permission.INTERNET`

## 6. CI — adicionar step ktlint

- [x] 6.1 Adicionar step `./gradlew ktlintCheck --stacktrace` em `.github/workflows/ci.yml` após o step de testes unitários

## 7. Validação e documentação

- [x] 7.1 Rodar `./gradlew test` — BUILD SUCCESSFUL, todos os testes (incluindo os novos) passando
- [x] 7.2 Atualizar `CHANGELOG.md` com entrada F9
- [x] 7.3 Marcar F9 como `✅ feito` em `docs/openspec-plan.md`
