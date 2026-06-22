# reporting Specification

## Purpose
TBD - created by archiving change add-reporting-and-browsing. Update Purpose after archive.
## Requirements
### Requirement: Relatório do dia
O sistema SHALL apresentar, para uma data selecionada, as doses esperadas derivadas dos
horários cadastrados dos medicamentos ativos naquela data, agrupadas por período do dia
(manhã, tarde, noite) e ordenadas por horário, com o respectivo status de cada dose.

#### Scenario: Doses derivadas dos horários do dia
- **WHEN** um medicamento ativo possui um horário cujo dia da semana inclui a data
  selecionada e não há registro de tomada
- **THEN** o sistema SHALL exibir uma dose para aquele horário
- **AND** SHALL classificá-la como "atrasada" se o horário já passou ou como "pendente"
  caso contrário

#### Scenario: Dose com registro de tomada
- **WHEN** existe um `IntakeLog` correspondente à dose com status TAKEN
- **THEN** o sistema SHALL exibir a dose como "tomada"

#### Scenario: Agrupamento por período
- **WHEN** o relatório do dia contém doses em horários distintos
- **THEN** o sistema SHALL agrupá-las em manhã, tarde e noite
- **AND** SHALL ordená-las por horário dentro de cada período

#### Scenario: Resumo de contagens
- **WHEN** o relatório do dia é exibido
- **THEN** o sistema SHALL apresentar a contagem de doses tomadas, pendentes e atrasadas

### Requirement: Timeline navegável por dia
O sistema SHALL permitir navegar entre dias para visualizar o relatório de cada data,
incluindo a seleção de dias dentro da semana corrente.

#### Scenario: Selecionar outro dia
- **WHEN** o usuário seleciona um dia diferente na timeline
- **THEN** o sistema SHALL atualizar o relatório para a data escolhida

### Requirement: Detalhe do medicamento
O sistema SHALL apresentar uma tela de detalhe do medicamento contendo seus dados
(nome, dosagem, observações, período, datas e lembretes), suas fotos cadastradas, seus
horários e o histórico recente de tomadas.

#### Scenario: Abrir detalhe a partir da lista
- **WHEN** o usuário toca em um medicamento na lista
- **THEN** o sistema SHALL abrir a tela de detalhe daquele medicamento

#### Scenario: Editar a partir do detalhe
- **WHEN** o usuário aciona a ação de editar no detalhe
- **THEN** o sistema SHALL abrir o formulário de edição do medicamento

### Requirement: Histórico limitado pela retenção
O sistema SHALL exibir, no detalhe do medicamento, apenas os registros de tomada cuja
data esteja dentro do período de retenção configurado nas configurações do app.

#### Scenario: Registro fora da retenção
- **WHEN** existe um registro de tomada mais antigo que o período de retenção
- **THEN** o sistema SHALL omiti-lo do histórico exibido

### Requirement: Navegação por abas
O sistema SHALL oferecer navegação por abas entre a tela "Hoje" (relatório do dia) e a
lista "Meus remédios", de forma acessível ao público idoso.

#### Scenario: Alternar entre abas
- **WHEN** o usuário toca em uma aba da barra de navegação
- **THEN** o sistema SHALL exibir a tela correspondente preservando o estado da aba

