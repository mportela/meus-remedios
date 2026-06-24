## ADDED Requirements

### Requirement: Ação pós-reconhecimento confiante

Após um resultado confiante, o sistema SHALL oferecer ação de registrar tomada
diretamente na tela Confirmar, sem exigir navegação para a tela Hoje.

#### Scenario: Botão "Tomei" visível após resultado confiante

- **WHEN** o sistema exibe um resultado confiante de reconhecimento
- **THEN** SHALL exibir botão "Tomei" proeminente abaixo do nome do remédio identificado

#### Scenario: Resultado ambíguo ou sem correspondência não exibe "Tomei"

- **WHEN** o resultado do reconhecimento é Ambiguous, NoMatch ou NoPhotosRegistered
- **THEN** o sistema SHALL NOT exibir botão "Tomei"
  (o usuário não sabe qual remédio é, portanto não pode registrar a tomada)
