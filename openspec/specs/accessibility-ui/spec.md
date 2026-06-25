# accessibility-ui Specification

## Purpose
Definir os requisitos de acessibilidade do app: tamanhos mínimos de fonte e área de toque, alto contraste, suporte a TalkBack, linguagem simples e escala de fonte configurável pelo usuário.
## Requirements
### Requirement: NavigationBar resistente a fonte grande
O sistema SHALL exibir os labels da NavigationBar sem quebra de linha em
configurações de fonte do sistema de até 1.3x (Android "Fonte grande").

#### Scenario: Label "Configurações" não quebra com fonte grande
- **WHEN** o sistema Android está configurado com font_scale 1.3
- **THEN** o label "Configurações" na NavigationBar exibe em uma única linha, possivelmente
  truncado com reticências, sem ocupar duas linhas

### Requirement: Cards de resumo da tela Hoje com altura consistente
O sistema SHALL manter os 4 cards de resumo (Tomados/Pendentes/Atrasados/Pulados)
com altura uniforme e labels legíveis com font_scale até 1.3x, sem colapsar ou
superpor conteúdo.

#### Scenario: Cards de resumo com fonte grande mantêm proporção
- **WHEN** o sistema Android está configurado com font_scale 1.3
- **THEN** os 4 cards de resumo têm alturas iguais e seus labels são visíveis (máximo 2 linhas)

### Requirement: Rows de configuração resilientes a fonte grande
O sistema SHALL garantir que Rows com Switch + label + descrição na SettingsScreen
exibam o conteúdo corretamente com font_scale até 1.3x, sem desalinhamento ou
truncamento do Switch.

#### Scenario: Row de Auto-captura com fonte grande
- **WHEN** o sistema Android está configurado com font_scale 1.3
- **THEN** o Switch de Auto-captura está visivelmente alinhado ao texto e toque na
  área toda do item aciona o Switch

### Requirement: contentDescription em todos os elementos visuais interativos
O sistema SHALL fornecer `contentDescription` não-nulo e descritivo em todos os
ícones, imagens e botões de ícone de todas as telas.

#### Scenario: Ícones de ação têm descrição
- **WHEN** o TalkBack foca um ícone de ação (ex.: voltar, excluir, câmera)
- **THEN** o elemento é anunciado com a ação que executa

#### Scenario: Fotos de medicamentos têm descrição semântica
- **WHEN** o TalkBack foca uma imagem de medicamento
- **THEN** o elemento é anunciado com nome do medicamento e lado da foto

### Requirement: Escala de fonte dinâmica via AppSettings
O sistema SHALL aplicar a escala de fonte definida em `AppSettings.fontScale` ao
tema, com opções padrão / grande (1.15x) / maior (1.30x).

#### Scenario: Usuário seleciona fonte "Grande"
- **WHEN** o usuário seleciona "Grande" em Configurações > Acessibilidade
- **THEN** todos os textos do app são renderizados com escala 1.15x

#### Scenario: Usuário seleciona fonte "Maior"
- **WHEN** o usuário seleciona "Maior" em Configurações > Acessibilidade
- **THEN** todos os textos do app são renderizados com escala 1.30x

#### Scenario: Configuração de fonte persiste entre sessões
- **WHEN** o usuário fecha e reabre o app após ter selecionado uma escala de fonte
- **THEN** a escala selecionada é aplicada desde a primeira tela renderizada

### Requirement: Tema de alto contraste via AppSettings
O sistema SHALL aplicar paleta de alto contraste quando `AppSettings.highContrast = true`,
com razão de contraste mínima de 4.5:1 (WCAG AA) entre texto e fundo.

#### Scenario: Usuário ativa alto contraste
- **WHEN** o usuário ativa "Alto contraste" em Configurações > Acessibilidade
- **THEN** o tema muda para a paleta de alto contraste em todas as telas imediatamente

#### Scenario: Alto contraste persiste entre sessões
- **WHEN** o app é reiniciado com alto contraste ativado
- **THEN** a paleta de alto contraste é aplicada desde a primeira renderização

