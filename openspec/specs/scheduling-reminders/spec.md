# scheduling-reminders Specification

## Purpose
TBD - created by archiving change add-scheduling-reminders. Update Purpose after archive.
## Requirements
### Requirement: Agendamento de lembretes por horário

O sistema SHALL agendar uma notificação local para cada dose ativa do dia, `reminderLeadMinutes`
antes do `timeOfDay` do respectivo `ScheduleTime`, desde que `remindersGlobal` e
`Medication.remindersEnabled` estejam ambos ativos.

#### Scenario: Lembrete disparado no horário

- **WHEN** o instante do alarme é atingido e a dose correspondente ainda não foi registrada como TAKEN ou SKIPPED
- **THEN** o sistema SHALL exibir uma notificação com o nome do medicamento e o horário previsto

#### Scenario: Lembrete suprimido por toggle global

- **WHEN** `AppSettings.remindersGlobal` é `false`
- **THEN** o sistema SHALL NOT agendar nenhum alarme

#### Scenario: Lembrete suprimido por toggle do medicamento

- **WHEN** `Medication.remindersEnabled` é `false`
- **THEN** o sistema SHALL NOT agendar alarme para nenhum horário desse medicamento

#### Scenario: Antecipação configurável

- **WHEN** `AppSettings.reminderLeadMinutes` é N
- **THEN** o alarme SHALL disparar N minutos antes do `timeOfDay`

### Requirement: Reagendamento após reinicialização do dispositivo

O sistema SHALL reagendar todos os alarmes pendentes do dia imediatamente após a
reinicialização do dispositivo, sem interação do usuário.

#### Scenario: Boot completo detectado

- **WHEN** o dispositivo reinicia e o boot é concluído
- **THEN** o sistema SHALL cancelar alarmes obsoletos e agendar os alarmes do dia atual

### Requirement: Reagendamento ao alterar cadastro

O sistema SHALL reagendar todos os alarmes do dia sempre que um medicamento ou horário for
criado, editado ou excluído.

#### Scenario: Horário adicionado

- **WHEN** o usuário salva um novo horário para um medicamento com lembretes ativos
- **THEN** o sistema SHALL agendar um alarme para esse horário no dia corrente (se ainda não passou)

#### Scenario: Medicamento excluído

- **WHEN** o usuário exclui um medicamento
- **THEN** o sistema SHALL cancelar todos os alarmes associados a esse medicamento

### Requirement: Ações de notificação de lembrete

A notificação de lembrete SHALL oferecer 4 ações ao usuário:

#### Scenario: Ação "Tomei" registra tomada e descarta notificação

- **WHEN** o usuário toca em "Tomei" na notificação
- **THEN** o sistema SHALL registrar a dose como TAKEN via `MarkIntakeTakenUseCase`
- **AND** SHALL descartar a notificação imediatamente

#### Scenario: Ação "Confirmar comprimido" abre tela de reconhecimento

- **WHEN** o usuário toca em "Confirmar comprimido" na notificação
- **THEN** o sistema SHALL abrir o aplicativo na tela Confirmar (câmera)
- **AND** SHALL descartar a notificação
- **AND** o fluxo de reconhecimento SHALL funcionar normalmente, permitindo registrar a tomada

#### Scenario: Ação "Lembrar em 2 min" reagenda o alarme

- **WHEN** o usuário toca em "Lembrar em 2 min" na notificação
- **THEN** o sistema SHALL agendar um novo alarme para 2 minutos a partir do momento atual
- **AND** SHALL descartar a notificação atual

#### Scenario: Ação "Silenciar" descarta sem registro

- **WHEN** o usuário descarta a notificação sem tocar em nenhuma ação
- **THEN** o sistema SHALL descartar a notificação sem registrar nenhuma tomada

### Requirement: Permissão de notificação (Android 13+)

O sistema SHALL solicitar a permissão `POST_NOTIFICATIONS` ao usuário na primeira abertura
do aplicativo em dispositivos com Android 13 ou superior.

#### Scenario: Permissão concedida

- **WHEN** o usuário concede `POST_NOTIFICATIONS`
- **THEN** o sistema SHALL exibir notificações normalmente

#### Scenario: Permissão negada com possibilidade de reverter

- **WHEN** o usuário nega `POST_NOTIFICATIONS` na primeira solicitação
- **THEN** o sistema SHALL informar que lembretes estão desativados

#### Scenario: Permissão negada permanentemente

- **WHEN** o usuário negou `POST_NOTIFICATIONS` e não pode mais ser solicitada
- **THEN** o sistema SHALL exibir diálogo com botão "Abrir configurações" que leva às
  configurações de notificação do app no sistema Android

### Requirement: Alarmes exatos com fallback

O sistema SHALL usar alarmes exatos (`setExactAndAllowWhileIdle`) quando a permissão
`SCHEDULE_EXACT_ALARM` estiver disponível. Quando indisponível, SHALL usar alarmes
de melhor esforço e SHALL informar o usuário que a pontualidade pode variar.

#### Scenario: Alarme exato disponível

- **WHEN** `AlarmManager.canScheduleExactAlarms()` retorna `true`
- **THEN** o sistema SHALL usar `setExactAndAllowWhileIdle`

#### Scenario: Alarme exato indisponível

- **WHEN** `AlarmManager.canScheduleExactAlarms()` retorna `false`
- **THEN** o sistema SHALL usar `setAndAllowWhileIdle` como fallback

### Requirement: Reagendamento ao alterar configurações de lembrete
O sistema SHALL reagendar imediatamente todos os alarmes do dia quando o usuário
alterar `remindersGlobal` ou `reminderLeadMinutes` via tela de Configurações.

#### Scenario: Reagendamento ao desligar lembretes globais
- **WHEN** o usuário desativa lembretes globais nas Configurações
- **THEN** todos os alarmes pendentes são cancelados e nenhum novo é agendado

#### Scenario: Reagendamento ao mudar antecedência
- **WHEN** o usuário altera `reminderLeadMinutes` nas Configurações (e lembretes estão ativos)
- **THEN** todos os alarmes do dia são cancelados e reagendados com o novo offset

