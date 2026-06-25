## Context

O app possui `ScheduleTime` (horário + dias da semana) e `Medication.remindersEnabled` +
`AppSettings.remindersGlobal` + `AppSettings.reminderLeadMinutes`, mas nenhum mecanismo de
disparo existe. A solução usa `AlarmManager` com alarmes exatos para garantir pontualidade,
sem WorkManager (latência imprevisível) e sem servidor (restrição offline).

## Goals / Non-Goals

**Goals:**
- Notificar o usuário no horário de cada dose ativa (com antecipação configurável).
- Reagendar automaticamente após boot e após qualquer mudança de cadastro.
- Respeitar `remindersEnabled` por medicamento e `remindersGlobal` nas configurações.
- Usar alarmes exatos com fallback gracioso para inexatos se o usuário negar a permissão
  `SCHEDULE_EXACT_ALARM`.

**Non-Goals:**
- Notificações persistentes / ongoing — apenas aviso pontual por dose.
- Agendar mais de um dia à frente (alarmes do dia atual apenas; a meia-noite agenda o próximo).
- Histórico de snoozes ou configuração de duração do snooze por dose.

## Decisions

**D1 — `AlarmManager` com alarmes exatos, sem WorkManager**
`WorkManager` tem janela de execução de até 15 min; lembretes de remédio precisam ser
pontuais. `AlarmManager.setExactAndAllowWhileIdle()` garante disparo mesmo em Doze Mode.
Alternativa descartada: `setExact()` sem `AllowWhileIdle` falha em Doze.

**D2 — Um alarme por (medicationId, scheduleTimeId) por dia**
`PendingIntent` com `requestCode = scheduleTimeId.toInt()` identifica unicamente cada alarme.
Cancela-se com o mesmo `requestCode`. Evita colisão entre horários diferentes.

**D3 — `RescheduleAllAlarmsUseCase` como ponto único de verdade**
Em vez de agendar/cancelar incrementalmente, recalcula todos os alarmes do dia do zero.
Simplifica a lógica: cancela todos os `PendingIntent` existentes (por scheduleTimeId) e
reagenda. Chamado em 3 pontos: boot, `SaveMedicationUseCase` (pós-salvar/deletar) e
meia-noite (via alarme recorrente diário).

**D4 — Alarme diário de meia-noite para o dia seguinte**
Um `AlarmManager.setRepeating()` às 00:01 cancela os alarmes do dia que passou e agenda os
do novo dia. Alternativa: `setExactAndAllowWhileIdle` encadeado — mais complexo sem ganho.

**D5 — Fallback de `SCHEDULE_EXACT_ALARM`**
Android 12+ exige permissão especial. Verificar `AlarmManager.canScheduleExactAlarms()`;
se falso, usar `setAndAllowWhileIdle()` (inexato, melhor esforço). Mostrar aviso na UI
de configurações se alarmes exatos não estiverem disponíveis.

**D6 — Canal de notificação `REMINDERS` criado no `Application.onCreate`**
Canal de importância `HIGH` (toca som, aparece no topo). Uma vez criado, o Android o
mantém mesmo que o app seja atualizado. Nome localizado em `strings.xml`.

**D7 — `NotificationHelper` como wrapper injetável**
Encapsula `NotificationManagerCompat` e `AlarmManager` para facilitar testes (sem
Robolectric, basta mockar `NotificationHelper`).

**D8 — 4 ações na notificação via `NotificationActionReceiver`**
A notificação inclui 4 botões de ação:
- **"Tomei"**: `PendingIntent` para `NotificationActionReceiver` com action `ACTION_MARK_TAKEN`;
  injeta `MarkIntakeTakenUseCase`, usa sobrecarga com `ScheduledDose` (quando `scheduleTimeId`
  não nulo) ou ad-hoc (nulo); cancela a notificação em seguida.
- **"Confirmar comprimido"**: `PendingIntent` com `FLAG_ACTIVITY_NEW_TASK` para `MainActivity`
  com extra `NAVIGATE_TO_RECOGNITION = true`; a `MainActivity` detecta o extra no `onNewIntent`
  e navega para a tab Confirmar; descarta a notificação.
- **"Lembrar em 2 min"**: `PendingIntent` para `NotificationActionReceiver` com action
  `ACTION_SNOOZE`; reagenda um alarme exato para `now + 2 min` com os mesmos extras;
  cancela a notificação atual.
- **"Silenciar"**: dismiss padrão (sem `PendingIntent` extra — o usuário desliza ou toca fora).

O `NotificationActionReceiver` recebe os extras: `medicationId`, `medicationName`,
`scheduleTimeId` (nullable), `scheduledAt` (Instant ISO string), `date` (LocalDate ISO),
`notificationId`. É anotado com `@AndroidEntryPoint` e injeta `MarkIntakeTakenUseCase`
e `NotificationHelper`.

Alternativa descartada: `RemoteViews` customizado — mais complexo e desnecessário para 4 botões.

**D9 — Fluxo de recuperação de permissões**
Para `POST_NOTIFICATIONS`:
- Primeira abertura: pede via `ActivityResultContracts.RequestPermission()`.
- Se negada permanentemente (`shouldShowRequestPermissionRationale` retorna `false` após
  a segunda negativa): exibe `AlertDialog` com botão "Abrir configurações" que abre
  `Settings.ACTION_APP_NOTIFICATION_SETTINGS`.
Para `SCHEDULE_EXACT_ALARM` (Android 12+):
- Se `!canScheduleExactAlarms()`: exibe aviso persistente na tela de configurações do app
  com botão "Abrir configurações" que leva a `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`.
- O aviso some automaticamente quando a permissão é concedida (via `onResume`).

## Risks / Trade-offs

- **`SCHEDULE_EXACT_ALARM` negada pelo usuário** → alarmes inexatos; notificação pode
  chegar com atraso de minutos. Mitigação: informar o usuário na tela de configurações.
- **Muitos horários** → muitos `PendingIntent`. Com dezenas de medicamentos e horários por
  dia o impacto é negligenciável; o Android suporta centenas de alarmes por app.
- **`reminderLeadMinutes = 0`** → alarme no momento exato do horário. Aceito.
- **Deletar medicamento não cancela alarmes imediatamente** se o `RescheduleAll` falhar.
  Mitigação: `AlarmReceiver` verifica se o medicamento ainda existe antes de notificar.

## Migration Plan

1. Adicionar permissões ao manifesto.
2. Criar canal de notificação no `Application.onCreate`.
3. Implementar `AlarmReceiver`, `BootReceiver`, `NotificationHelper`, use cases.
4. Hookear `SaveMedicationUseCase` → `RescheduleAllAlarmsUseCase`.
5. Solicitar `POST_NOTIFICATIONS` na primeira abertura (Android 13+).
6. Sem migração de dados — os `ScheduleTime` existentes já têm todos os dados necessários.
