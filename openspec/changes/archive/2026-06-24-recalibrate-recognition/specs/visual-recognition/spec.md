## MODIFIED Requirements

### Requirement: Decisão conservadora de confiança
O sistema SHALL afirmar a identidade de um medicamento apenas quando a confiança for alta e a
diferença para o segundo candidato for suficiente; caso contrário, SHALL tratar o resultado
como ambíguo, nunca afirmando com baixa confiança. Com o engine multimodal completo
(embedding TFLite + cor + forma + imprint), o limiar de confiança SHALL ser calibrado para o
regime com embedding ativo, de forma que:
- Um comprimido genuinamente igual (embedding similar, cor e forma compatíveis) seja afirmado
  como confiante.
- Um comprimido diferente com cor parecida mas embedding distante **não** seja afirmado como
  confiante.
- O comportamento de fallback (componentes ausentes) mantenha a decisão conservadora:
  quando operando sem embedding, o limiar efetivo permanece conservador o suficiente para
  não afirmar identidade com base apenas em cor e forma.

#### Scenario: Identificação confiante com embedding ativo
- **WHEN** a foto de consulta tem embedding disponível e o top-1 supera `THRESHOLD_CONFIDENT`
  com margem sobre o top-2
- **THEN** o sistema SHALL afirmar a identidade do medicamento

#### Scenario: Comprimido diferente não é afirmado — controle negativo com embedding
- **WHEN** a foto de consulta é de um comprimido diferente dos cadastrados, mesmo com cor
  parecida, e o embedding distingue os comprimidos (cosine baixo)
- **THEN** o sistema NÃO SHALL afirmar a identidade como confiante
- **AND** SHALL tratar o resultado como ambíguo ou sem correspondência

#### Scenario: Comprimido diferente não é afirmado — controle negativo sem embedding
- **WHEN** o reconhecimento opera sem embedding (fallback cor+forma) e a foto de consulta é de
  um comprimido diferente com cor parecida
- **THEN** o sistema NÃO SHALL afirmar a identidade como confiante mesmo com score de cor alto

#### Scenario: Comprimido cadastrado é afirmado — controle positivo com embedding
- **WHEN** a foto de consulta corresponde de fato a um medicamento cadastrado, com embedding
  similar e sem competidor próximo
- **THEN** o sistema SHALL afirmar a identidade como confiante

#### Scenario: Ambiguidade quando candidatos estão próximos
- **WHEN** o melhor candidato não atinge o limiar de confiança ou a margem sobre o segundo é
  insuficiente
- **THEN** o sistema SHALL apresentar o resultado como ambíguo independentemente de quais
  componentes de feature estão disponíveis

### Requirement: Scoring determinístico e parametrizável
O sistema SHALL calcular o score de cada candidato como uma combinação ponderada das
similaridades de embedding, cor, forma e imprint, normalizada pelos componentes disponíveis,
com pesos e limiares centralizados em `RecognitionParams` e cobertos por testes
determinísticos. Os parâmetros SHALL refletir o regime final multimodal (embedding ativo),
sem comentários de "provisório". O golden set de testes SHALL cobrir todos os modos de
componente: completo (embedding+cor+forma+imprint), sem imprint, sem embedding (fallback), e
cenários de ambiguidade.

#### Scenario: Golden set — positivo full (todos os componentes)
- **WHEN** query e candidato têm embedding similar, cor igual, forma igual e imprint idêntico
- **THEN** o score SHALL superar `THRESHOLD_CONFIDENT` e o sistema SHALL decidir CONFIANTE

#### Scenario: Golden set — positivo sem imprint
- **WHEN** query e candidato têm embedding similar, cor e forma compatíveis, mas sem imprint
- **THEN** o score SHALL superar `THRESHOLD_CONFIDENT` e o sistema SHALL decidir CONFIANTE

#### Scenario: Golden set — negativo com embedding distante
- **WHEN** query tem cor parecida com um cadastrado mas embedding distante (comprimido diferente)
- **THEN** o score SHALL ficar abaixo de `THRESHOLD_CONFIDENT` e o sistema NÃO SHALL decidir CONFIANTE

#### Scenario: Golden set — fallback sem embedding (compatibilidade retroativa)
- **WHEN** nenhuma das fotos tem embedding (cadastros antigos)
- **THEN** o score é calculado com cor e forma, e o limiar efetivo mantém a proteção contra
  falso positivo observada na F4.1
