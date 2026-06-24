## Context

`IntakeLog` e `IntakeStatus` (PENDING/TAKEN/SKIPPED) já existem no schema Room v2.
`ObserveDailyReportUseCase` já cruza horários com logs e resolve o status correto via
`resolveStatus()`. O que falta é a **camada de escrita**: use cases que gravam/removem
logs e a UI que expõe as ações ao usuário. Nenhuma migração de schema é necessária.

`IntakeLogDao` tem insert/update/delete, mas falta uma query para buscar o log
correspondente a uma dose específica (medicamento + horário + data), necessária para
decidir o toggle (existe? → update/delete; não existe? → insert).

## Goals / Non-Goals

**Goals:**
- Gravar TAKEN ou SKIPPED para uma dose específica do dia.
- Toggle idempotente: tocar novamente no mesmo estado desfaz o registro (PENDING).
- Transição entre estados: TAKEN ↔ SKIPPED atualizando o registro existente.
- Relatório do dia atualiza em tempo real via Flow reativo (já funciona — sem mudança).
- UI acessível: ação grande, legível, responde ao toque único.

**Non-Goals:**
- Notificações ou lembretes (F6).
- Edição do `takenAt` (hora exata da tomada — sempre `Instant.now()`).
- Exportação ou compartilhamento do histórico.

## Decisions

### D1 — Dois use cases separados: `MarkIntakeTakenUseCase` e `MarkIntakeSkippedUseCase`

**Decisão:** dois use cases com intenção explícita em vez de um `ToggleIntakeUseCase`
polimórfico.

**Rationale:** lógica de toggle (`se existe com mesmo status → delete; diferente → update;
ausente → insert`) é idêntica nos dois — extraída para função privada compartilhada num
futuro refactor se necessário. Mas no ponto de chamada (ViewModel/UI) a intenção é clara:
o usuário tocou em "tomei" ou em "pulei". Dois use cases distintos tornam o ViewModel
legível e os testes diretos.

**Alternativa descartada:** `ToggleIntakeUseCase(status: IntakeStatus)` — economiza uma
classe mas obscurece a intenção no ViewModel e nos testes.

### D2 — Query `getByMedicationScheduleDate` no DAO

**Decisão:** adicionar
`suspend fun getByMedicationScheduleDate(medicationId, scheduleTimeId, date): IntakeLogEntity?`
ao `IntakeLogDao` para buscar o log existente de forma síncrona antes do toggle.

**Rationale:** o toggle precisa saber se já existe um log e qual o status atual. A query
é pontual (LIMIT 1 implícito por unicidade de negócio), não reativa — não precisa de Flow.

**Alternativa descartada:** ler de `observeByDate` já coletado no ViewModel — acoplamento
entre ViewModel e lógica de negócio; e o use case ficaria dependente de estado externo.

### D3 — Semântica do toggle

**Decisão:** mesma intenção que o estado atual → **delete** (volta a PENDING); intenção
diferente → **update**; nenhum log existente → **insert**.

**Rationale:** "desmarcar" é mais natural que "marcar como PENDING" — o registro
simplesmente não existe para aquela dose. Isso também simplifica `observeByDate`: só
existem logs com TAKEN ou SKIPPED, nunca PENDING.

### D4 — UI: checkbox para TAKEN, botão "Pular" secundário

**Decisão:** `DoseCard` exibe um checkbox grande (ação primária = TAKEN) e um botão
textual "Pular" (ação secundária = SKIPPED). Dose SKIPPED exibe ícone de "bloqueado"
e o botão "Pular" se converte em "Desfazer".

**Rationale:** público idoso prefere ações óbvias e separadas a gestos (long-press/swipe).
Checkbox para "tomei" é padrão universal reconhecível. "Pular" é exceção, deve ser menos
proeminente mas igualmente acessível.

### D5 — Fluxo "Tomei" na tela Confirmar após reconhecimento confiante

**Decisão:** ao exibir `ConfidentResult`, mostrar botão "Tomei" abaixo do nome do
remédio. O `RecognitionViewModel` injeta `MarkIntakeTakenUseCase` e
`GetTodayDosesForMedicationUseCase`. Ao tocar "Tomei":

1. Busca as doses pendentes/atrasadas de hoje para o `medicationId` reconhecido.
2. **0 doses** → cria `IntakeLog` ad-hoc (`scheduleTimeId = null`, `date = today`).
3. **1 dose** → chama `MarkIntakeTakenUseCase` diretamente com aquela dose.
4. **N > 1 doses** → atualiza `RecognitionUiState` com a lista; UI exibe bottom sheet
   para o usuário escolher qual horário marcar; confirmação chama o use case.

Após sucesso, exibe feedback visual (snackbar ou troca do botão por "✓ Registrado").

**Rationale:** o caso mais comum é 1 dose por remédio por dia — cobertura imediata
sem friction. Ad-hoc para zero doses garante que o usuário não fique sem forma de
registrar quando toma fora do horário. Bottom sheet apenas quando necessário (N > 1).

**Alternativa descartada:** navegar para a tela Hoje após reconhecimento — perde o
contexto, o usuário tem o comprimido na mão e quer registrar ali mesmo.

**Alternativa descartada:** não registrar se não houver dose hoje — omite o registro
de tomadas ad-hoc (ex.: dose extra prescrita pelo médico), reduzindo utilidade do histórico.

## Risks / Trade-offs

- **[Risco] Dose sem `scheduleTimeId`**: atualmente toda dose deriva de um `ScheduleTime`,
  então `scheduleTimeId` nunca é nulo nas doses exibidas. Se isso mudar no futuro, o use
  case lança `IllegalArgumentException` com mensagem clara.
- **[Trade-off] `takenAt = Instant.now()`**: não permite registrar retroativamente uma
  hora diferente. Aceitável para o escopo atual — o campo existe no modelo para uso futuro.
