## MODIFIED Requirements

### Requirement: Scoring determinístico e parametrizável
O sistema SHALL calcular o score de cada candidato como uma combinação ponderada das
similaridades de embedding, cor e forma, normalizada pelos componentes disponíveis, com pesos
e limiares centralizados e cobertos por testes determinísticos. Quando ambas as fotos
(consulta e cadastrada) possuírem embedding, o sistema SHALL incluir a similaridade de
embedding no score, com o peso reservado para esse componente.

#### Scenario: Score sem embedding
- **WHEN** as features comparadas não possuem embedding
- **THEN** o sistema SHALL calcular o score apenas com as similaridades de cor e forma,
  mantendo o resultado no intervalo válido

#### Scenario: Score com embedding
- **WHEN** as features de consulta e cadastrada possuem embedding
- **THEN** o sistema SHALL incluir a similaridade de embedding (cosseno) na combinação
  ponderada, com o resultado mantido no intervalo válido

#### Scenario: Ranking determinístico
- **WHEN** o mesmo conjunto de features de consulta e cadastradas é avaliado
- **THEN** o sistema SHALL produzir o mesmo ranking de candidatos

## ADDED Requirements

### Requirement: Embedding de imagem on-device
O sistema SHALL calcular o embedding de uma foto de comprimido por meio de um modelo
executado inteiramente no dispositivo, sem acesso à rede, produzindo um vetor numérico
estável e comparável por similaridade de cosseno. O modelo SHALL ser versionado junto ao
app (sem download em runtime). Em caso de falha de carregamento ou inferência, o sistema
SHALL tratar o embedding como ausente e prosseguir o reconhecimento com as demais features.

#### Scenario: Embedding calculado no dispositivo
- **WHEN** uma foto de comprimido é processada para reconhecimento
- **THEN** o sistema SHALL produzir seu embedding localmente, sem qualquer acesso à rede

#### Scenario: Falha de inferência não interrompe o reconhecimento
- **WHEN** o modelo de embedding não pode ser carregado ou a inferência falha
- **THEN** o sistema SHALL prosseguir o reconhecimento usando apenas cor e forma
