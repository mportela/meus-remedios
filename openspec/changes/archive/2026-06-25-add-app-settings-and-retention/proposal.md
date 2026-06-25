## Why

O app tem `AppSettings` persistido em Room desde a F1, mas nenhuma tela expõe as preferências
ao usuário. Lembretes (F6), auto-captura (F4) e limpeza automática do histórico (retenção)
ficam travados nos defaults, sem controle visível.

## What Changes

- Adicionar tela de Configurações acessível pela barra de navegação inferior (4º item).
- Expor na UI: retenção de histórico (dias), auto-captura, lembretes globais (on/off +
  antecedência em minutos) e tela "Sobre".
- Criar job periódico (WorkManager) de limpeza: apaga `IntakeLog` mais antigos que
  `historyRetentionDays`; nunca apaga cadastros de medicamentos.
- Ao alterar `remindersGlobal` ou `reminderLeadMinutes`, acionar `RescheduleAllAlarmsUseCase`
  para que os alarmes reflitam imediatamente a mudança.
- Ao alterar `autoCapture`, a tela de reconhecimento passa a respeitar o novo valor em tempo
  real (já lê via `SettingsRepository`, mas não há forma de mudar a config; agora há).
- Tela "Sobre": exibir versão do app, disclaimer ("auxílio de confirmação visual, não substitui
  médico") e confirmação de operação 100% offline.

## Capabilities

### New Capabilities

- `app-settings`: tela de configurações, ViewModel, use cases de leitura/escrita de settings,
  job de retenção de histórico, tela "Sobre".

### Modified Capabilities

- `scheduling-reminders`: novo requisito — alterar lembretes globais ou antecedência via
  Configurações dispara reagendamento imediato de todos os alarmes.
- `visual-recognition`: novo requisito — `autoCapture` controlável via Configurações; a tela
  de reconhecimento observa a configuração em tempo real.

## Impact

- **UI**: novo destino `SETTINGS` na `NavigationBar` (4º item); nova rota `settings`; novos
  arquivos `SettingsScreen.kt` e `SettingsViewModel.kt`.
- **Domain**: `SaveSettingsUseCase` (persiste + dispara reschedule quando lembrete muda);
  `CleanupOldIntakesUseCase` (deleta `IntakeLog` antigos).
- **Data**: `IntakeLogDao` precisa de query `deleteOlderThan(cutoffDate)`.
- **WorkManager**: `RetentionWorker` agendado como `PeriodicWorkRequest` (diário).
- **Di**: `WorkManager` providenciado via Hilt `WorkerFactory` (HiltWorkerFactory).
- **Strings**: labels da tela de configurações e "Sobre" em pt-BR.
- **Sem mudanças em Room schema**: `AppSettings` já contém todos os campos necessários.
