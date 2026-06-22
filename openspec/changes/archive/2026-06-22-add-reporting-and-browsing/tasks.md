## 1. Camada de dados e DI

- [x] 1.1 Adicionar `observeAll(): Flow<List<ScheduleTimeEntity>>` em `ScheduleTimeDao`.
- [x] 1.2 Adicionar `observeAll(): Flow<List<ScheduleTime>>` em `ScheduleRepository` + impl.
- [x] 1.3 Criar `di/TimeModule` provendo `java.time.Clock` (systemDefaultZone).

## 2. Modelos de domínio (`domain/model`)

- [x] 2.1 `DayPeriod` (MANHÃ/TARDE/NOITE) com `fromTime(LocalTime)`.
- [x] 2.2 `DoseStatus` (TAKEN/PENDING/LATE/SKIPPED).
- [x] 2.3 `ScheduledDose` (medicationId, medicationName, scheduleTimeId, time, status, period).
- [x] 2.4 `DailyReport` (date, doses ordenadas) com contagens derivadas.
- [x] 2.5 `MedicationDetail` (medication, schedules, photos, history).

## 3. Use cases (`domain/usecase`)

- [x] 3.1 `ObserveDailyReportUseCase(date)` — deriva doses do dia + status (Clock).
- [x] 3.2 `ObserveMedicationDetailUseCase(id)` — agrega remédio, horários, fotos e
      histórico dentro da retenção.

## 4. UI Hoje (`ui/today`)

- [x] 4.1 `TodayViewModel` — data selecionada + `StateFlow` do relatório.
- [x] 4.2 `TodayScreen` — timeline semanal navegável, resumo (tomados/pendentes/atrasados),
      doses agrupadas por período; estado vazio; abrir detalhe ao tocar numa dose.

## 5. UI Detalhe (`ui/medications/detail`)

- [x] 5.1 `MedicationDetailViewModel` — `StateFlow` do detalhe por id.
- [x] 5.2 `MedicationDetailScreen` — dados, fotos, horários, histórico recente, ação editar.

## 6. Navegação (`ui/navigation`)

- [x] 6.1 Rotas `today` e `medications/detail/{id}` em `Routes`.
- [x] 6.2 `MeusRemediosNavHost` com `NavigationBar` (Hoje · Meus remédios); lista→detalhe,
      detalhe→edição.

## 7. Strings e acessibilidade

- [x] 7.1 Strings pt-BR (Hoje, períodos, status, detalhe, histórico, abas).
- [x] 7.2 Semântica de cabeçalho, `contentDescription` e status legível por TalkBack.

## 8. Testes e verificação

- [x] 8.1 Fakes: `observeAll` em `FakeScheduleRepository`; `FakeIntakeLogRepository`,
      `FakeSettingsRepository`.
- [x] 8.2 `ObserveDailyReportUseCaseTest` (clock fixo, vetores sintéticos de status).
- [x] 8.3 `ObserveMedicationDetailUseCaseTest` (retenção, agregação).
- [x] 8.4 `TodayViewModelTest` e `MedicationDetailViewModelTest`.
- [x] 8.5 `./gradlew test assembleDebug` verde; atualizar `CHANGELOG.md`.
