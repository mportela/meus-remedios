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
como ambíguo, nunca afirmando com baixa confiança.

#### Scenario: Resultado ambíguo dispara segunda foto
- **WHEN** o melhor candidato não atinge o limiar de confiança ou está muito próximo do
  segundo candidato
- **THEN** o sistema SHALL apresentar o resultado como ambíguo
- **AND** SHALL sugerir capturar uma segunda foto (outro lado do comprimido) e/ou listar os
  candidatos para o usuário escolher

### Requirement: Combinação de duas fotos
O sistema SHALL permitir combinar a foto inicial com uma segunda foto do comprimido para
desambiguar o resultado, agregando as comparações por medicamento.

#### Scenario: Segunda foto melhora a decisão
- **WHEN** o usuário captura uma segunda foto após um resultado ambíguo
- **THEN** o sistema SHALL recalcular o ranking considerando ambas as fotos de consulta

### Requirement: Scoring determinístico e parametrizável
O sistema SHALL calcular o score de cada candidato como uma combinação ponderada das
similaridades de embedding, cor e forma, normalizada pelos componentes disponíveis, com pesos
e limiares centralizados e cobertos por testes determinísticos.

#### Scenario: Score sem embedding
- **WHEN** as features comparadas não possuem embedding
- **THEN** o sistema SHALL calcular o score apenas com as similaridades de cor e forma,
  mantendo o resultado no intervalo válido

#### Scenario: Ranking determinístico
- **WHEN** o mesmo conjunto de features de consulta e cadastradas é avaliado
- **THEN** o sistema SHALL produzir o mesmo ranking de candidatos

