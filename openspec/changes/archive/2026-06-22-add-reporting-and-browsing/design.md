# Design: add-reporting-and-browsing

## Context

Implementa F3 (PRD-2: `docs/product/prd-2-consulta-relatorios.md`). Reaproveita a
arquitetura MVVM + use cases puros já estabelecida nas changes `add-medication-catalog`
e `add-medication-photos`. Referências: `docs/technical/tech-1-arquitetura.md`,
`docs/technical/tech-2-modelo-dados.md`, `docs/technical/tech-4-acessibilidade.md`.

## Goals / Non-Goals

**Goals**
- Relatório do dia derivado de horários + `IntakeLog`, determinístico e testável.
- Detalhe de remédio agregando dados, fotos, horários e histórico dentro da retenção.
- Navegação por abas acessível (Hoje · Meus remédios), fluxo lista→detalhe→edição.

**Non-Goals**
- Marcar tomadas (gravar `IntakeLog`) — é F5.
- Exportar/compartilhar relatório (PDF/share) — fora do escopo do PRD-2.
- Lembretes/notificações — é F6.

## Decisions

- **Derivação de doses (pura):** para cada remédio ativo na data (intervalo
  `startDate..endDate`) e cada horário cujo `daysOfWeekMask` inclui o dia da semana
  (bit `dayOfWeek.value - 1`, Segunda = bit 0), gera-se uma `ScheduledDose`. O status vem
  do `IntakeLog` correspondente (`medicationId` + `scheduleTimeId` + `date`); na ausência
  de registro TAKEN/SKIPPED, deriva-se por horário: `LATE` se o instante já passou,
  senão `PENDING`. Isso evita depender da F5 para a tela ser útil.
- **Clock injetável:** `ObserveDailyReportUseCase`/`ObserveMedicationDetailUseCase`
  recebem `java.time.Clock` via Hilt (`TimeModule`), permitindo testes com clock fixo.
- **Agrupamento por período (RN-2.3):** `DayPeriod` = MANHÃ `[05:00,12:00)`,
  TARDE `[12:00,18:00)`, NOITE caso contrário; doses ordenadas por horário.
- **Retenção (RN-2.2 / RF-2.6):** histórico do detalhe filtra `IntakeLog` por
  `date >= hoje - settings.historyRetentionDays`.
- **Reatividade:** use cases combinam Flows (`combine`) dos repositórios; adiciona-se
  `ScheduleRepository.observeAll()` para o relatório reagir a qualquer horário.
- **Navegação:** `NavigationBar` com destinos top-level (Hoje, Meus remédios); detalhe e
  formulário são telas empilhadas sem a barra. Lista abre o detalhe; detalhe abre edição.

## Risks / Trade-offs

- **Doses sem registro:** até a F5, tudo aparece como pendente/atrasado. Mitigação:
  status derivado deixa claro o que já passou; copy não afirma "não tomado".
- **`observeAll()` de horários:** custo de observar todos os horários é baixo (dataset
  pequeno, uso doméstico). Mantém a tela reativa sem polling.
- **Mudança na navegação raiz:** introduzir abas altera a experiência inicial; mitigado
  por manter a lista existente intacta e apenas reapontar o clique para o detalhe.

## Migration Plan

Sem migração de dados (apenas leitura/observação). A query adicional do DAO não altera o
schema do Room.

## Open Questions

- Visão semanal/mensal agregada (além da timeline por dia) fica para iteração futura se o
  usuário pedir; PRD-2 cobre dia/semana, atendido pela timeline navegável.
