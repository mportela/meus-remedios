## 1. Correções de layout com fonte grande (bugs confirmados no emulador)

- [x] 1.1 Em `MeusRemediosNavHost.kt`, no `label` do `NavigationBarItem`, trocar
         `Text(stringResource(dest.labelRes))` por
         `Text(stringResource(dest.labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis)`
         para impedir que "Configurações" quebre em 2 linhas com fonte grande
- [x] 1.2 Em `TodayScreen.kt`, na função `SummaryCard`, adicionar
         `Modifier.heightIn(min = 88.dp)` no `Card` e `maxLines = 2` no `Text` do label,
         para que os 4 cards mantenham altura consistente com fonte 1.3x
- [x] 1.3 Em `SettingsScreen.kt`, em todas as `Row` que contêm `Column(weight(1f))` +
         `Switch`, adicionar `verticalAlignment = Alignment.CenterVertically` na Row
         para corrigir o desalinhamento do Switch de Auto-captura com fonte grande
- [x] 1.3b Em `MedicationFormScreen.kt`, na Row de "Avise-me nos horários" + Switch
         (linha ~208), adicionar `Modifier.weight(1f)` no `Text` e
         `Modifier.padding(start = 8.dp)` no `Switch` para evitar que o texto
         com fonte grande sobreponha o Toggle (`Arrangement.SpaceBetween` sem
         weight não protege quando o texto ocupa toda a largura)
- [x] 1.4 Em `MedicationFormScreen.kt`, adicionar `modifier = Modifier.fillMaxWidth()`
         nos `OutlinedButton` de "Adicionar horário" e "Adicionar foto" para
         consistência visual com fonte grande
- [x] 1.5 Em `MedicationFormScreen.kt`, nos dois `SegmentedButton` do seletor
         "Contínuo / Por período", adicionar `maxLines = 1` e
         `overflow = TextOverflow.Ellipsis` no `Text` interno para impedir que
         "Por período" quebre em 2 linhas com fonte grande (o `SegmentedButton`
         tem altura fixa e não expande com texto multilinha)
- [x] 1.6 Em `RecognitionScreen.kt`, na função `BigConfirmButton`, adicionar
         `maxLines = 1` e `overflow = TextOverflow.Ellipsis` no `Text` interno
         (que usa `headlineSmall` + ícone 40dp) para evitar clipping dentro da
         altura fixa `Modifier.height(120.dp)` com fonte grande
- [x] 1.7 Em `MedicationFormScreen.kt`, substituir `TimePicker(state = state)` por
         `TimeInput(state = state)` no `AlertDialog` de horário (linha ~464);
         `TimeInput` usa campos numéricos HH/MM que não têm o problema de sobreposição
         do mostrador analógico com font_scale 1.3x, e é mais acessível para idosos
         (entrada direta pelo teclado, sem arrastar ponteiro)
- [x] 1.8 Em `TodayScreen.kt`, na função `DayChip`, adicionar `maxLines = 1` e
         `overflow = TextOverflow.Ellipsis` nos dois `Text` (weekdayLabel e dayOfMonth)
         para evitar que "dom" e "28" quebrem em linhas separadas com fonte grande;
         os chips usam `Modifier.weight(1f)` sem largura mínima, ficando estreitos
- [x] 1.9 Em `TodayScreen.kt`, na função `DoseCard`, adicionar `maxLines = 2` e
         `overflow = TextOverflow.Ellipsis` no `Text` do `medicationName` para
         evitar quebra de palavra no meio (ex.: "Caltr / ate") com fonte grande

## 2. Tema — fontScale e highContrast

- [x] 2.1 Em `Color.kt`, adicionar paleta `HighContrastLightColors`
         (primary `#0D47A1`, onPrimary `#FFFFFF`, background `#FFFFFF`, onBackground `#000000`,
         surface `#FFFFFF`, onSurface `#000000`, error `#BA1A1A`)
