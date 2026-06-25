## ADDED Requirements

### Requirement: Reagendamento ao alterar configurações de lembrete
O sistema SHALL reagendar imediatamente todos os alarmes do dia quando o usuário
alterar `remindersGlobal` ou `reminderLeadMinutes` via tela de Configurações.

#### Scenario: Reagendamento ao desligar lembretes globais
- **WHEN** o usuário desativa lembretes globais nas Configurações
- **THEN** todos os alarmes pendentes são cancelados e nenhum novo é agendado

#### Scenario: Reagendamento ao mudar antecedência
- **WHEN** o usuário altera `reminderLeadMinutes` nas Configurações (e lembretes estão ativos)
- **THEN** todos os alarmes do dia são cancelados e reagendados com o novo offset
