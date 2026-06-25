## ADDED Requirements

### Requirement: Anúncio do resultado de reconhecimento via TalkBack
O sistema SHALL anunciar o resultado do reconhecimento via semântica Compose
(`semantics { contentDescription = ... }`) para que o TalkBack leia o resultado
(confiante / ambíguo / sem match) e o nome do medicamento sem interação manual.

#### Scenario: Resultado confiante é anunciado automaticamente
- **WHEN** o reconhecimento retorna resultado confiante com nome do medicamento
- **THEN** o TalkBack anuncia o nome do medicamento e a mensagem de confirmação

#### Scenario: Resultado ambíguo é anunciado com instrução de ação
- **WHEN** o reconhecimento retorna resultado ambíguo
- **THEN** o TalkBack anuncia "Resultado incerto" e orienta o usuário a fotografar o verso

#### Scenario: Sem match é anunciado com instrução clara
- **WHEN** o reconhecimento não encontra match
- **THEN** o TalkBack anuncia "Comprimido não reconhecido" e orienta o próximo passo
