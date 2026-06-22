## 1. Modelos de domínio e enums

- [x] 1.1 Criar enums `PeriodType`, `PhotoSide`, `IntakeStatus` em `domain/model`
- [x] 1.2 Criar modelos de domínio `Medication`, `MedicationPhoto`,
      `ScheduleTime`, `IntakeLog`, `AppSettings`

## 2. Entidades Room e converters

- [x] 2.1 Criar `MedicationEntity` (índice em `name`)
- [x] 2.2 Criar `MedicationPhotoEntity` (FK + índice, `onDelete = CASCADE`)
- [x] 2.3 Criar `ScheduleTimeEntity` (FK + índice, `onDelete = CASCADE`)
- [x] 2.4 Criar `IntakeLogEntity` (FKs + índice em `date`, `onDelete = CASCADE`)
- [x] 2.5 Criar `AppSettingsEntity` (singleton, `id = 1`)
- [x] 2.6 Criar `Converters` (LocalDate/LocalTime ISO, FloatArray↔BLOB, enums)
- [x] 2.7 Criar mapeadores entidade ↔ domínio

## 3. DAOs e banco

- [x] 3.1 Criar `MedicationDao` (CRUD + listagem `Flow` ordenada por nome)
- [x] 3.2 Criar `MedicationPhotoDao` (CRUD + consulta por `medicationId`)
- [x] 3.3 Criar `ScheduleTimeDao` (CRUD + consulta por `medicationId`)
- [x] 3.4 Criar `IntakeLogDao` (CRUD + consulta por `date`)
- [x] 3.5 Criar `AppSettingsDao` (upsert + leitura `Flow`)
- [x] 3.6 Criar `MeusRemediosDatabase` (versão 1, registrando entidades/converters)

## 4. Repositórios base

- [x] 4.1 Criar `MedicationRepository` (expõe modelos de domínio via `Flow`)
- [x] 4.2 Criar `MedicationPhotoRepository`
- [x] 4.3 Criar `ScheduleRepository`
- [x] 4.4 Criar `IntakeLogRepository`
- [x] 4.5 Criar `SettingsRepository` (com valores padrão)

## 5. Injeção de dependência (Hilt)

- [x] 5.1 Criar `DatabaseModule` (provê `MeusRemediosDatabase` e DAOs)
- [x] 5.2 Criar `RepositoryModule` (vincula repositórios)

## 6. Testes e verificação

- [x] 6.1 Testes de `Converters` (round-trip determinístico)
- [x] 6.2 Testes de DAOs com Room in-memory (Robolectric) cobrindo CRUD,
      cascade, consultas por data/medicationId e listagem reativa (Turbine)
- [x] 6.3 Testes de repositório (mapeamento e settings padrão)
- [x] 6.4 Rodar `./gradlew test` e `./gradlew assembleDebug`
- [x] 6.5 Atualizar `CHANGELOG.md` (seção "Não lançado")
