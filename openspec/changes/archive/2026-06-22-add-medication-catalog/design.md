## Context

A F1 entregou os repositórios (`MedicationRepository`, `ScheduleRepository`) e os
modelos de domínio. Esta change implementa a primeira feature funcional do app —
o catálogo de medicamentos (PRD-1, exceto fotos) — com use cases puros e UI
Compose acessível, seguindo o MVVM em camadas definido em
[TECH-1](../../../docs/technical/tech-1-arquitetura.md) e a acessibilidade de
[TECH-4](../../../docs/technical/tech-4-acessibilidade.md).

## Goals / Non-Goals

**Goals:**
- Use cases determinísticos de CRUD + busca, com validações de RN-1.1 e RN-1.3.
- Gestão de horários (`ScheduleTime`) integrada ao formulário do medicamento.
- UI Compose: lista com busca, formulário de cadastro/edição e exclusão com
  confirmação, tudo acessível (campos/botões grandes, pt-BR, semântica TalkBack).
- ViewModels MVVM expondo `StateFlow`; navegação via Navigation Compose.
- Testes de use cases e ViewModels com fakes de repositório.

**Non-Goals:**
- Fotos do comprimido e extração de features (RF-1.4/1.5/1.8) → change
  `add-medication-photos`.
- Reconhecimento visual (F4), lembretes/alarmes (F6), relatórios (F3).
- Migração de schema (a F1 já está na v1; nenhuma entidade nova).

## Decisions

- **Use cases puros em `domain/usecase`**: cada operação (observar, buscar, obter,
  adicionar, atualizar, excluir) como classe injetável com `operator fun invoke`,
  testável fora da UI. Validações de negócio (nome obrigatório, datas) ficam no
  use case de escrita, retornando `Result` para a UI tratar erros. Alternativa
  (validar na UI/ViewModel) descartada para manter a regra testável e única.
- **Salvamento atômico de medicamento + horários**: o use case de salvar recebe o
  medicamento e a lista de horários e persiste ambos; horários removidos na edição
  são deletados. Como Room já está local e single-writer, não há transação
  distribuída; a operação é sequencial dentro do use case.
- **`StateFlow` na UI**: ViewModels expõem um estado imutável (`data class`) e
  eventos one-shot (mensagens de erro/sucesso) via canal/efeito, conforme padrão
  do projeto (StateFlow → UI).
- **Navigation Compose**: rotas `medications` (lista) e `medications/form?id={id}`
  (cadastro/edição). `id` ausente = novo; presente = edição.
- **Datas e horários**: pickers nativos do Material 3; horários como `LocalTime`,
  datas como `LocalDate`. Dias da semana como chips selecionáveis mapeados ao
  bitmask de 7 bits já existente em `ScheduleTime`.
- **Acessibilidade**: alvos de toque ≥ 48dp, tipografia ampliada do tema, rótulos
  e `contentDescription` em pt-BR, agrupamento semântico de campos.

## Risks / Trade-offs

- **Formulário longo para idosos**: mitigado com seções claras e rótulos grandes;
  etapas curtas conforme PRD. Sem wizard multi-tela nesta change para reduzir
  complexidade — pode evoluir depois.
- **Validação de datas só faz sentido com `PeriodType.RANGED`**: o use case
  valida `endDate ≥ startDate` apenas quando ambas estão presentes.

## Migration Plan

Sem migração de dados: a feature consome o schema v1 da F1. Entrega incremental —
lista e formulário podem ser implementados e testados isoladamente via fakes.

## Open Questions

- Tela de **detalhe** dedicada vs. abrir direto em edição: nesta change, tocar em
  um item abre o formulário em modo edição (mais simples para o público-alvo).
