# Proposal: add-reporting-and-browsing

## Why

A F1 (dados) e a F2 (cadastro + fotos) estão concluídas. O usuário já consegue
cadastrar remédios com horários e fotos, mas ainda não tem como **consultar** o que
precisa tomar nem **navegar** pelos detalhes de um remédio. Esta change implementa a
fase **F3 — Consulta e Relatórios** (PRD-2), entregando o lado de **leitura/derivação**:

- Uma tela **Hoje** com o relatório do dia (doses esperadas, agrupadas por período do
  dia) e uma timeline navegável por dia da semana.
- Uma tela de **Detalhe do remédio** com dados, fotos, horários e histórico recente.
- Navegação por **abas** (Hoje · Meus remédios), tornando o app utilizável de ponta a
  ponta para o público idoso.

A marcação real de tomadas (gravar `IntakeLog`) é da F5; aqui as doses sem registro são
**derivadas** dos horários cadastrados (pendente / atrasado conforme o horário).

## What Changes

- **Camada de dados (medication-catalog):** adiciona `ScheduleRepository.observeAll()`
  (+ query no DAO) para o relatório reagir a mudanças de horários de todos os remédios.
- **DI:** novo `TimeModule` provendo um `java.time.Clock` injetável (testes usam clock fixo).
- **Domínio:** novos modelos `DayPeriod`, `DoseStatus`, `ScheduledDose`, `DailyReport`,
  `MedicationDetail`.
- **Use cases:** `ObserveDailyReportUseCase` (deriva doses do dia + status) e
  `ObserveMedicationDetailUseCase` (remédio + horários + fotos + histórico na retenção).
- **UI:** telas `today/` (relatório + timeline semanal) e `medications/detail/`; navegação
  por abas com `NavigationBar`; a lista passa a abrir o **detalhe** (e o detalhe leva à edição).
- **Strings/acessibilidade:** novos rótulos pt-BR, semântica de cabeçalho, status legível.

## Capabilities

- **reporting** (novo): relatório do dia, timeline e detalhe de consulta.
- **medication-catalog** (modificado): adição de `observeAll()` de horários.

## Impact

- Sem novas dependências; 100% offline mantido (nenhuma chamada de rede).
- Não altera schema do banco (apenas leitura/observação adicional).
- A navegação raiz muda de tela única para abas; fluxo lista→detalhe→edição.
- Doses do dia sem `IntakeLog` aparecem como pendentes/atrasadas (registro real é F5).

## Documentation

- Atualizar `CHANGELOG.md` (seção "Não lançado").
