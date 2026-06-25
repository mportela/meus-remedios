## ADDED Requirements

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
