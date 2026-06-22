## 1. Build e configuração Gradle

- [x] 1.1 Criar wrapper Gradle (`gradlew`, `gradlew.bat`, `gradle/wrapper/`)
- [x] 1.2 Criar `settings.gradle.kts` com o módulo `:app` e repositórios
- [x] 1.3 Criar `gradle/libs.versions.toml` com a stack base (Kotlin, AGP,
      Compose/BOM, Hilt, Room, CameraX, TFLite, Coroutines, WorkManager, testes)
- [x] 1.4 Criar `build.gradle.kts` raiz (plugins/aliases comuns)
- [x] 1.5 Criar `app/build.gradle.kts` (`minSdk 24`, Compose, Hilt+KSP, testes)
- [x] 1.6 Criar `gradle.properties` (AndroidX, JVM args) e `.gitignore`

## 2. Manifest, Application e restrições de plataforma

- [x] 2.1 Criar `AndroidManifest.xml` sem permissão `INTERNET`
- [x] 2.2 Definir `allowBackup=false` e regras de data extraction/backup
- [x] 2.3 Criar `MeusRemediosApplication` com `@HiltAndroidApp`
- [x] 2.4 Registrar Application e `MainActivity` no manifest

## 3. UI base (Compose + tema acessível)

- [x] 3.1 Criar tema Material 3 base (cores/tipografia com foco em contraste)
- [x] 3.2 Criar `MainActivity` com `@AndroidEntryPoint` e setContent
- [x] 3.3 Criar tela placeholder em pt-BR com semântica/acessibilidade
- [x] 3.4 Adicionar `strings.xml` (pt-BR) e recursos básicos (ícone, themes)

## 4. Estrutura de pacotes por camada

- [x] 4.1 Criar pacotes `ui`, `domain/model`, `domain/usecase`
- [x] 4.2 Criar pacotes `data/local`, `data/repository`, `data/ml`, `data/media`
- [x] 4.3 Criar pacotes `notifications` e `di`

## 5. Testes

- [x] 5.1 Configurar dependências de teste (JUnit, MockK, Turbine,
      coroutines-test, Robolectric, Compose UI test)
- [x] 5.2 Adicionar teste unitário JVM mínimo (smoke) que passa
- [x] 5.3 Verificar `./gradlew test` e `./gradlew assembleDebug` (JDK 17 + Android
      SDK instalados; testes passam e APK de debug gerado com sucesso)

## 6. CI e documentação

- [x] 6.1 Criar workflow de CI que roda `assembleDebug` e `test`
- [x] 6.2 Atualizar `CHANGELOG.md` (seção "Não lançado") com o scaffolding
