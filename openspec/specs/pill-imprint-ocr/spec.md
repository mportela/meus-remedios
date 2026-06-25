# pill-imprint-ocr Specification

## Purpose
Especificar a leitura de texto impresso no comprimido via ML Kit OCR, usado como dado auxiliar ao reconhecimento visual para aumentar a precisão da identificação.
## Requirements
### Requirement: Leitura on-device do texto gravado no comprimido
O sistema SHALL ler, ao processar a foto de um comprimido, o texto eventualmente gravado
(imprint — letras e/ou números) por meio de um reconhecedor de texto executado
**inteiramente no dispositivo**, sem qualquer acesso à rede e sem download de modelo em
runtime. O texto reconhecido SHALL ser normalizado de forma determinística (maiúsculas,
apenas caracteres alfanuméricos, espaços colapsados). Quando nenhum texto for reconhecido,
a foto não possuir imprint, ou o reconhecimento falhar, o sistema SHALL tratar o imprint
como ausente, sem interromper a extração das demais features.

#### Scenario: Imprint reconhecido e normalizado
- **WHEN** uma foto de comprimido com letras/números gravados é processada
- **THEN** o sistema SHALL produzir o texto do imprint localmente, sem acesso à rede
- **AND** SHALL normalizá-lo de forma determinística (maiúsculas, apenas alfanuméricos)

#### Scenario: Comprimido sem imprint
- **WHEN** a foto não contém texto gravado legível
- **THEN** o sistema SHALL tratar o imprint como ausente

#### Scenario: Falha de OCR não interrompe a extração
- **WHEN** o reconhecedor de texto não pode ser carregado ou a leitura falha
- **THEN** o sistema SHALL prosseguir com as demais features e deixar o imprint ausente

### Requirement: Inicialização do reconhecedor fora da main thread
O sistema SHALL garantir que a criação do cliente de OCR (`TextRecognition.getClient`) e
qualquer leitura de arquivo de imagem no fluxo de OCR NÃO ocorram na main thread. A
implementação SHALL usar inicialização lazy do cliente e dispatcher de I/O explícito
(`withContext(Dispatchers.IO)`) na função de leitura, de forma que o primeiro uso nunca
cause jank visível.

#### Scenario: Sem jank no primeiro uso
- **WHEN** o OCR de imprint é acionado pela primeira vez após install
- **THEN** a inicialização do modelo e a leitura da imagem ocorrem em thread de background
- **AND** a main thread não é bloqueada

### Requirement: OCR de imprint 100% offline
O sistema SHALL realizar a leitura do imprint sem qualquer acesso à rede, com o modelo de
reconhecimento embarcado no aplicativo. O aplicativo NÃO SHALL declarar a permissão
`INTERNET` no manifesto final (mergeado), ainda que bibliotecas de terceiros tentem
declará-la.

#### Scenario: Sem conectividade
- **WHEN** o dispositivo está sem internet
- **THEN** a leitura do imprint funciona normalmente

#### Scenario: Permissão de rede não entra no APK
- **WHEN** o aplicativo é construído com a biblioteca de OCR
- **THEN** o manifesto final NÃO SHALL conter a permissão `INTERNET`

