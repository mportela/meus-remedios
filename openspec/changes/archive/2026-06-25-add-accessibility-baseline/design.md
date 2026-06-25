## Context

Testes no emulador com `adb shell settings put system font_scale 1.30` revelaram
problemas concretos de layout em todas as telas principais. O diagnóstico foi feito
com screenshots de cada tela com fonte grande antes de escrever qualquer código.

Problemas confirmados:
1. **NavBar**: "Configurações" quebra em "Configuraç / ões" (2 linhas)
2. **TodayScreen SummaryCard**: labels "Tomados"/"Pendentes"/"Atrasados"/"Pulados"
   quebram em 2 linhas e os 4 cards ficam espremidos
3. **SettingsScreen Row de Auto-captura**: descrição em 2 linhas com Switch desalinhado
   (falta `verticalAlignment = CenterVertically` na Row externa)
4. **MedicationFormScreen botões**: `OutlinedButton` de horário/foto sem `fillMaxWidth`7. **TodayScreen `DayChip`**: chips de dia da semana usam `weight(1f)` sem largura mínima;
   com fonte 1.3x "dom" e o número "28" quebram para linhas separadas dentro do chip
8. **TodayScreen `DoseCard`**: nome do medicamento sem `maxLines`; com fonte grande
   quebra no meio da palavra (ex.: "Caltr / ate")5. **MedicationFormScreen Row "Avise-me nos horários"**: Switch sobrepõe o texto com
   fonte grande (`Arrangement.SpaceBetween` sem `weight(1f)` no Text)
6. **MedicationFormScreen — `TimePicker` (relógio analógico)**: os números do mostrador
   têm posições fixas em dp mas o texto escala com sp; com font_scale 1.3x todos os
   algarismos se sobrepõem tornando o seletor inutilizável

Além disso, `AppSettings` tem `fontScale` e `highContrast` desde a F1, sem UI.

## Goals / Non-Goals

**Goals:**
- Corrigir os 3 problemas de layout documentados no emulador com font_scale 1.3.
- Conectar `fontScale` e `highContrast` ao tema (`MeusRemediosTheme`).
- Expor controles na `SettingsScreen` (seção "Acessibilidade").
- Adicionar `contentDescription` nos ~12 ícones/imagens sem descrição.
- Anunciar resultado do reconhecimento via `semantics { liveRegion = Polite }`.

**Non-Goals:**
- Suporte a `darkTheme` explícito (segue o sistema).
- Auditoria WCAG AA completa (seria F9/QA).
- `reduceMotion`. Testes instrumentados de TalkBack.

## Decisions

### D1 — NavBar: maxLines=1 + TextOverflow.Ellipsis no label
O `NavigationBarItem` do Material 3 por padrão não limita o label a 1 linha. Passar
explicitamente `maxLines = 1` e `overflow = TextOverflow.Ellipsis` no `Text` do label.
Alternativa descartada: encurtar "Configurações" para "Config" — perde clareza para idosos.

### D2 — SummaryCard: heightIn(min) + maxLines=2 no label
Adicionar `Modifier.heightIn(min = 88.dp)` no Card e `maxLines = 2` no Text do label.
Garante altura consistente nos 4 cards e nenhum label truncado até 2 linhas com 1.3x.

### D3 — SettingsScreen rows: verticalAlignment = CenterVertically
A Row de Auto-captura tem `Column(weight(1f)) { label; descrição }` + `Switch`. O problema
é ausência de `verticalAlignment = Alignment.CenterVertically` na Row. Correção mínima.
Mesmo padrão aplicado a todas as Rows de configuração com Switch.

### D4 — fontScale via LocalDensity (sem sobrescrever sistema quando null)
```kotlin
if (fontScale != null) {
    val adjusted = Density(LocalDensity.current.density, fontScale)
    CompositionLocalProvider(LocalDensity provides adjusted) { MaterialTheme(...) }
} else {
    MaterialTheme(...) // respeita o sistema — sem duplicação para quem já usa fonte grande
}
```

### D5 — Alto contraste: paleta estática HighContrastColors
`HighContrastLightColors`: primary `#0D47A1`, background `#FFFFFF`, onBackground `#000000`.
Selecionada em `MeusRemediosTheme(highContrast = true)`.

### D6 — MainActivity coleta settings e injeta no tema
`MainActivity` coleta `settingsRepository.observe()` via `collectAsStateWithLifecycle()`
e passa `fontScale` e `highContrast` ao `MeusRemediosTheme`. Sem acoplamento no tema.

### D7 — contentDescription via stringResource (prefixo cd_)
Todas as novas `contentDescription` usam `stringResource(R.string.cd_*)` para
internacionalização futura e auditoria pelo lint Android.

### D8 — TimePicker substituído por TimeInput
`TimePicker` (mostrador analógico) posiciona os algarismos em coordenadas dp fixas mas
renderiza o texto em sp; com font_scale 1.3x os números se sobrepõem completamente.
Solução: substituir por `TimeInput` (dois campos numéricos HH e MM) em
`MedicationFormScreen`. Além de corrigir o bug, `TimeInput` é mais adequado para o
público idoso — entrada direta via teclado numérico, sem precisar arrastar o ponteiro
o clock dial.
Alternativa descartada: manter `TimePicker` e reduzir `fontScale` localmente via
`CompositionLocalProvider` — hack frágil que mascararia o problema em vez de corrigi-lo.

## Risks / Trade-offs

- **Alto contraste parcial**: DropdownMenu e outros componentes Material 3 usam tokens
  internos não cobertos → aceitar, documentar como dívida residual.
- **heightIn no SummaryCard**: em dispositivos muito pequenos os 4 cards podem ficar
  altos → usar apenas `min`, nunca `max`.

## Migration Plan

Sem migração de schema Room. Colunas `font_scale` e `high_contrast` já existem.
Default `null`/`null` → comportamento atual preservado.
