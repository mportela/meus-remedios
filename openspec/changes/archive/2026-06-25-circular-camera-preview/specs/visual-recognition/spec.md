## MODIFIED Requirements

### Requirement: Auto-captura controlada por configuração
O sistema SHALL permitir que o usuário inicie o reconhecimento de um comprimido por câmera.
Quando `autoCapture` estiver habilitado nas configurações, o sistema SHALL, ao detectar no
frame ao vivo um score de embedding ≥ ao limiar de confiança do preview, **capturar a
imagem automaticamente E iniciar a análise de reconhecimento**, transitando para o estado
de análise e exibindo o resultado (confiante, ambíguo ou sem correspondência) pelo mesmo
fluxo da captura manual. O flash branco e a vibração háptica são sinais de feedback que
acompanham a captura, e NÃO substituem a captura nem a análise. Quando `autoCapture`
estiver desabilitado, o sistema SHALL exibir um botão de captura manual (comportamento
atual preservado).

O frame ao vivo usado para detecção SHALL ser orientado conforme a rotação do sensor antes
do cálculo do embedding, de modo que a similaridade reflita o comprimido na orientação
correta. A pontuação do preview SHALL usar critério próprio baseado em embedding, sem
introduzir componentes de feature ausentes (como proporção espúria) que distorçam o score
calibrado para o pipeline multimodal completo.

O preview ao vivo do CameraX SHALL ser exibido **recortado em formato circular**,
centralizado, envolto por uma **moldura (anel/borda) visível**, com área visível (diâmetro
não-nulo) mesmo dentro de um contêiner rolável. O recorte circular é apenas apresentação
visual: o frame usado para detecção e a foto capturada SHALL continuar usando a imagem
completa do sensor, sem que o recorte reduza a área disponível para o reconhecimento. Como
o preview ao vivo exige a permissão de câmera em runtime (diferente da captura manual por
Intent), o sistema SHALL solicitar a permissão `CAMERA` quando a auto-captura estiver
habilitada; se a permissão for negada, o sistema SHALL degradar para o fluxo de captura
manual (botão), em vez de exibir um preview vazio.

A foto capturada pela auto-captura SHALL ser gravada com os pixels na mesma orientação das
fotos cadastradas (em pé), aplicando a rotação do sensor antes de persistir o arquivo. Isso
é necessário porque o reconhecimento decodifica os pixels da imagem sem honrar metadados de
orientação (EXIF); uma foto de consulta girada degradaria a similaridade de embedding e de
forma, levando a um resultado ambíguo indevido.

#### Scenario: Auto-captura com confiança suficiente
- **WHEN** `autoCapture = true` E o score de embedding do frame ao vivo ≥ ao limiar de confiança do preview
- **THEN** o sistema captura a imagem automaticamente, exibe flash branco e vibração háptica
- **AND** transita para o estado de análise e, ao concluir, exibe o resultado do reconhecimento

#### Scenario: Resultado é exibido após auto-captura
- **WHEN** uma auto-captura é disparada e a análise das fotos de consulta conclui
- **THEN** o sistema SHALL exibir o resultado (confiante, ambíguo ou sem correspondência),
  nunca permanecendo indefinidamente no preview ao vivo após uma detecção confiante

#### Scenario: Frame ao vivo é orientado antes do embedding
- **WHEN** um frame do preview é processado para detecção
- **THEN** o sistema SHALL aplicar a rotação do sensor ao bitmap antes de calcular o embedding,
  de forma que a orientação do comprimido não degrade a similaridade

#### Scenario: Preview ao vivo é exibido como círculo com moldura
- **WHEN** `autoCapture = true` E a permissão `CAMERA` está concedida
- **THEN** o sistema SHALL exibir o preview ao vivo do CameraX recortado em formato circular,
  centralizado, com uma moldura (anel/borda) visível ao redor
- **AND** o círculo SHALL ter diâmetro visível não-nulo (nunca colapsado a zero por estar
  dentro de um contêiner rolável)

#### Scenario: Recorte circular não reduz a área de reconhecimento
- **WHEN** o preview é exibido recortado em círculo e ocorre uma captura (manual ou automática)
- **THEN** o frame usado para detecção e a foto capturada SHALL usar a imagem completa do
  sensor, independentemente do recorte circular aplicado apenas à exibição

#### Scenario: Permissão de câmera concedida habilita o preview
- **WHEN** `autoCapture = true` E a permissão `CAMERA` ainda não foi concedida
- **THEN** o sistema SHALL solicitar a permissão `CAMERA`
- **AND** ao ser concedida, SHALL exibir o preview ao vivo

#### Scenario: Permissão de câmera negada cai no fluxo manual
- **WHEN** `autoCapture = true` E a permissão `CAMERA` é negada
- **THEN** o sistema SHALL exibir o botão de captura manual (que usa Intent, sem exigir a
  permissão), em vez de um preview vazio

#### Scenario: Foto capturada é gravada na orientação correta
- **WHEN** a auto-captura grava a foto de consulta a partir do sensor da câmera
- **THEN** o sistema SHALL aplicar a rotação do sensor aos pixels antes de salvar, de modo
  que a proporção e a orientação da consulta coincidam com as das fotos cadastradas
- **AND** o resultado de um comprimido cadastrado SHALL ser afirmado como confiante (não
  rebaixado a ambíguo por desalinhamento de orientação)

#### Scenario: Auto-captura bloqueada por cooldown
- **WHEN** uma captura automática foi realizada há menos de 2 segundos
- **THEN** o sistema NÃO dispara nova captura automática, mesmo com score suficiente

#### Scenario: Score insuficiente no preview
- **WHEN** `autoCapture = true` E o score de embedding do frame ao vivo < ao limiar de confiança do preview
- **THEN** o sistema NÃO captura automaticamente; botão manual permanece disponível

#### Scenario: Auto-captura desabilitada
- **WHEN** `autoCapture = false`
- **THEN** o sistema usa câmera nativa (Intent) e botão de captura manual; nenhum preview ao vivo é exibido
