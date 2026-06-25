## Context

O projeto tem 35+ testes JVM cobrindo use cases, scoring e ViewModels, mas a camada de UI
(telas Compose) não tem cobertura automatizada. Os fakes de repositório já existem em
`domain/usecase/FakeRepositories.kt` e `FakePhotoDependencies.kt`. A injeção de dependências
usa Hilt; para substituir módulos em testes é necessário `hilt-android-testing`. O CI atual
executa apenas `./gradlew test` + `assembleDebug`; não há análise estática de código.

## Goals / Non-Goals

**Goals:**
- Habilitar testes Compose/Robolectric no contexto JVM usando `@HiltAndroidTest` e
  `@TestInstallIn` para substituir os repositórios reais por fakes
- Cobrir 4 fluxos de UI críticos (lista, formulário, hoje, reconhecimento) via
  `createAndroidComposeRule<HiltTestActivity>()`
- Verificar automaticamente a restrição offline via `PackageManager` em Robolectric
- Adicionar ktlint como análise estática e integrá-lo ao CI

**Non-Goals:**
- Testes instrumented no emulador (CI sem emulador; cobertura JVM é suficiente para UI)
- 100% de cobertura de UI (apenas os 4 fluxos de maior risco de regressão)
- Configurar detekt ou outros linters além do ktlint nesta fase
- Modificar qualquer lógica de produção

## Decisions

### D1 — Robolectric para testes de UI (não instrumented)

**Escolha:** `@RunWith(RobolectricTestRunner::class)` + `@Config(application = HiltTestApplication::class)` para todos os testes de tela.

**Rationale:** O CI roda em `ubuntu-latest` sem emulador Android. Robolectric executa no JVM,
é mais rápido, está já configurado (`isIncludeAndroidResources = true`) e suporta Compose
via `createAndroidComposeRule<HiltTestActivity>()`. Os testes de tela são de renderização e
interação básica — Robolectric é adequado para este nível.

**Alternativa descartada:** `connectedCheck` (instrumented) exigiria emulador no CI ou um
serviço pago como Firebase Test Lab, aumentando custo e complexidade.

---

### D2 — `@TestInstallIn` para substituir módulos Hilt em testes

**Escolha:** Criar `FakeRepositoryModule` com `@TestInstallIn(SingletonComponent::class)` que
desinstala `RepositoryModule` e provê as fakes já existentes (reutilizando
`FakeMedicationRepository`, `FakeIntakeLogRepository`, etc.). Idem para `DatabaseModule`
(substituído por Room in-memory) e para `MlModule` (fake `FeatureExtractor`).

**Rationale:** `@TestInstallIn` é o mecanismo oficial Hilt para substituir módulos em testes
sem `@Provides`/`@Binds` duplicados por teste. Reutiliza os fakes existentes, evitando
duplicação de código.

**Alternativa descartada:** Passar ViewModels como parâmetro nos composables — exigiria
refatorar as telas de produção e quebraria o padrão `hiltViewModel()` já estabelecido.

---

### D3 — `OfflineConstraintTest` via `PackageManager` (Robolectric)

**Escolha:** `@RunWith(RobolectricTestRunner::class)`, obter contexto com
`ApplicationProvider.getApplicationContext()`, consultar
`packageManager.getPackageInfo(packageName, GET_PERMISSIONS).requestedPermissions` e
assertar que `android.permission.INTERNET` está ausente.

**Rationale:** Robolectric com `isIncludeAndroidResources = true` lê o manifest real da
aplicação. Este teste falha automaticamente se `INTERNET` for adicionado — detecção na
própria suite de testes JVM, sem passo de CI extra.

**Alternativa descartada:** Ler o arquivo `app/build/intermediates/merged_manifests/...`
diretamente — frágil (depende de path de build e de ter rodado `assembleDebug` antes).

---

### D4 — ktlint via plugin Gradle `org.jlleitschuh.gradle.ktlint`

**Escolha:** Plugin `org.jlleitschuh.gradle.ktlint` (versão 12.x) aplicado no
`app/build.gradle.kts`. Tarefa `ktlintCheck` no CI; `ktlintFormat` disponível localmente.

**Rationale:** Integração nativa com Gradle (sem binário externo), compatível com Android
builds e com a versão do Kotlin usada. A task `ktlintCheck` falha o build se houver
violações de estilo — comportamento idêntico ao `./gradlew test`.

**Alternativa descartada:** `detekt` — mais voltado a bugs/code smell do que a estilo
consistente; adiciona configuração extra fora do escopo desta fase.

---

### D5 — Estrutura de pastas dos novos arquivos de teste

```
app/src/test/java/com/meusremedios/
  di/
    FakeRepositoryModule.kt        # @TestInstallIn — fakes de todos os repos
    FakeMlModule.kt                # @TestInstallIn — fake FeatureExtractor + recognizer
  ui/
    medications/list/MedicationListScreenTest.kt
    medications/form/MedicationFormScreenTest.kt
    today/TodayScreenTest.kt
    recognition/RecognitionScreenTest.kt
  OfflineConstraintTest.kt
```

Todos os testes de UI ficam em `src/test/` (JVM/Robolectric), não em `src/androidTest/`
(que é reservado para instrumented no dispositivo).

## Risks / Trade-offs

- **[Risco] Flakiness em testes Compose/Robolectric** com animações ou composição assíncrona →
  Mitigação: usar `composeRule.waitForIdle()` e `advanceUntilIdle()` nos testes; desabilitar
  animações via `ComposeTestRule`.

- **[Risco] `@TestInstallIn` pode conflitar com testes de use case existentes** se eles
  dependem de Hilt diretamente → Mitigação: os testes de use case existentes usam fakes
  manuais (sem Hilt), então não serão afetados.

- **[Trade-off] Robolectric não simula o dispatcher de câmera/CameraX** → Testes de
  `RecognitionScreen` mockarão o `RecognitionViewModel` via fake recognizer; o fluxo de câmera
  real não é testado nesta fase (pertence a testes manuais/E2E).

- **[Risco] ktlint pode quebrar o CI pela primeira vez** (código existente fora do estilo) →
  Mitigação: rodar `./gradlew ktlintFormat` antes de commitar esta change para auto-corrigir
  o código existente e garantir CI verde.

## Migration Plan

1. Adicionar deps `hilt-android-testing` e plugin ktlint ao `app/build.gradle.kts` + `libs.versions.toml`
2. Rodar `./gradlew ktlintFormat` para formatar código existente
3. Criar `FakeRepositoryModule.kt` e `FakeMlModule.kt` com `@TestInstallIn`
4. Criar os 4 testes de tela + `OfflineConstraintTest`
5. Rodar `./gradlew test` — todos os testes devem passar
6. Atualizar `.github/workflows/ci.yml` com step `ktlintCheck`
7. Commit e verificar CI verde

**Rollback:** remover o plugin ktlint e os novos arquivos de teste não afeta produção.
