## Why

O projeto tem 35+ testes JVM cobrindo use cases e scoring, mas faltam: (1) testes de UI
Compose para os fluxos críticos com fakes Hilt, (2) verificação automática da restrição
offline (sem permissão `INTERNET`) e (3) análise estática (ktlint) integrada ao CI. Sem
isso, regressões de interface e violações de segurança/privacidade não são detectadas
automaticamente.

## What Changes

- Adicionar dependências Hilt testing (`hilt-android-testing`) ao `app/build.gradle.kts`
- Criar `HiltTestRunner` e `TestAppModule` para substituir bindings reais por fakes nos testes de UI
- Adicionar testes Compose/Robolectric (JVM) para 4 fluxos críticos:
  - **MedicationListScreen**: exibição da lista, navegação para detalhe
  - **MedicationFormScreen**: preencher campos obrigatórios e salvar
  - **TodayScreen**: exibição de DoseCard e marcar dose como tomada
  - **RecognitionScreen**: render de resultado confiante e de resultado ambíguo (com fake recognizer)
- Adicionar teste JVM `OfflineConstraintTest`: assert que o manifest mergeado **não** contém `INTERNET`
- Adicionar **ktlint** via plugin Gradle (`org.jlleitschuh.gradle.ktlint`) no `app/build.gradle.kts`
- Atualizar `.github/workflows/ci.yml`: adicionar step `./gradlew ktlintCheck` após testes

## Capabilities

### New Capabilities
<!-- nenhuma — esta change é de infraestrutura de testes, não altera comportamento de produto -->

### Modified Capabilities
<!-- nenhuma — nenhum requisito de produto muda -->

## Impact

- `app/build.gradle.kts`: novas deps (`hilt-android-testing`, `hilt-compiler` para test/androidTest, `ui-test-manifest`, `ktlint` plugin)
- `gradle/libs.versions.toml`: novas entradas para `hilt-android-testing`
- Novos arquivos em `app/src/test/` e `app/src/androidTest/`:
  - `HiltTestRunner.kt`, `TestAppModule.kt`, `FakeRecognitionModule.kt`
  - `MedicationListScreenTest.kt`, `MedicationFormScreenTest.kt`
  - `TodayScreenTest.kt`, `RecognitionScreenTest.kt`
  - `OfflineConstraintTest.kt`
- `.github/workflows/ci.yml`: step adicional de ktlintCheck
- Nenhuma mudança de lógica de produção; sem novas permissões; offline preservado
