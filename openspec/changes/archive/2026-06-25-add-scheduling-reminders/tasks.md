## 1. Manifesto e permissões

- [x] 1.1 Adicionar ao `AndroidManifest.xml`:
         `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`;
         registrar `AlarmReceiver` (`exported=false`), `BootReceiver`
         (`exported=true`, action `BOOT_COMPLETED`) e `NotificationActionReceiver`
         (`exported=false`)

## 2. Infraestrutura de notificações

- [x] 2.1 Criar `notifications/NotificationChannels.kt` com constante
         `REMINDERS_CHANNEL_ID = "reminders"` e `REMINDERS_CHANNEL_NAME`
- [x] 2.2 Criar `notifications/NotificationHelper.kt` injetável via Hilt:
         - `fun showDoseReminder(medicationId: Long, medicationName: String, time: String)`
           — constrói `NotificationCompat.Builder` no canal REMINDERS e notifica
         - `fun scheduleExact(scheduleTimeId: Long, triggerAtMillis: Long, intent: PendingIntent)`
           — usa `setExactAndAllowWhileIdle` ou `setAndAllowWhileIdle` conforme
           `canScheduleExactAlarms()`
         - `fun cancelAlarm(scheduleTimeId: Long)` — cancela o `PendingIntent` correspondente
- [x] 2.3 Criar `di/NotificationsModule.kt` (`@Module @InstallIn(SingletonComponent::class)`):
         provê `AlarmManager` e `NotificationManagerCompat` via `@Provides`
- [x] 2.4 Criar canal de notificação em `MeusRemediosApplication.onCreate`:
         `NotificationChannels.createAll(this)`; extrair método `createNotificationChannels()`

## 3. BroadcastReceivers

- [x] 3.1 Criar `notifications/AlarmReceiver.kt` (`@AndroidEntryPoint`):
         - Recebe extras: `EXTRA_MEDICATION_ID`, `EXTRA_MEDICATION_NAME`, `EXTRA_TIME_LABEL`,
           `EXTRA_SCHEDULE_TIME_ID` (Long?, nullable), `EXTRA_SCHEDULED_AT` (ISO string),
           `EXTRA_DATE` (ISO string), `EXTRA_NOTIFICATION_ID`
         - Constrói notificação com 4 ações (ver tarefa 3.4) e chama `notificationHelper.notify`
- [x] 3.2 Criar `notifications/BootReceiver.kt` (`@AndroidEntryPoint`):
         - Responde a `BOOT_COMPLETED`
         - Injeta `RescheduleAllAlarmsUseCase` e chama em `CoroutineScope(Dispatchers.IO).launch`
- [x] 3.3 Criar `notifications/NotificationActionReceiver.kt` (`@AndroidEntryPoint`):
         - Injeta `MarkIntakeTakenUseCase`, `NotificationHelper`
         - Action `ACTION_MARK_TAKEN`: lê extras, reconstrói `ScheduledDose` (ou usa overload
           ad-hoc se `scheduleTimeId` for nulo), chama `markIntakeTakenUseCase`, cancela
           a notificação via `notificationHelper.cancel(notificationId)`
         - Action `ACTION_SNOOZE`: agenda alarme exato para `System.currentTimeMillis() + 2*60*1000`
           com os mesmos extras, cancela a notificação atual
         - Action `ACTION_OPEN_RECOGNITION`: cancela a notificação (a navegação é feita pelo
           `PendingIntent` de activity — não precisa de lógica adicional aqui)
- [x] 3.4 Em `AlarmReceiver`, construir `NotificationCompat.Builder` com as 4 ações:
         - **"Tomei"**: `PendingIntent.getBroadcast` → `NotificationActionReceiver` action `ACTION_MARK_TAKEN`
         - **"Confirmar comprimido"**: `PendingIntent.getActivity` → `MainActivity` com
           `Intent.FLAG_ACTIVITY_NEW_TASK` + extra `NAVIGATE_TO_RECOGNITION = true`
         - **"Lembrar em 2 min"**: `PendingIntent.getBroadcast` → `NotificationActionReceiver` action `ACTION_SNOOZE`
         - Título: `notification_dose_title`; texto: nome + horário

## 4. Use cases

- [x] 4.1 Criar `domain/usecase/ScheduleAlarmsForTodayUseCase.kt`:
         - Injeta `MedicationRepository`, `ScheduleRepository`, `SettingsRepository`,
           `NotificationHelper`, `Clock`
         - Para cada (`medication`, `schedule`) ativo hoje:
           - Pula se `!settings.remindersGlobal` ou `!medication.remindersEnabled`
           - Calcula `triggerAt = LocalDateTime.of(today, schedule.timeOfDay)
             .minusMinutes(settings.reminderLeadMinutes).toInstant(ZoneOffset.UTC).toEpochMilli()`
           - Pula se `triggerAt` já passou
           - Constrói `PendingIntent` com action `AlarmReceiver`, `requestCode = scheduleTimeId.toInt()`,
             extras: `medicationId`, `medicationName`, `timeLabel` (formato HH:mm)
           - Chama `notificationHelper.scheduleExact(scheduleTimeId, triggerAt, intent)`
