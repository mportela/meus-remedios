## MODIFIED Requirements

### Requirement: Decisão conservadora de confiança
O sistema SHALL afirmar a identidade de um medicamento apenas quando a confiança for alta e a
diferença para o segundo candidato for suficiente; caso contrário, SHALL tratar o resultado
como ambíguo, nunca afirmando com baixa confiança. Enquanto o reconhecimento operar sem
embedding (apenas cor e forma), o limiar de confiança SHALL ser conservador o suficiente para
não afirmar identidade com base apenas em semelhança de cor, evitando falsos positivos entre
comprimidos visualmente parecidos.

#### Scenario: Resultado ambíguo dispara segunda foto
- **WHEN** o melhor candidato não atinge o limiar de confiança ou está muito próximo do
  segundo candidato
- **THEN** o sistema SHALL apresentar o resultado como ambíguo
- **AND** SHALL sugerir capturar uma segunda foto (outro lado do comprimido) e/ou listar os
  candidatos para o usuário escolher

#### Scenario: Comprimido diferente não é afirmado (controle negativo)
- **WHEN** a foto de consulta é de um comprimido **diferente** dos cadastrados, porém de cor
  semelhante, e o reconhecimento opera sem embedding
- **THEN** o sistema NÃO SHALL afirmar a identidade como confiante
- **AND** SHALL tratar o resultado como ambíguo ou sem correspondência

#### Scenario: Comprimido cadastrado é afirmado (controle positivo)
- **WHEN** a foto de consulta corresponde de fato a um medicamento cadastrado, com alta
  similaridade e sem competidor próximo
- **THEN** o sistema SHALL afirmar a identidade como confiante
