## 1. Repositório e domain

- [x] 1.1 Adicionar `fun observe(): Flow<AppSettings>` a `SettingsRepository` e
         `SettingsRepositoryImpl` (query Room: `SELECT * FROM app_settings WHERE id = 1`)
- [x] 1.2 Criar `domain/usecase/SaveSettingsUseCase.kt`:
         injeta `SettingsRepository`, `RescheduleAllAlarmsUseCase`;
         lê o valor atual, persiste o novo `AppSettings`, e chama `rescheduleAllAlarmsUseCase()`
         se `remindersGlobal` ou `reminderLeadMinutes` mudaram
- [x] 1.3 Criar `domain/usecase/CleanupOldIntakesUseCase.kt`:
         injeta `IntakeLogRepository`, `SettingsRepository`, `Clock`;
         calcula `cutoff = LocalDate.now(clock).minusDays(settings.historyRetentionDays.toLong())`
         e chama `intakeLogRepository.deleteOlderThan(cutoff)`

## 2. RetentionWorker (WorkManager)

- [x] 2.1 Criar `work/RetentionWorker.kt` (`CoroutineWorker`):
         define `@EntryPoint @InstallIn(SingletonComponent)` `RetentionWorkerEntryPoint`
         com `fun cleanupOldIntakesUseCase(): CleanupOldIntakesUseCase`;
         em `doWork()` usa `EntryPointAccessors.fromApplication(...)` para obter o use case
         e chama-o; retorna `Result.success()`
- [x] 2.2 Em `MeusRemediosApplication.onCreate`, agendar `RetentionWorker` como
         `PeriodicWorkRequest` (período: 1 dia, `KEEP` policy):
         `WorkManager.getInstance(this).enqueueUniquePeriodicWork("retention", KEEP, request)`

## 3. UI — SettingsViewModel e SettingsScreen

- [x] 3.1 Criar `ui/settings/SettingsViewModel.kt` (`@HiltViewModel`):
         expõe `uiState: StateFlow<AppSettings>` via `settingsRepository.observe()
         .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())`;
         expõe `fun save(settings: AppSettings)` que chama `saveSettingsUseCase` em
         `viewModelScope.launch`
- [x] 3.2 Criar `ui/settings/SettingsScreen.kt` (`@Composable`):
         - Seção "Histórico": `DropdownMenuBox` com opções 30/60/90/180/365 dias
         - Seção "Câmera": `Switch` para `autoCapture`
         - Seção "Lembretes": `Switch` para `remindersGlobal`; quando ativo, exibir
           `DropdownMenuBox` com opções 0/1/5/10/15/30 min para `reminderLeadMinutes`
         - Seção "Sobre": versão do app (`BuildConfig.VERSION_NAME`), texto de disclaimer,
           confirmação offline
         - Cada item com label grande e descrição curta em pt-BR
         - Ao mudar qualquer valor, chamar `viewModel.save(updatedSettings)` imediatamente

## 4. Navegação — 4º tab

- [x] 4.1 Adicionar `const val SETTINGS = "settings"` a `Routes.kt`
- [x] 4.2 Em `MeusRemediosNavHost.kt`, adicionar `SETTINGS` ao enum `TopLevelDestination`
         com ícone `Icons.Default.Settings` e label `R.string.nav_settings`
- [x] 4.3 Adicionar `composable(Routes.SETTINGS) { SettingsScreen() }` ao grafo de navegação

## 5. Strings

- [x] 5.1 Adicionar ao `res/values/strings.xml`:
         `nav_settings` ("Configurações"),
         `settings_section_history` ("Histórico"),
         `settings_history_retention_label` ("Manter histórico por"),
         `settings_history_retention_days` ("%1$d dias"),
         `settings_section_camera` ("Câmera"),
         `settings_auto_capture_label` ("Auto-captura"),
         `settings_auto_capture_desc` ("A câmera tira foto ao focar o comprimido"),
         `settings_section_reminders` ("Lembretes"),
         `settings_reminders_global_label` ("Ativar lembretes"),
         `settings_reminder_lead_label` ("Avisar com antecedência"),
         `settings_reminder_lead_minutes` ("%1$d min"),
         `settings_section_about` ("Sobre"),
         `settings_about_disclaimer` ("Este app é um auxílio de confirmação visual e não substitui orientação médica."),
         `settings_about_offline` ("Funciona 100% offline. Nenhum dado é enviado para a internet.")

## 6. Testes

- [x] 6.1 Criar `SaveSettingsUseCaseTest`: verifica que `rescheduleAllAlarmsUseCase` é chamado
         quando `remindersGlobal` muda e NÃO é chamado quando só `autoCapture` muda
- [x] 6.2 Criar `CleanupOldIntakesUseCaseTest`: verifica que `deleteOlderThan` é chamado com
         `today - historyRetentionDays` e que logs antigos são removidos (fake repo)
- [x] 6.3 Rodar `./gradlew :app:testDebugUnitTest` e confirmar 0 falhas

## 7. Documentação

- [x] 7.1 Atualizar `CHANGELOG.md` com entrada F7 em "Não lançado"
- [x] 7.2 Marcar F7 como feita em `docs/openspec-plan.md`
- [x] 7.3 Executar `openspec archive --change add-app-settings-and-retention`
