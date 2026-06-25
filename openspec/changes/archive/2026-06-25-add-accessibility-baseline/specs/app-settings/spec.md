## ADDED Requirements

### Requirement: Controles de acessibilidade na tela de Configurações
O sistema SHALL exibir na tela de Configurações uma seção "Acessibilidade" com:
seletor de tamanho de fonte (padrão / grande / maior) e toggle de alto contraste.
Ao alterar qualquer valor, a mudança SHALL ser aplicada imediatamente e persistida.

#### Scenario: Seletor de fonte exibe opção atual
- **WHEN** o usuário abre a tela de Configurações
- **THEN** a seção "Acessibilidade" exibe o tamanho de fonte atual selecionado

#### Scenario: Alterar tamanho de fonte atualiza o tema imediatamente
- **WHEN** o usuário seleciona um novo tamanho de fonte
- **THEN** os textos do app refletem a nova escala sem necessidade de reiniciar

#### Scenario: Toggle de alto contraste reflete estado atual
- **WHEN** o usuário abre a tela de Configurações
- **THEN** o toggle de "Alto contraste" reflete o valor atual de `AppSettings.highContrast`
