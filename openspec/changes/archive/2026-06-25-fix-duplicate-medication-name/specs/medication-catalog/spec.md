## ADDED Requirements

### Requirement: Unicidade de nome de medicamento
O sistema SHALL impedir o cadastro ou edição de um medicamento quando já existir outro
medicamento com o mesmo nome (comparação case-insensitive após trim). A checagem SHALL
excluir o próprio medicamento em caso de edição.

#### Scenario: Tentar salvar nome já existente (novo cadastro)
- **WHEN** o usuário tenta salvar um medicamento com um nome igual ao de outro já cadastrado
- **THEN** o sistema NÃO persiste o medicamento
- **AND** exibe mensagem de erro inline abaixo do campo "Nome do remédio"

#### Scenario: Tentar salvar nome já existente (edição)
- **WHEN** o usuário edita um medicamento e escolhe um nome igual ao de um medicamento diferente
- **THEN** o sistema NÃO persiste a edição
- **AND** exibe mensagem de erro inline abaixo do campo "Nome do remédio"

#### Scenario: Editar mantendo o mesmo nome (sem conflito)
- **WHEN** o usuário salva um medicamento sem alterar o seu próprio nome
- **THEN** o sistema persiste normalmente sem erro de duplicidade

#### Scenario: Nomes diferindo apenas em capitalização ou espaços
- **WHEN** o usuário tenta salvar "caltrat" sendo que "Caltrat" já existe
- **THEN** o sistema trata como duplicata e exibe o erro inline
