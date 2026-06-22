## Context

A F0 entregou o esqueleto do app com Room declarado mas sem schema. Esta change
implementa a camada de dados definida em
[TECH-2](../../../docs/technical/tech-2-modelo-dados.md): 5 entidades, suas
relações, type converters, índices e repositórios. É a fundação de persistência
para todas as features funcionais (F2–F7) e deve ser determinística e testável
fora da UI.

## Goals / Non-Goals

**Goals:**
- Esquema Room v1 completo (entidades, FKs, índices) conforme TECH-2.
- Modelos de domínio + enums desacoplados das entidades (mapeamento nos repos).
- DAOs reativos (`Flow`) e repositórios base que expõem modelos de domínio.
- Type converters reversíveis e cobertos por testes.
- Módulos Hilt provendo database/DAOs/repositórios.

**Non-Goals:**
- Use cases de feature (cadastro, busca, etc.) — ficam em F2+.
- Lógica de reconhecimento (preenchimento de embedding/cor) — F4.
- Telas/ViewModels — fases funcionais.
- Migrações além da v1 (não há schema anterior).

## Decisions

- **Entidades Room separadas dos modelos de domínio**: entidades em `data/local`,
  modelos puros em `domain/model`; mapeamento nos repositórios. Alternativa (usar
  entidades direto na UI) descartada para manter a UI/domínio independentes do
  Room e facilitar testes.
- **DAOs retornam `Flow`** para leituras observáveis (StateFlow → UI no padrão
  do projeto); operações de escrita são `suspend`.
- **`AppSettings` como singleton** com `id` fixo (= 1) e estratégia de upsert;
  leitura via `Flow` com fallback para padrões quando ausente.
- **Type converters**: `LocalDate`/`LocalTime` em ISO-8601 (estável, legível),
  `FloatArray` serializado em `ByteArray` (BLOB) via `ByteBuffer`, enums por
  `name`. Alternativa (epoch long) descartada para preservar legibilidade e
  evitar ambiguidade de timezone em datas locais.
- **`daysOfWeekMask` como bitmask Int** (7 bits) — compacto e suficiente.
- **Índices** em todas as FKs, `IntakeLog(date)` (relatórios/limpeza) e
  `Medication(name)` (busca), conforme TECH-2.
- **`onDelete = CASCADE`** nas FKs filhas para integridade ao remover medicamento.

## Risks / Trade-offs

- [Mapear entidade↔domínio adiciona boilerplate] → mantido por baixo acoplamento
  e testabilidade; o custo é pequeno e isolado nos repositórios.
- [Serialização de `FloatArray` precisa ser estável entre versões] → usar ordem
  de bytes fixa (`ByteBuffer` big-endian) e cobrir com teste de round-trip.
- [Singleton de settings pode gerar múltiplas linhas se mal gerenciado] → forçar
  `id = 1` e `OnConflictStrategy.REPLACE`.

## Migration Plan

Schema inicial (versão 1). Sem migração de dados. Reversão = reverter os arquivos
da camada de dados. Testes de migração serão adicionados quando o schema evoluir.

## Open Questions

- Dimensão exata do embedding (definida na F4) — o BLOB acomoda qualquer tamanho.
- Necessidade de exportação de schema Room (`room.schemaLocation`) para testes de
  migração futuros — habilitar quando a primeira migração surgir.
