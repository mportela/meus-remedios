## Context

`AppSettings` existe desde a F1 com todos os campos necessários (`historyRetentionDays`,
`autoCapture`, `remindersGlobal`, `reminderLeadMinutes`, `fontScale`, `highContrast`).
`SettingsRepository` e `SettingsRepositoryImpl` já persistem em Room. `IntakeLogDao`
já tem `deleteOlderThan(thresholdDate)`. O que falta é a UI de configuração, o
`SaveSettingsUseCase` que dispara efeitos colaterais, e o `RetentionWorker`.

WorkManager (`work-runtime-ktx: 2.9.1`) já está no classpath. `hilt-work` (Hilt + WorkManager
integration) **não** está; optar por `EntryPointAccessors` evita a dependência extra.

## Goals / Non-Goals

**Goals:**
- Tela `SettingsScreen` completa com todas as preferências de RF-6.1 a RF-6.3 e RF-6.5.
- `SaveSettingsUseCase` atômico: persiste e dispara `RescheduleAllAlarmsUseCase` quando
  lembrete muda.
- `RetentionWorker` periódico (diário, `PeriodicWorkRequest`) via `EntryPointAccessors`.
- 4º tab na `NavigationBar`.
- Testes de `SaveSettingsUseCase` e `CleanupOldIntakesUseCase` (JVM, MockK/Fakes).

**Non-Goals:**
- RF-6.4 acessibilidade (`fontScale`/`highContrast`): campos existem no modelo mas a
  implementação de tema dinâmico no Compose fica para F8 (`add-accessibility-baseline`).
- Backup/restore ou exportação de dados.

## Decisions

### D1 — Injeção no RetentionWorker via EntryPointAccessors (sem hilt-work)
`hilt-work` adiciona `@HiltWorker` + `HiltWorkerFactory`, mas exige sobrescrever
`Configuration` na `Application` e um `ksp` adicional. Com `EntryPointAccessors` o worker
acessa o grafo Hilt diretamente pelo `applicationContext`:
```kotlin
@EntryPoint @InstallIn(SingletonComponent::class)
interface RetentionWorkerEntryPoint {
    fun cleanupOldIntakesUseCase(): CleanupOldIntakesUseCase
}
// no worker:
val useCase = EntryPointAccessors.fromApplication(applicationContext, RetentionWorkerEntryPoint::class.java).cleanupOldIntakesUseCase()
```
Alternativa descartada: `hilt-work` — a mesma DI com uma dependência a mais.

### D2 — SaveSettingsUseCase encapsula efeitos colaterais
O ViewModel não chama `RescheduleAllAlarmsUseCase` diretamente. `SaveSettingsUseCase`
recebe o novo `AppSettings`, persiste via `SettingsRepository.save()`, e se
`remindersGlobal` ou `reminderLeadMinutes` mudaram em relação ao valor anterior,
chama `rescheduleAllAlarmsUseCase()`. Isso mantém o ViewModel simples e testável.

### D3 — Retenção com seletor discreto (não slider livre)
Opções fixas: 30 / 60 / 90 / 180 / 365 dias. Evita valores inválidos e é mais
legível para o público idoso. UI: `DropdownMenuBox` ou chips de seleção única.

### D4 — Antecedência de lembrete com opções fixas
Opções: 0 / 1 / 5 / 10 / 15 / 30 minutos. Dropdown. Exibido apenas quando
`remindersGlobal = true` (condicional na tela).

### D5 — RetentionWorker agendado na Application.onCreate (idempotente)
`WorkManager.getInstance(this).enqueueUniquePeriodicWork("retention", KEEP, request)`.
`ExistingPeriodicWorkPolicy.KEEP` garante que uma entrada já existente não seja
substituída a cada startup — sem drift de execução.

### D6 — SettingsViewModel lê settings via Flow
`SettingsRepository.observe(): Flow<AppSettings>` — se não existir, adicionar método
`observe()` (Room já suporta). O ViewModel usa `stateIn(SharingStarted.WhileSubscribed)`.
Ao salvar, chama `SaveSettingsUseCase` e aguarda (suspending).

## Risks / Trade-offs

- **`SettingsRepository` atual só tem `get()` (suspending), sem Flow.** Solução: adicionar
  `fun observe(): Flow<AppSettings>` ao `SettingsRepository` e `SettingsRepositoryImpl`
  via query `@Query("SELECT * FROM app_settings WHERE id = 1") fun observe(): Flow<AppSettingsEntity?>`.
- **`RetentionWorker` sem Hilt teste unitário**: não testamos o Worker diretamente;
  testamos `CleanupOldIntakesUseCase` com `FakeIntakeLogRepository`. O Worker vira
  um adaptador fino (glue code).
- **WorkManager em testes instrumentados**: o `WorkManager` em testes precisa ser
  inicializado via `WorkManagerTestInitHelper`. Para testes JVM, o Worker não é testado
  diretamente.

## Migration Plan

Sem migração de schema Room (todos os campos já existem em `app_settings`).
O `RetentionWorker` no primeiro run poda o histórico com base no default de 90 dias;
dados que o usuário nunca quis perder podem ser removidos — aceitável pois o default
alinhado ao PRD é 90 dias e os dados históricos têm valor decrescente.

## Open Questions

- (Fechado) fontScale/highContrast: adiado para F8. Os campos no modelo ficam mas sem UI.
- (Fechado) hilt-work vs EntryPointAccessors: optou-se por EntryPointAccessors (D1).
