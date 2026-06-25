# app-settings Specification

## Purpose
Especificar a tela de Configurações (4ª tab), incluindo retenção de histórico configurável, auto-captura da câmera, lembretes globais, antecedência de aviso, job de limpeza periódica (WorkManager), seção "Sobre" e controles de acessibilidade (fonte e alto contraste).
## Requirements
### Requirement: Tela de configurações acessível
O sistema SHALL expor uma tela de Configurações como 4º item da NavigationBar inferior,
acessível a partir de qualquer tab de nível superior.

#### Scenario: Navegação para Configurações
- **WHEN** o usuário toca no item "Configurações" da barra de navegação
- **THEN** a tela de Configurações é exibida com todas as preferências atuais carregadas

### Requirement: Retenção de histórico configurável
O sistema SHALL permitir que o usuário configure quantos dias de histórico de tomadas
serão retidos (opções: 30, 60, 90, 180, 365 dias; default 90).

#### Scenario: Alterar retenção e persistir
- **WHEN** o usuário seleciona um valor de retenção diferente
- **THEN** o valor é salvo imediatamente e persiste entre sessões

#### Scenario: Limpeza automática não apaga cadastros
- **WHEN** o job de limpeza periódica executa
- **THEN** somente `IntakeLog` mais antigos que o período configurado são removidos
- **AND** medicamentos e horários cadastrados permanecem intactos

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

### Requirement: Lembretes globais e antecedência configuráveis
O sistema SHALL permitir ligar/desligar lembretes globais e ajustar a antecedência do
aviso (opções: 0, 1, 5, 10, 15, 30 minutos; default 1 min).

#### Scenario: Desligar lembretes globais
- **WHEN** o usuário desativa "Lembretes"
- **THEN** todos os alarmes existentes são cancelados e nenhum novo é agendado

#### Scenario: Alterar antecedência com lembretes ativos
- **WHEN** o usuário altera a antecedência (e lembretes globais estão ativos)
- **THEN** todos os alarmes do dia são reagendados com o novo offset

### Requirement: Job de limpeza periódica de histórico
O sistema SHALL executar um job periódico (diário, via WorkManager) que remove
`IntakeLog` com `date` anterior ao corte definido por `historyRetentionDays`.

#### Scenario: Job executado diariamente
- **WHEN** o WorkManager aciona o `RetentionWorker`
- **THEN** `IntakeLog` com `date < hoje - historyRetentionDays` são deletados do banco

#### Scenario: Job agendado no startup
- **WHEN** o app é iniciado
- **THEN** o `RetentionWorker` é agendado (ou reusado se já existe) como trabalho periódico único

### Requirement: Tela "Sobre"
O sistema SHALL exibir na tela de Configurações uma seção "Sobre" com: versão do app,
disclaimer que o app é auxílio de confirmação visual e não substitui médico, e confirmação
de que o app funciona 100% offline sem envio de dados.

#### Scenario: Exibição do disclaimer
- **WHEN** o usuário rola até a seção "Sobre" na tela de Configurações
- **THEN** o texto de disclaimer e a confirmação offline são visíveis

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

