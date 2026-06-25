# project-foundation Specification

## Purpose
Definir o scaffolding inicial do projeto Android: módulo único `app`, minSdk 24, Kotlin + Compose, Hilt, Room e estrutura de pacotes MVVM adotada pelo projeto.
## Requirements
### Requirement: Projeto Android compilável
O projeto SHALL ser um aplicativo Android baseado em Gradle (Kotlin DSL) com um
módulo único `app`, `minSdk 24`, Kotlin e Jetpack Compose habilitados, capaz de
gerar um APK de debug com sucesso.

#### Scenario: Build de debug bem-sucedido
- **WHEN** o desenvolvedor executa `./gradlew assembleDebug`
- **THEN** o build conclui sem erros e produz um APK de debug instalável

#### Scenario: Aplicativo inicia com tela placeholder
- **WHEN** o aplicativo é iniciado em um dispositivo/emulador com Android 7.0+
- **THEN** uma tela inicial Compose é exibida com texto em pt-BR

### Requirement: Estrutura de arquitetura MVVM em camadas
O projeto SHALL organizar o código em camadas conforme a arquitetura definida,
com pacotes para `ui`, `domain/model`, `domain/usecase`, `data/local`,
`data/repository`, `data/ml`, `data/media`, `notifications` e `di`.

#### Scenario: Pacotes de camadas presentes
- **WHEN** a estrutura de pacotes do módulo `app` é inspecionada
- **THEN** existem os pacotes `ui`, `domain`, `data`, `notifications` e `di`
  conforme as convenções de arquitetura

### Requirement: Injeção de dependência com Hilt
O projeto SHALL configurar o Hilt para injeção de dependência, com uma classe
`Application` anotada e pronta para prover dependências às camadas.

#### Scenario: Application Hilt configurada
- **WHEN** o aplicativo é construído
- **THEN** a classe `Application` está anotada com `@HiltAndroidApp` e
  registrada no `AndroidManifest`

### Requirement: Operação 100% offline garantida pelo projeto
O projeto SHALL impedir acesso à rede no nível da plataforma: o
`AndroidManifest` NÃO DEVE declarar a permissão `INTERNET` e o app NÃO DEVE
incluir dependências que exijam rede em runtime.

#### Scenario: Manifest sem permissão de internet
- **WHEN** o `AndroidManifest.xml` é inspecionado
- **THEN** não há declaração de `android.permission.INTERNET`

### Requirement: Privacidade dos dados no nível do app
O projeto SHALL configurar o aplicativo para não realizar backup automático nem
permitir extração de dados, mantendo dados em armazenamento privado.

#### Scenario: Backup e extração desabilitados
- **WHEN** a configuração `application` do manifest é inspecionada
- **THEN** `allowBackup` está `false` e regras de extração de dados impedem
  cópia dos dados do app

### Requirement: Infraestrutura de testes configurada
O projeto SHALL incluir as dependências e a configuração necessárias para
executar testes unitários JVM e testes de UI, com os comandos padrão
documentados funcionando.

#### Scenario: Testes unitários executam
- **WHEN** o desenvolvedor executa `./gradlew test`
- **THEN** a tarefa de teste executa com sucesso (mesmo que com a suíte inicial
  mínima)

### Requirement: Integração contínua de build e testes
O projeto SHALL fornecer um workflow de CI que, a cada push/PR, execute o build
e a suíte de testes do projeto.

#### Scenario: Pipeline executa build e testes
- **WHEN** um push ou pull request é aberto no repositório
- **THEN** o workflow de CI executa `./gradlew assembleDebug` e `./gradlew test`

