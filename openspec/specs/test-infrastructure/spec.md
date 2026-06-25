# test-infrastructure Specification

## Purpose
Especificar a infraestrutura de testes automatizados: módulos Hilt fake com `@TestInstallIn`, testes Compose/Robolectric para telas críticas, `OfflineConstraintTest` e linting com ktlint integrado ao build e ao CI.
## Requirements
### Requirement: Testes de UI Compose com Hilt fakes
O sistema de build SHALL executar testes Compose/Robolectric (JVM) para os 4 fluxos
críticos de UI usando fakes Hilt injetados via `@TestInstallIn`, sem depender de emulador
Android.

#### Scenario: Lista de remédios renderiza com dados fake
- **WHEN** `MedicationListScreenTest` é executado com `FakeMedicationRepository` injetado
- **THEN** a tela exibe os itens do fake e o teste passa sem emulador

#### Scenario: Formulário de remédio salva com fake repository
- **WHEN** `MedicationFormScreenTest` preenche nome e salva
- **THEN** o fake repository recebe o remédio e o teste passa

#### Scenario: TodayScreen exibe dose e aceita marcar como tomada
- **WHEN** `TodayScreenTest` é executado com `FakeIntakeLogRepository` injetado
- **THEN** a dose aparece na tela e a ação de marcar como tomada é aceita

#### Scenario: RecognitionScreen exibe resultado confiante com fake recognizer
- **WHEN** `RecognitionScreenTest` injeta fake recognizer que retorna resultado confiante
- **THEN** a tela exibe o nome do remédio identificado

---

### Requirement: Verificação automática da restrição offline
O sistema de build SHALL verificar via teste JVM (Robolectric) que o manifest mergeado da
aplicação não contém a permissão `android.permission.INTERNET`.

#### Scenario: Manifest não tem permissão INTERNET
- **WHEN** `OfflineConstraintTest` consulta as permissões do PackageInfo via Robolectric
- **THEN** `android.permission.INTERNET` está ausente na lista de permissões declaradas

#### Scenario: CI falha se INTERNET for adicionada acidentalmente
- **WHEN** alguém adiciona `INTERNET` ao manifest e roda `./gradlew test`
- **THEN** `OfflineConstraintTest` falha com mensagem clara antes do merge

---

### Requirement: Análise estática de estilo com ktlint
O sistema de build SHALL validar o estilo do código Kotlin via `./gradlew ktlintCheck`,
e o CI SHALL falhar se houver violações.

#### Scenario: ktlintCheck passa no CI
- **WHEN** o pipeline de CI executa `./gradlew ktlintCheck`
- **THEN** o step conclui com código 0 se nenhuma violação existir

#### Scenario: ktlintCheck falha com violação de estilo
- **WHEN** um arquivo Kotlin contém violação de estilo (ex: linha longa, import sem ordenar)
- **THEN** `./gradlew ktlintCheck` falha com lista das violações e o CI bloqueia o merge

#### Scenario: ktlintFormat corrige código localmente
- **WHEN** o desenvolvedor roda `./gradlew ktlintFormat`
- **THEN** os arquivos são reformatados automaticamente e `ktlintCheck` passa em seguida

