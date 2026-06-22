# medication-catalog Specification

## Purpose
TBD - created by archiving change add-medication-catalog. Update Purpose after archive.
## Requirements
### Requirement: Cadastro de medicamento
O sistema SHALL permitir cadastrar um medicamento com nome (obrigatório) e
campos opcionais (dosagem, observações, tipo de período, datas de início/fim e
flag de lembretes), persistindo-o na camada de dados local.

#### Scenario: Cadastro mínimo válido
- **WHEN** o usuário informa um nome e salva o medicamento
- **THEN** o medicamento é persistido com os valores padrão dos demais campos
- **AND** passa a aparecer na lista de medicamentos

#### Scenario: Nome obrigatório
- **WHEN** o usuário tenta salvar um medicamento sem nome (vazio ou só espaços)
- **THEN** o sistema rejeita a operação e sinaliza que o nome é obrigatório

#### Scenario: Intervalo de datas inválido
- **WHEN** o usuário informa uma data de término anterior à data de início
- **THEN** o sistema rejeita a operação e sinaliza o intervalo inválido

### Requirement: Edição e exclusão de medicamento
O sistema SHALL permitir editar os dados de um medicamento existente e excluí-lo,
refletindo as alterações na camada de dados.

#### Scenario: Edição persistida
- **WHEN** o usuário altera campos de um medicamento e salva
- **THEN** as alterações são persistidas e refletidas na lista e no detalhe

#### Scenario: Exclusão remove o medicamento
- **WHEN** o usuário confirma a exclusão de um medicamento
- **THEN** o medicamento é removido da camada de dados e da lista
- **AND** seus horários associados também são removidos

### Requirement: Listagem e busca de medicamentos
O sistema SHALL exibir a lista de medicamentos cadastrados ordenada por nome e
permitir filtrá-la por uma consulta textual de nome, atualizando reativamente.

#### Scenario: Lista reativa ordenada
- **WHEN** a tela de lista é exibida
- **THEN** mostra os medicamentos ordenados por nome
- **AND** reflete automaticamente inclusões, edições e exclusões

#### Scenario: Busca por nome
- **WHEN** o usuário digita parte de um nome no campo de busca
- **THEN** a lista exibe apenas os medicamentos cujo nome corresponde à consulta

### Requirement: Horários do medicamento
O sistema SHALL permitir definir um ou mais horários (hora do dia e dias da
semana) para um medicamento durante o cadastro/edição, persistindo-os associados
ao medicamento.

#### Scenario: Adicionar horário
- **WHEN** o usuário adiciona um horário com hora e dias da semana ao medicamento
- **THEN** o horário é persistido e associado àquele medicamento

#### Scenario: Remover horário
- **WHEN** o usuário remove um horário do medicamento
- **THEN** o horário deixa de existir para aquele medicamento

### Requirement: Toggle de lembrete por medicamento
O sistema SHALL permitir ativar ou desativar os lembretes de um medicamento
individualmente por meio de uma opção "avise-me".

#### Scenario: Desativar lembrete individual
- **WHEN** o usuário desativa a opção "avise-me" de um medicamento
- **THEN** a preferência é persistida no medicamento para uso futuro pela agenda

### Requirement: UI acessível do catálogo
O sistema SHALL apresentar as telas do catálogo com requisitos de acessibilidade:
campos e botões grandes, rótulos e textos de ajuda em pt-BR e semântica adequada
para leitores de tela.

#### Scenario: Navegação por leitor de tela
- **WHEN** o usuário navega o cadastro com um leitor de tela (TalkBack)
- **THEN** cada campo e ação expõe rótulo descritivo em pt-BR

