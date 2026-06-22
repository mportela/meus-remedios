## Why

Com o scaffolding (F0) pronto, as próximas features (cadastro, reconhecimento,
lembretes, registro de tomadas, relatórios, configurações) precisam de uma
**camada de persistência local** comum. Esta change estabelece o banco Room —
entidades, DAOs, type converters, banco e repositórios base — para que F2–F7
construam sobre uma fundação de dados consistente, testável e 100% offline.

## What Changes

- Adicionar as entidades Room: `Medication`, `MedicationPhoto`, `ScheduleTime`,
  `IntakeLog`, `AppSettings` (singleton), conforme TECH-2.
- Definir enums de domínio (`PeriodType`, `PhotoSide`, `IntakeStatus`) e as
  relações 1—N de `Medication` com fotos, horários e logs.
- Implementar type converters (`LocalDate`/`LocalTime` ↔ ISO, `FloatArray` ↔
  BLOB, enums ↔ string) e índices (FKs, `IntakeLog(date)`, `Medication(name)`).
- Criar a classe `MeusRemediosDatabase` (Room) na versão 1, com DAOs por
  entidade expondo `Flow` para leitura reativa.
- Criar repositórios base que mediam domínio ↔ DAOs, expondo modelos de domínio.
- Configurar módulos Hilt para prover database, DAOs e repositórios.
- Adicionar testes de DAO/repositório com Room in-memory (Robolectric) e testes
  determinísticos dos type converters.

## Capabilities

### New Capabilities
- `local-data-layer`: camada de persistência local (Room) transversal —
  entidades, DAOs, type converters, banco versionado e repositórios base que
  habilitam as features de catálogo, fotos, agenda, tomadas e configurações.

### Modified Capabilities
<!-- Nenhuma: as capabilities funcionais ainda não foram criadas. -->

## Impact

- **Novo código**: `data/local` (entidades, DAOs, converters, database),
  `domain/model` (modelos + enums), `data/repository` (repos base), `di`
  (módulos Hilt de dados).
- **Dependências**: já declaradas na F0 (Room runtime/ktx/compiler, room-testing,
  Robolectric, coroutines-test, Turbine).
- **Sem rede**: persistência 100% local em armazenamento privado do app.
- **Documentação**: atualização do `CHANGELOG.md` (seção "Não lançado").
