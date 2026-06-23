## MODIFIED Requirements

### Requirement: Scoring determinístico e parametrizável
O sistema SHALL calcular o score de cada candidato como uma combinação ponderada das
similaridades de embedding, cor, forma e **imprint (texto gravado)**, normalizada pelos
componentes disponíveis, com pesos e limiares centralizados e cobertos por testes
determinísticos. Quando ambas as fotos (consulta e cadastrada) possuírem um determinado
componente, o sistema SHALL incluí-lo no score com o peso reservado para ele; componentes
ausentes em qualquer um dos lados SHALL ser ignorados.

#### Scenario: Score sem embedding
- **WHEN** as features comparadas não possuem embedding
- **THEN** o sistema SHALL calcular o score apenas com as similaridades disponíveis (cor,
  forma e, se houver, imprint), mantendo o resultado no intervalo válido

#### Scenario: Score com embedding
- **WHEN** as features de consulta e cadastrada possuem embedding
- **THEN** o sistema SHALL incluir a similaridade de embedding (cosseno) na combinação
  ponderada, com o resultado mantido no intervalo válido

#### Scenario: Score com imprint
- **WHEN** as fotos de consulta e cadastrada possuem imprint (texto gravado)
- **THEN** o sistema SHALL incluir a similaridade de imprint na combinação ponderada, com o
  peso reservado para esse componente, mantendo o resultado no intervalo válido

#### Scenario: Imprint ausente é ignorado
- **WHEN** ao menos uma das fotos comparadas não possui imprint
- **THEN** o sistema SHALL ignorar o componente de imprint e normalizar o score pelos demais
  componentes presentes

#### Scenario: Ranking determinístico
- **WHEN** o mesmo conjunto de features de consulta e cadastradas é avaliado
- **THEN** o sistema SHALL produzir o mesmo ranking de candidatos