- [x] 2.2 Atualizar `MeusRemediosTheme` em `Theme.kt` para aceitar `fontScale: Float? = null`
         e `highContrast: Boolean = false`; quando `highContrast = true` usar
         `HighContrastLightColors`; quando `fontScale != null` envolver o conteúdo com
         `CompositionLocalProvider(LocalDensity provides Density(density, fontScale))`
- [x] 2.3 Em `MainActivity.kt`, injetar `SettingsRepository` via Hilt, coletar
         `settingsRepository.observe()` e passar `fontScale` e `highContrast` ao
         `MeusRemediosTheme`

## 3. Configurações — seção Acessibilidade

- [x] 3.1 Adicionar ao `res/values/strings.xml`:
         `settings_section_accessibility` ("Acessibilidade"),
         `settings_font_size_label` ("Tamanho de fonte"),
         `settings_font_size_default` ("Padrão (sistema)"),
         `settings_font_size_large` ("Grande (1.15×)"),
         `settings_font_size_larger` ("Maior (1.30×)"),
         `settings_high_contrast_label` ("Alto contraste"),
         `settings_high_contrast_desc` ("Aumenta o contraste de cores para melhor legibilidade")
- [x] 3.2 Em `SettingsScreen.kt`, adicionar seção "Acessibilidade" (após seção Lembretes)
         com `DropdownMenuBox` para `fontScale` (opções: null/"Padrão", 1.15f/"Grande",
         1.30f/"Maior") e `Row` com `Switch` para `highContrast`; ao mudar, chamar
         `viewModel.save(settings.copy(...))` imediatamente

## 4. contentDescription — ícones e imagens sem descrição

- [x] 4.1 Adicionar ao `res/values/strings.xml` strings `cd_*`:
         `cd_action_capture` ("Capturar foto"),
         `cd_action_gallery` ("Escolher da galeria"),
         `cd_medication_photo_front` ("Foto da frente de %1\$s"),
         `cd_medication_photo_back` ("Foto do verso de %1\$s"),
         `cd_action_add_schedule` ("Adicionar horário"),
         `cd_action_remove_schedule` ("Remover horário"),
         `cd_action_add_photo` ("Adicionar foto"),
         `cd_action_remove_photo` ("Remover foto")
- [x] 4.2 Em `RecognitionScreen.kt`: adicionar `contentDescription` nos dois `Icon()`
         sem descrição (~linha 178: ícone de câmera; ~linha 242: ícone de galeria)
- [x] 4.3 Em `MedicationFormScreen.kt`: corrigir `contentDescription` dos `Icon()`
         nas linhas ~122, ~131, ~419, ~642 usando as strings `cd_*` criadas
- [x] 4.4 Em `MedicationDetailScreen.kt`: adicionar `contentDescription` no `Icon()`
         ~linha 84 (botão voltar) e no `Image()` ~linha 203 (foto do medicamento)
- [x] 4.5 Em `MedicationListScreen.kt`: confirmar `contentDescription` no `Icon()` ~linha 82

## 5. Semântica de reconhecimento (TalkBack)

- [x] 5.1 Adicionar ao `res/values/strings.xml`:
         `cd_recognition_confident` ("Reconhecido: %1\$s"),
         `cd_recognition_ambiguous` ("Resultado incerto. Fotografe o verso do comprimido."),
         `cd_recognition_no_match` ("Comprimido não reconhecido. Tente uma nova foto.")
- [x] 5.2 Em `RecognitionScreen.kt`, no composable que exibe o resultado, adicionar
         `Modifier.semantics { liveRegion = LiveRegionMode.Polite; contentDescription = desc }`
         onde `desc` é montado conforme o estado (confiante/ambíguo/sem match)

## 6. Testes

- [x] 6.1 Rodar `./gradlew :app:testDebugUnitTest` e confirmar 0 falhas

## 7. Documentação

- [x] 7.1 Atualizar `CHANGELOG.md` com entrada F8 em "Não lançado"
- [x] 7.2 Marcar F8 como feita em `docs/openspec-plan.md`
- [x] 7.3 Executar `openspec archive --change add-accessibility-baseline`
