## MODIFIED Requirements

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
