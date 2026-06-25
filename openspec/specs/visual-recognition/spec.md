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

### Requirement: Combinação de duas fotos
O sistema SHALL permitir combinar a foto inicial com uma segunda foto do comprimido para
desambiguar o resultado, agregando as comparações por medicamento.

#### Scenario: Segunda foto melhora a decisão
- **WHEN** o usuário captura uma segunda foto após um resultado ambíguo
- **THEN** o sistema SHALL recalcular o ranking considerando ambas as fotos de consulta

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

### Requirement: Warm-up do modelo TFLite no startup
O sistema SHALL pré-inicializar o `TfliteEmbedder` (criação do `Interpreter` e carga de
`libtensorflowlite.so`) em thread de background durante o `Application.onCreate()`, de forma
que a primeira foto cadastrada pelo usuário não sofra latência adicional de inicialização de
modelo. A inicialização SHALL ocorrer exclusivamente em `Dispatchers.Default`, nunca na main
thread.

#### Scenario: Primeiro cadastro sem jank de carregamento de modelo
- **WHEN** o usuário adiciona a primeira foto a um medicamento após instalar o app
- **THEN** o modelo TFLite já está inicializado em background
- **AND** a operação de embedding não bloqueia a main thread

### Requirement: Ação pós-reconhecimento confiante

Após um resultado confiante, o sistema SHALL oferecer ação de registrar tomada
diretamente na tela Confirmar, sem exigir navegação para a tela Hoje.

#### Scenario: Botão "Tomei" visível após resultado confiante

- **WHEN** o sistema exibe um resultado confiante de reconhecimento
- **THEN** SHALL exibir botão "Tomei" proeminente abaixo do nome do remédio identificado

#### Scenario: Resultado ambíguo ou sem correspondência não exibe "Tomei"

- **WHEN** o resultado do reconhecimento é Ambiguous, NoMatch ou NoPhotosRegistered
- **THEN** o sistema SHALL NOT exibir botão "Tomei"
  (o usuário não sabe qual remédio é, portanto não pode registrar a tomada)

### Requirement: Auto-captura controlada por configuração
O sistema SHALL respeitar a preferência `autoCapture` de `AppSettings` na tela de
reconhecimento: quando ativa, o foco bem-sucedido da câmera dispara captura automática;
quando inativa, o usuário precisa tocar para capturar.

#### Scenario: Auto-captura ativa — foco dispara captura
- **WHEN** `autoCapture = true` e a câmera foca o comprimido
- **THEN** a foto é capturada automaticamente sem interação do usuário

#### Scenario: Auto-captura inativa — toque necessário
- **WHEN** `autoCapture = false`
- **THEN** a câmera aguarda o toque do usuário no botão de captura para tirar a foto

