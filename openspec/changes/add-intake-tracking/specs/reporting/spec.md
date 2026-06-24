## MODIFIED Requirements

### Requirement: Relatório do dia

O sistema SHALL apresentar, para uma data selecionada, as doses esperadas derivadas dos
horários cadastrados dos medicamentos ativos naquela data, agrupadas por período do dia
(manhã, tarde, noite) e ordenadas por horário, com o respectivo status de cada dose e
**ações para registrar tomada ou pulo diretamente na lista**.

#### Scenario: Doses derivadas dos horários do dia

- **WHEN** um medicamento ativo possui um horário cujo dia da semana inclui a data
  selecionada e não há registro de tomada
- **THEN** o sistema SHALL exibir uma dose para aquele horário
- **AND** SHALL classificá-la como "atrasada" se o horário já passou ou como "pendente"
  caso contrário

#### Scenario: Dose com registro de tomada

- **WHEN** existe um `IntakeLog` correspondente à dose com status TAKEN
- **THEN** o sistema SHALL exibir a dose como "tomada" com indicador visual distinto
- **AND** SHALL exibir ação para desfazer (volta a pendente/atrasado)

#### Scenario: Dose com registro de pulo

- **WHEN** existe um `IntakeLog` correspondente à dose com status SKIPPED
- **THEN** o sistema SHALL exibir a dose como "pulada" com indicador visual distinto
- **AND** SHALL exibir ação para desfazer (volta a pendente/atrasado)

#### Scenario: Ação de marcar tomada na lista

- **WHEN** o usuário aciona "tomei" na dose exibida na tela Hoje
- **THEN** o sistema SHALL registrar a tomada (ver spec intake-tracking)
- **AND** SHALL atualizar o status da dose na lista sem recarregar a tela

#### Scenario: Ação de pular na lista

- **WHEN** o usuário aciona "pular" na dose exibida na tela Hoje
- **THEN** o sistema SHALL registrar o pulo (ver spec intake-tracking)
- **AND** SHALL atualizar o status da dose na lista sem recarregar a tela

#### Scenario: Agrupamento por período

- **WHEN** o relatório do dia contém doses em horários distintos
- **THEN** o sistema SHALL agrupá-las em manhã, tarde e noite
- **AND** SHALL ordená-las por horário dentro de cada período

#### Scenario: Resumo de contagens

- **WHEN** o relatório do dia é exibido
- **THEN** o sistema SHALL apresentar a contagem de doses tomadas, pendentes, atrasadas
  e puladas, refletindo o estado real persistido
