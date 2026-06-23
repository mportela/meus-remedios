# visual-recognition Specification

## Purpose
TBD - created by archiving change add-visual-recognition. Update Purpose after archive.
## Requirements
### Requirement: Reconhecimento de medicamento pela câmera
O sistema SHALL permitir capturar a foto de um comprimido pela câmera e identificar, entre os
medicamentos cadastrados com foto, qual corresponde ao comprimido, processando tudo localmente
no dispositivo. Em builds com recursos de desenvolvimento habilitados, o sistema SHALL
permitir, adicionalmente, escolher a imagem de consulta a partir da galeria do dispositivo
como alternativa à câmera; em builds de produção, a fonte da imagem SHALL ser exclusivamente
a câmera.

#### Scenario: Identificação confiante
- **WHEN** a foto de consulta corresponde com alta confiança a um único medicamento cadastrado
- **THEN** o sistema SHALL exibir o nome do medicamento identificado em destaque

#### Scenario: Sem fotos cadastradas
- **WHEN** nenhum medicamento cadastrado possui foto
- **THEN** o sistema SHALL informar que não há fotos cadastradas para comparação

#### Scenario: Comprimido não reconhecido
- **WHEN** nenhuma foto cadastrada atinge a similaridade mínima com a foto de consulta
- **THEN** o sistema SHALL informar que o comprimido não foi reconhecido

#### Scenario: Imagem da galeria em build de desenvolvimento
- **WHEN** os recursos de desenvolvimento estão habilitados e o usuário escolhe uma imagem da
  galeria em vez de capturar pela câmera
- **THEN** o sistema SHALL usar essa imagem como foto de consulta e executar a mesma análise
  de reconhecimento, produzindo o mesmo tipo de resultado da captura por câmera

#### Scenario: Galeria indisponível em produção
- **WHEN** os recursos de desenvolvimento estão desabilitados (build de produção)
- **THEN** o sistema NÃO SHALL oferecer a opção de escolher a imagem pela galeria

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

### Requirement: Combinação de duas fotos
O sistema SHALL permitir combinar a foto inicial com uma segunda foto do comprimido para
desambiguar o resultado, agregando as comparações por medicamento.

#### Scenario: Segunda foto melhora a decisão
- **WHEN** o usuário captura uma segunda foto após um resultado ambíguo
- **THEN** o sistema SHALL recalcular o ranking considerando ambas as fotos de consulta

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

