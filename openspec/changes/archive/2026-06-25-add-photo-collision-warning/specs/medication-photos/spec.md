## ADDED Requirements

### Requirement: Verificação de colisão visual ao salvar fotos
O sistema SHALL, ao salvar um medicamento com fotos pendentes que possuam embedding
extraído, comparar as features dessas fotos com todas as fotos já cadastradas de outros
medicamentos usando o scorer de reconhecimento. Se algum candidato tiver score ≥
`THRESHOLD_CONFIDENT`, o sistema SHALL exibir um aviso não-bloqueante antes de persistir.

#### Scenario: Foto pendente similar a medicamento existente
- **WHEN** o usuário tenta salvar um medicamento com uma foto pendente cujo embedding é
  similar ao de outro medicamento já cadastrado (score ≥ THRESHOLD_CONFIDENT)
- **THEN** o sistema exibe um aviso com o nome do medicamento mais similar
- **AND** oferece as ações "Salvar assim mesmo" e "Cancelar"
- **AND** NÃO persiste automaticamente — aguarda a decisão do usuário

#### Scenario: Usuário confirma save com aviso de colisão
- **WHEN** o usuário seleciona "Salvar assim mesmo" no aviso de colisão
- **THEN** o medicamento e as fotos são persistidos normalmente

#### Scenario: Usuário cancela ao ver o aviso
- **WHEN** o usuário seleciona "Cancelar" no aviso de colisão
- **THEN** o medicamento NÃO é persistido e o usuário permanece no formulário

#### Scenario: Foto pendente sem embedding não aciona verificação
- **WHEN** o usuário salva um medicamento com foto(s) pendente(s) sem embedding (extração
  falhou ou não foi realizada)
- **THEN** o sistema persiste normalmente sem exibir aviso de colisão

#### Scenario: Nenhuma foto de outros medicamentos cadastrada
- **WHEN** o usuário salva o primeiro medicamento com foto
- **THEN** o sistema persiste normalmente sem aviso (sem candidatos para comparar)
