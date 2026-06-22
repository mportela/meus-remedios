## ADDED Requirements

### Requirement: Adicionar fotos ao medicamento
O sistema SHALL permitir adicionar uma ou mais fotos do comprimido a um
medicamento, capturando pela câmera do dispositivo ou selecionando da galeria, e
indicando o lado (frente ou verso) de cada foto.

#### Scenario: Foto capturada pela câmera
- **WHEN** o usuário captura uma foto do comprimido pela câmera no formulário
- **THEN** a foto é associada ao medicamento com o lado indicado

#### Scenario: Foto selecionada da galeria
- **WHEN** o usuário seleciona uma imagem da galeria
- **THEN** a imagem é associada ao medicamento com o lado indicado

#### Scenario: Medicamento sem foto permanece válido
- **WHEN** o usuário salva um medicamento sem nenhuma foto
- **THEN** o medicamento é salvo normalmente, porém sem participar do
  reconhecimento visual

### Requirement: Armazenamento privado das fotos
O sistema SHALL armazenar os arquivos de imagem em diretório privado do
aplicativo, referenciando apenas o caminho do arquivo no banco de dados, sem
expô-los a outros aplicativos nem à rede.

#### Scenario: Arquivo em área privada
- **WHEN** uma foto é adicionada a um medicamento
- **THEN** o arquivo é gravado no armazenamento privado do app
- **AND** o registro da foto referencia o caminho desse arquivo

#### Scenario: Remoção apaga o arquivo
- **WHEN** o usuário remove uma foto de um medicamento
- **THEN** o registro e o arquivo de imagem correspondente são apagados

### Requirement: Extração e persistência de features
O sistema SHALL extrair, ao adicionar uma foto, as features determinísticas de
reconhecimento — cor dominante em espaço Lab e proporção (aspect ratio) — e
persisti-las junto da foto, reservando o campo de embedding para preenchimento
pelo engine de reconhecimento.

#### Scenario: Features persistidas no cadastro
- **WHEN** uma foto é adicionada a um medicamento
- **THEN** sua cor dominante em Lab e sua proporção são calculadas e persistidas

#### Scenario: Conversão de cor determinística
- **WHEN** a cor de um pixel sRGB conhecido é convertida para Lab
- **THEN** o resultado é determinístico e reproduzível entre execuções

### Requirement: Operação 100% offline
O sistema SHALL realizar toda a captura, armazenamento e extração de features das
fotos no dispositivo, sem qualquer acesso à rede.

#### Scenario: Sem conectividade
- **WHEN** o dispositivo está sem internet
- **THEN** adicionar, extrair features e remover fotos funcionam normalmente
