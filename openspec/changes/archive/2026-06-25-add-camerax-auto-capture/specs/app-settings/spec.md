## MODIFIED Requirements

### Requirement: Auto-captura configurável
O sistema DEVE persistir a preferência `autoCapture` do usuário e aplicá-la ao comportamento
da câmera na tela de reconhecimento. Quando `autoCapture = true`, a câmera DEVE operar em
modo de preview ao vivo com captura automática por confiança. Quando `autoCapture = false`,
a câmera DEVE operar em modo manual (Intent nativo), preservando o comportamento anterior.

#### Scenario: Configuração habilitada reflete na câmera
- **WHEN** o usuário habilita auto-captura nas configurações
- **THEN** a tela de reconhecimento exibe preview ao vivo (CameraX) e captura automaticamente ao detectar foco + confiança

#### Scenario: Configuração desabilitada preserva comportamento manual
- **WHEN** o usuário desabilita auto-captura nas configurações
- **THEN** a tela de reconhecimento exibe o botão de captura manual e usa câmera nativa (Intent)
