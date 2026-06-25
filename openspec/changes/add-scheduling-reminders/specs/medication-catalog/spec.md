## MODIFIED Requirements

### Requirement: Salvar medicamento aciona reagendamento de alarmes

Após salvar ou excluir um medicamento (incluindo seus horários), o sistema SHALL reagendar
todos os alarmes do dia de forma atômica.

#### Scenario: Medicamento salvo com horários

- **WHEN** o usuário salva um medicamento (novo ou editado) com pelo menos um horário ativo
- **THEN** o sistema SHALL reconciliar os horários no banco
- **AND** SHALL reagendar todos os alarmes do dia corrente em seguida
