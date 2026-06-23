## MODIFIED Requirements

### Requirement: Extração e persistência de features
O sistema SHALL extrair, ao adicionar uma foto, as features de reconhecimento — cor dominante
em espaço Lab, proporção (aspect ratio), **embedding** de imagem calculado por um modelo
on-device e **imprint (texto gravado)** lido por OCR on-device — e persisti-las junto da
foto. A extração SHALL ocorrer 100% no dispositivo, sem acesso à rede. Caso o modelo de
embedding ou o OCR não estejam disponíveis ou falhem, o sistema SHALL persistir as demais
features e deixar o componente faltante ausente, sem impedir o cadastro.

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
