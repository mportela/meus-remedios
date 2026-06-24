## MODIFIED Requirements

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

## ADDED Requirements

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
