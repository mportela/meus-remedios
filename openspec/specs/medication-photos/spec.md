# medication-photos Specification

## Purpose
TBD - created by archiving change add-medication-photos. Update Purpose after archive.
## Requirements
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
O sistema SHALL extrair, ao adicionar uma foto, as features de reconhecimento — cor dominante
em espaço Lab, proporção (aspect ratio) e **embedding** de imagem calculado por um modelo
on-device — e persisti-las junto da foto. A extração SHALL ocorrer 100% no dispositivo, sem
acesso à rede. Caso o modelo de embedding não esteja disponível ou falhe, o sistema SHALL
persistir as demais features e deixar o embedding ausente, sem impedir o cadastro.

#### Scenario: Features persistidas no cadastro
- **WHEN** uma foto é adicionada a um medicamento
- **THEN** sua cor dominante em Lab, sua proporção e seu embedding são calculados e persistidos

#### Scenario: Conversão de cor determinística
- **WHEN** a cor de um pixel sRGB conhecido é convertida para Lab
- **THEN** o resultado é determinístico e reproduzível entre execuções

#### Scenario: Embedding indisponível não impede o cadastro
- **WHEN** o modelo de embedding não pode ser carregado ou a inferência falha
- **THEN** a foto é cadastrada mesmo assim, com cor e proporção persistidas e o embedding ausente

### Requirement: Operação 100% offline
O sistema SHALL realizar toda a captura, armazenamento e extração de features das
fotos no dispositivo, sem qualquer acesso à rede.

#### Scenario: Sem conectividade
- **WHEN** o dispositivo está sem internet
- **THEN** adicionar, extrair features e remover fotos funcionam normalmente

