## Why

O app é destinado a idosos que frequentemente usam a opção "Fonte grande" do Android
(font_scale 1.3x). Testes no emulador com essa configuração revelaram quebras de layout
reais: label "Configurações" se parte em 2 linhas na NavigationBar; cards de resumo
na tela Hoje ficam espremidos com labels quebrando; a Row de Auto-captura nas
Configurações fica desalinhada. Além disso, faltam `contentDescription` em ícones
e suporte a TalkBack no resultado do reconhecimento. Esta change corrige os problemas
documentados e adiciona as bases de acessibilidade previstas no TECH-4.

## What Changes

### Correções de layout com fonte grande (confirmadas no emulador)
- **NavigationBar**: substituir o label text literal por string com `maxLines = 1`
  e `overflow = TextOverflow.Ellipsis` — impede quebra de "Configurações" em 2 linhas
- **TodayScreen — SummaryCard**: adicionar `minHeight` nos cards de resumo e usar
  `maxLines = 2` no label, garantindo que os 4 cards mantenham altura consistente
  sem colapsar com fonte grande
- **SettingsScreen — Row de Auto-captura**: mudar layout da Row para `Column` quando
  o texto da descrição precisar de mais espaço; garantir alinhamento vertical correto
  do Switch com o label
- **MedicationFormScreen — botões OutlinedButton**: garantir `fillMaxWidth` nos botões
  "Adicionar horário" e "Adicionar foto" para consistência com fonte grande

### TalkBack e semântica
- Adicionar `contentDescription` nos ~12 ícones/imagens sem descrição identificados
- Anunciar resultado do reconhecimento via `semantics { liveRegion = Polite }`

### Tema dinâmico (RF-6.4)
- Conectar `AppSettings.fontScale` ao tema via `LocalDensity`
- Conectar `AppSettings.highContrast` com paleta alternativa
- Expor controles na `SettingsScreen` (seção "Acessibilidade")

## Capabilities

### New Capabilities
*(nenhuma — acessibilidade é transversal)*

### Modified Capabilities
- `accessibility-ui`: criar spec consolidada com requisitos de layout responsivo a
  fonte grande e suporte a TalkBack
- `app-settings`: adicionar controles de `fontScale` e `highContrast` (RF-6.4)
- `visual-recognition`: anúncio do resultado via TalkBack

## Impact

- `ui/navigation/MeusRemediosNavHost.kt`: labels da NavBar com maxLines/ellipsis
- `ui/today/TodayScreen.kt`: SummaryCard com minHeight e label com maxLines
- `ui/settings/SettingsScreen.kt`: Row de Auto-captura refatorada + nova seção
  Acessibilidade (fontScale/highContrast)
- `ui/theme/Theme.kt` + `Color.kt`: paleta highContrast + suporte a fontScale
- `ui/medications/form/MedicationFormScreen.kt`: botões com fillMaxWidth
- `ui/recognition/RecognitionScreen.kt`: semantics liveRegion no resultado
- Todas as telas com ícones sem contentDescription
- Sem novas dependências
