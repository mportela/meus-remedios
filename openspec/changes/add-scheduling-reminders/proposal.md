## Why

O app já possui todos os dados de horário (`ScheduleTime` com `timeOfDay` + `daysOfWeekMask`,
`Medication.remindersEnabled`, `AppSettings.remindersGlobal`) mas nunca dispara notificações —
a tela "Hoje" só funciona se o usuário lembrar de abrir o app. A promessa central do PRD-4
(lembretes nos horários cadastrados) está por cumprir.

## What Changes

- Criação do `NotificationChannel` no startup do app.
- Agendamento de alarmes exatos via `AlarmManager` por dose do dia (respeitando
  `remindersEnabled`, `remindersGlobal` e `reminderLeadMinutes`).
- `BroadcastReceiver` (`AlarmReceiver`) que dispara a notificação ao ser acionado.
- `BroadcastReceiver` (`BootReceiver`) que reagenda todos os alarmes após reinicialização.
- `ScheduleAlarmsForTodayUseCase`: agenda alarmes para todas as doses ativas de hoje.
- `RescheduleAllAlarmsUseCase`: cancela e reagenda do zero (boot + mudança de cadastro).
- `SaveMedicationUseCase` chama `RescheduleAllAlarmsUseCase` após salvar ou deletar.
- Permissões: `POST_NOTIFICATIONS` (Android 13+), `SCHEDULE_EXACT_ALARM` com fallback
  para alarme inexato se o usuário negar, `RECEIVE_BOOT_COMPLETED`.
- Solicitação de permissão `POST_NOTIFICATIONS` na primeira abertura do app (Android 13+);
  se negada definitivamente, dialog com botão "Abrir configurações" que leva às configurações
  do sistema. Se `SCHEDULE_EXACT_ALARM` não disponível, botão que leva a
  `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`.
- Notificação com 4 ações:
  - **"Tomei"** — registra a dose como TAKEN via `MarkIntakeTakenUseCase` e descarta a notificação.
  - **"Confirmar comprimido"** — abre a tela de reconhecimento (câmera) para identificar e
    registrar no fluxo normal; descarta a notificação.
  - **"Lembrar em 2 min"** — agenda novo alarme para `now + 2 min` e descarta a notificação.
  - **"Silenciar"** — descarta a notificação sem ação (ação padrão de dismiss).
- `NotificationActionReceiver` (novo `BroadcastReceiver`) processa as ações "Tomei" e
  "Lembrar em 2 min" em background; "Confirmar comprimido" abre `MainActivity` com flag
  de navegação para a tela Confirmar.

## Capabilities

### New Capabilities
- `scheduling-reminders`: agendamento de alarmes exatos, disparo de notificação por dose,
  reagendamento no boot e ao salvar/excluir medicamento ou horário.

### Modified Capabilities
- `medication-catalog`: `SaveMedicationUseCase` passa a triggar `RescheduleAllAlarmsUseCase`
  após qualquer alteração (adicionar, editar, excluir medicamento ou horário).

## Impact

- **Manifesto**: `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`;
  registro de `AlarmReceiver`, `BootReceiver` e `NotificationActionReceiver`.
- **Hilt**: novo módulo `NotificationsModule` provendo `AlarmManager`,
  `NotificationManager`, `NotificationHelper`.
- **`MeusRemediosApplication`**: cria canal de notificação no `onCreate`.
- **`SaveMedicationUseCase`**: injeta e chama `RescheduleAllAlarmsUseCase`.
- **Sem nova dependência de biblioteca** — usa apenas `AlarmManager` e
  `NotificationCompat` (já transitiva via AndroidX).
