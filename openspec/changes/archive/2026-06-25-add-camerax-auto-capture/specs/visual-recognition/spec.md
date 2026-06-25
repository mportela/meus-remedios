## MODIFIED Requirements

### Requirement: Auto-captura controlada por configuração
O sistema DEVE permitir que o usuário inicie o reconhecimento de um comprimido por câmera.
Quando `autoCapture` estiver habilitado nas configurações, o sistema DEVE capturar
automaticamente quando detectar foco estável e score TFLite ≥ THRESHOLD_CONFIDENT no frame
ao vivo. Quando `autoCapture` estiver desabilitado, o sistema DEVE exibir um botão de
captura manual (comportamento atual preservado).

#### Scenario: Auto-captura com confiança suficiente
- **WHEN** `autoCapture = true` E câmera está com foco estável E score TFLite do frame ≥ THRESHOLD_CONFIDENT
- **THEN** o sistema captura a imagem automaticamente, exibe flash branco e vibração háptica, e inicia a análise

#### Scenario: Auto-captura bloqueada por cooldown
- **WHEN** uma captura automática foi realizada há menos de 2 segundos
- **THEN** o sistema NÃO dispara nova captura automática, mesmo com foco estável e score suficiente

#### Scenario: Score insuficiente no preview
- **WHEN** `autoCapture = true` E câmera está com foco estável E score TFLite do frame < THRESHOLD_CONFIDENT
- **THEN** o sistema NÃO captura automaticamente; botão manual permanece disponível

#### Scenario: Auto-captura desabilitada
- **WHEN** `autoCapture = false`
- **THEN** o sistema usa câmera nativa (Intent) e botão de captura manual; nenhum preview ao vivo é exibido
