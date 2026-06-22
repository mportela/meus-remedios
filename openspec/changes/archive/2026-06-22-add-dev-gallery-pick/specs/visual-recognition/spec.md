# visual-recognition (delta)

## MODIFIED Requirements

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
