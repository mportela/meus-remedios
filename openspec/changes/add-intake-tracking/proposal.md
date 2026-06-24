## Why

A tela Hoje exibe as doses do dia com status derivado do horário (pendente/atrasado), mas
não permite ao usuário registrar que tomou ou pulou um remédio — o app não tem memória de
tomadas reais. Sem esse registro, o relatório diário é inútil como controle efetivo de
aderência, que é o caso de uso primário para o público idoso.

## What Changes

- Novo use case `MarkIntakeTakenUseCase` e `MarkIntakeSkippedUseCase` que gravam um
  `IntakeLog` com status `TAKEN` ou `SKIPPED` para a dose selecionada.
- Tela Hoje passa a exibir botão/ação de marcar tomada em cada dose pendente/atrasada,
  e ação de desmarcar em doses já marcadas (toggle idempotente).
- Dose com `IntakeLog.TAKEN` ou `SKIPPED` exibe estado visual distinto (ícone/cor);
  o resumo de contagens no topo do relatório reflete os valores reais persistidos.
- Remoção limpa de `IntakeLog` ao desmarcar (toggle), sem soft-delete.
- **Tela Confirmar (reconhecimento):** ao identificar um remédio com confiança, exibe botão
  "Tomei" para registrar a tomada sem sair da tela. Lógica de matching:
  - 1 dose pendente hoje → marca diretamente com o `scheduleTimeId` correspondente
  - Múltiplas doses pendentes → abre seletor de horário (bottom sheet)
  - Nenhuma dose hoje → grava `IntakeLog` ad-hoc com `scheduleTimeId = null` (aparece no
    histórico do medicamento mas não altera o status na tela Hoje)

## Capabilities

### New Capabilities

- `intake-tracking`: ações de registrar e desmarcar tomadas de doses; persistência e
  recuperação de `IntakeLog` por medicamento e data; idempotência do toggle.

### Modified Capabilities

- `reporting`: tela Hoje precisa expor a ação de marcar/desmarcar em cada dose e
  refletir o estado TAKEN/SKIPPED em tempo real via Flow reativo.
- `visual-recognition`: tela Confirmar exibe botão "Tomei" após resultado confiante,
  com fluxo de seleção de horário quando o medicamento tem múltiplas doses no dia.

## Impact

- **Novos arquivos:** `MarkIntakeTakenUseCase`, `MarkIntakeSkippedUseCase`,
  `GetTodayDosesForMedicationUseCase` (lookup de doses do dia para o matching),
  testes de use cases.
- **Modificados:** `IntakeLogDao` (query por medicamento+horário+data),
  `IntakeLogRepository`, UI da tela Hoje (`DoseCard`), UI da tela Confirmar
  (`ConfidentResult` + `RecognitionViewModel`).
- **Room:** sem nova migração — schema v2 já suporta `scheduleTimeId = null`.
- **Dependências:** nenhuma nova.
- **Offline:** mantido.