- [x] 4.2 Criar `domain/usecase/RescheduleAllAlarmsUseCase.kt`:
         - Injeta `ScheduleRepository`, `NotificationHelper`, e `ScheduleAlarmsForTodayUseCase`
         - Cancela alarmes de todos os `ScheduleTime` existentes via `notificationHelper.cancelAlarm`
         - Chama `ScheduleAlarmsForTodayUseCase.invoke()`

## 5. Integração com SaveMedicationUseCase

- [x] 5.1 Injetar `RescheduleAllAlarmsUseCase` em `SaveMedicationUseCase`;
         chamar `rescheduleAllAlarms()` ao final de `invoke()` (após `reconcileSchedules`)
         e ao deletar (criar `suspend fun delete(medicationId: Long)` em SaveMedicationUseCase
         que deleta o medicamento e chama reschedule)

## 6. Navegação via notificação

- [x] 6.1 Em `MainActivity.onCreate` e `onNewIntent`: detectar extra `NAVIGATE_TO_RECOGNITION`;
         se presente, navegar para a tab Confirmar (RecognitionScreen) e limpar o extra

## 7. Solicitação de permissão (Android 13+)

- [x] 7.1 Em `MainActivity.onCreate`, solicitar `POST_NOTIFICATIONS` via
         `ActivityResultContracts.RequestPermission()` se `Build.VERSION.SDK_INT >= 33`
         e permissão ainda não concedida
- [x] 7.2 Se `shouldShowRequestPermissionRationale` retorna `false` após negativa:
         exibir `AlertDialog` com botão "Abrir configurações" →
         `Settings.ACTION_APP_NOTIFICATION_SETTINGS`
- [x] 7.3 Se `!alarmManager.canScheduleExactAlarms()` (Android 12+): exibir banner/Snackbar
         persistente com botão "Abrir configurações" → `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`;
         rever em `onResume` e esconder o banner quando concedido

## 8. Strings

- [x] 8.1 Adicionar ao `res/values/strings.xml`:
         `notification_channel_reminders_name` ("Lembretes de medicamentos"),
         `notification_dose_title` ("Hora do remédio"),
         `notification_dose_text` ("%1$s às %2$s"),
         `notification_action_taken` ("Tomei"),
         `notification_action_confirm` ("Confirmar comprimido"),
         `notification_action_snooze` ("Lembrar em 2 min"),
         `notification_permission_rationale` ("Ative as notificações para receber lembretes nos horários cadastrados."),
         `notification_permission_open_settings` ("Abrir configurações"),
         `notification_exact_alarm_warning` ("Notificações podem chegar com atraso. Toque para corrigir.")

## 9. Alarme diário de meia-noite

- [x] 9.1 Em `RescheduleAllAlarmsUseCase`, após reagendar o dia, agendar um alarme
         repetitivo (`setRepeating`) para 00:01 do próximo dia que dispara `BootReceiver`
         com action `ACTION_MIDNIGHT_RESCHEDULE` — reagenda o dia seguinte automaticamente

## 10. Testes

- [x] 10.1 Criar `ScheduleAlarmsForTodayUseCaseTest` com fake de `NotificationHelper`:
          - `remindersGlobal=true` → alarmes agendados
          - `remindersGlobal=false` → nenhum alarme
          - `remindersEnabled=false` no medicamento → sem alarme
          - Horário já passou → sem alarme
          - Antecipação de N minutos → `triggerAt` correto
- [x] 10.2 Criar `RescheduleAllAlarmsUseCaseTest`:
          - Cancela alarmes existentes antes de reagendar
          - Chama `ScheduleAlarmsForToday` após cancelar
- [x] 10.3 Criar `NotificationActionHandlerTest` (ação Tomei e Snooze via handler extraído):
          - `ACTION_MARK_TAKEN` → `MarkIntakeTakenUseCase` chamado + notificação cancelada
          - `ACTION_SNOOZE` → novo alarme agendado para +2 min + notificação cancelada
- [x] 10.4 Atualizar `SaveMedicationUseCaseTest`:
          - Verificar que `RescheduleAllAlarmsUseCase` é chamado após salvar
- [x] 10.5 Rodar `./gradlew :app:testDebugUnitTest` e confirmar 0 falhas

## 11. Documentação

- [x] 11.1 Atualizar `CHANGELOG.md` com entrada F6 em "Não lançado"
- [x] 11.2 Marcar F6 como feita em `docs/openspec-plan.md`
- [x] 11.3 Executar `openspec archive --change add-scheduling-reminders`
