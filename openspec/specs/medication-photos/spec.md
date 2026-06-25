# medication-photos Specification

## Purpose
Especificar como fotos de comprimidos são capturadas (câmera/galeria), armazenadas em diretório privado e vinculadas a um medicamento, viabilizando o reconhecimento visual.
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
em espaço Lab, proporção (aspect ratio), **embedding** de imagem calculado por um modelo
on-device e **imprint (texto gravado)** lido por OCR on-device — e persisti-las junto da
foto. A extração SHALL ocorrer 100% no dispositivo, sem acesso à rede. Caso o modelo de
embedding ou o OCR não estejam disponíveis ou falhem, o sistema SHALL persistir as demais
features e deixar o componente faltante ausente, sem impedir o cadastro.

Para fotos cadastradas antes da disponibilidade do embedding ou OCR, o sistema SHALL
executar migração automática de features ausentes (ver Requirement abaixo).

#### Scenario: Features persistidas no cadastro
- **WHEN** uma foto é adicionada a um medicamento
- **THEN** sua cor dominante em Lab, sua proporção, seu embedding e seu imprint (quando
  legível) são calculados e persistidos

#### Scenario: Conversão de cor determinística
- **WHEN** a cor de um pixel sRGB conhecido é convertida para Lab
- **THEN** o resultado é determinístico e reproduzível entre execuções

#### Scenario: Embedding indisponível não impede o cadastro
- **WHEN** o modelo de embedding não pode ser carregado ou a inferência falha
- **THEN** a foto é cadastrada mesmo assim, com cor e proporção persistidas e o embedding ausente

#### Scenario: Imprint indisponível não impede o cadastro
- **WHEN** a foto não possui texto gravado legível ou o OCR falha
- **THEN** a foto é cadastrada mesmo assim, com as demais features persistidas e o imprint ausente

### Requirement: Operação 100% offline
O sistema SHALL realizar toda a captura, armazenamento e extração de features das
fotos no dispositivo, sem qualquer acesso à rede.

#### Scenario: Sem conectividade
- **WHEN** o dispositivo está sem internet
- **THEN** adicionar, extrair features e remover fotos funcionam normalmente

### Requirement: Migração automática de features ausentes
O sistema SHALL detectar, ao iniciar, fotos cadastradas sem embedding e reprocessá-las em
background para preencher embedding e imprint faltantes, sem exigir ação do usuário e sem
bloquear a interface. A migração SHALL ser idempotente: fotos com embedding já preenchido
são puladas. Falhas individuais SHALL ser isoladas — um erro em uma foto não impede a
migração das demais.

#### Scenario: Migração silenciosa ao iniciar com fotos antigas
- **WHEN** o app inicia e existem fotos cadastradas com `embedding` nulo
- **THEN** o sistema reprocessa cada uma em background (IO thread), calcula embedding e
  imprint e persiste os valores atualizados, sem exibir progresso ao usuário

#### Scenario: Idempotência — fotos já migradas são puladas
- **WHEN** o app inicia e todas as fotos já possuem embedding
- **THEN** nenhuma reprocessamento ocorre e o startup não é afetado

#### Scenario: Falha isolada não bloqueia demais fotos
- **WHEN** a migração de uma foto falha (arquivo ausente, erro de modelo)
- **THEN** o erro é registrado em log e a migração continua para as fotos seguintes;
  a foto com falha permanece sem embedding e será retentada no próximo startup

#### Scenario: Migração não bloqueia UI
- **WHEN** a migração está em andamento ao abrir o app
- **THEN** todas as telas do app respondem normalmente; a migração ocorre em IO thread

### Requirement: Verificação de colisão visual ao salvar fotos
O sistema SHALL, ao salvar um medicamento com fotos pendentes que possuam embedding
extraído, comparar as features dessas fotos com todas as fotos já cadastradas de outros
medicamentos usando o scorer de reconhecimento. Se algum candidato tiver score ≥
`THRESHOLD_CONFIDENT`, o sistema SHALL exibir um aviso não-bloqueante antes de persistir.

#### Scenario: Foto pendente similar a medicamento existente
- **WHEN** o usuário tenta salvar um medicamento com uma foto pendente cujo embedding é
  similar ao de outro medicamento já cadastrado (score ≥ THRESHOLD_CONFIDENT)
- **THEN** o sistema exibe um aviso com o nome do medicamento mais similar
- **AND** oferece as ações "Salvar assim mesmo" e "Cancelar"
- **AND** NÃO persiste automaticamente — aguarda a decisão do usuário

#### Scenario: Usuário confirma save com aviso de colisão
- **WHEN** o usuário seleciona "Salvar assim mesmo" no aviso de colisão
- **THEN** o medicamento e as fotos são persistidos normalmente

#### Scenario: Usuário cancela ao ver o aviso
- **WHEN** o usuário seleciona "Cancelar" no aviso de colisão
- **THEN** o medicamento NÃO é persistido e o usuário permanece no formulário

#### Scenario: Foto pendente sem embedding não aciona verificação
- **WHEN** o usuário salva um medicamento com foto(s) pendente(s) sem embedding (extração
  falhou ou não foi realizada)
- **THEN** o sistema persiste normalmente sem exibir aviso de colisão

#### Scenario: Nenhuma foto de outros medicamentos cadastrada
- **WHEN** o usuário salva o primeiro medicamento com foto
- **THEN** o sistema persiste normalmente sem aviso (sem candidatos para comparar)

