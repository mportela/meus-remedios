## 1. Camada de dados

- [x] 1.1 Adicionar query ao `IntakeLogDao`:
         `@Query("SELECT * FROM intake_logs WHERE medication_id = :medicationId AND schedule_time_id = :scheduleTimeId AND date = :date LIMIT 1")
          suspend fun getByMedicationScheduleDate(medicationId: Long, scheduleTimeId: Long, date: String): IntakeLogEntity?`
- [x] 1.2 Adicionar `suspend fun getByMedicationScheduleAndDate(medicationId: Long, scheduleTimeId: Long, date: LocalDate): IntakeLog?`
         à interface `IntakeLogRepository` e implementar em `IntakeLogRepositoryImpl`
         (converte `LocalDate` para String ISO via conversor existente; mapeia entidade para domínio)

## 2. Use case de lookup de doses pendentes do dia

- [x] 2.1 Criar `GetPendingDosesTodayForMedicationUseCase`:
         - Injeta `ScheduleRepository`, `MedicationRepository`, `IntakeLogRepository`, `Clock`
         - `suspend operator fun invoke(medicationId: Long): List<ScheduledDose>` retorna
           doses do medicamento para hoje com status PENDING ou LATE (sem IntakeLog existente
           ou com IntakeLog PENDING); exclui TAKEN e SKIPPED
         - Reutiliza a lógica de derivação de `ObserveDailyReportUseCase`, filtrada por
           medicationId e status

## 3. Use cases de registro

- [x] 3.1 Criar `MarkIntakeTakenUseCase`:
         - Injeta `IntakeLogRepository` e `Clock`
         - `suspend operator fun invoke(dose: ScheduledDose, date: LocalDate)`:
           busca log via `getByMedicationScheduleAndDate`; mesmo status → delete;
           status diferente → update para TAKEN + takenAt; ausente → insert TAKEN
         - Sobrecarga ad-hoc: `invoke(medicationId: Long, date: LocalDate)` — insere
           IntakeLog com `scheduleTimeId = null` e `scheduledAt = Instant.now(clock)`
- [x] 3.2 Criar `MarkIntakeSkippedUseCase`:
         - Mesma estrutura de 3.1, mas status alvo = SKIPPED e `takenAt = null`
         - Sem sobrecarga ad-hoc (pular sem dose agendada não faz sentido semântico)

## 4. ScheduledDose — campo scheduledAt

- [x] 4.1 Verificar se `ScheduledDose` tem `scheduledAt: Instant`; se não, derivar como
         `LocalDateTime.of(date, schedule.timeOfDay).toInstant(ZoneOffset.UTC)` em
         `ObserveDailyReportUseCase` e adicionar o campo ao modelo, para uso nos use cases
         ao criar o IntakeLog

## 5. ViewModel — TodayViewModel

- [x] 5.1 Injetar `MarkIntakeTakenUseCase` e `MarkIntakeSkippedUseCase` em `TodayViewModel`
- [x] 5.2 Adicionar `fun markTaken(dose: ScheduledDose)`:
         `viewModelScope.launch { markIntakeTakenUseCase(dose, selectedDate.value) }`
- [x] 5.3 Adicionar `fun markSkipped(dose: ScheduledDose)` com mesma estrutura

## 6. ViewModel — RecognitionViewModel

- [x] 6.1 Injetar `GetPendingDosesTodayForMedicationUseCase` e `MarkIntakeTakenUseCase`
         em `RecognitionViewModel`
- [x] 6.2 Adicionar campo ao `RecognitionUiState`:
         `pendingDosesToday: List<ScheduledDose> = emptyList()` e
         `intakeRegistered: Boolean = false`
- [x] 6.3 Adicionar `fun markTakenFromRecognition()`:
         - Obtém `medicationId` de `(uiState.outcome as? Confident)?.best?.medicationId`
         - Chama `GetPendingDosesTodayForMedicationUseCase(medicationId)`
         - 0 doses → chama sobrecarga ad-hoc `markIntakeTakenUseCase(medicationId, today)`
         - 1 dose → chama `markIntakeTakenUseCase(dose, today)` diretamente
         - N > 1 doses → atualiza `pendingDosesToday` (UI exibe seletor)
         - Em sucesso: atualiza `intakeRegistered = true`
- [x] 6.4 Adicionar `fun markTakenForDose(dose: ScheduledDose)` para o caso N > 1:
         chama `markIntakeTakenUseCase(dose, today)` e atualiza `intakeRegistered = true`

## 7. UI — TodayScreen

- [x] 7.1 Atualizar `PeriodSection` para receber `onMarkTaken: (ScheduledDose) -> Unit` e
         `onMarkSkipped: (ScheduledDose) -> Unit` e repassar para `DoseCard`
- [x] 7.2 Atualizar `DoseCard`:
         - PENDING/LATE: botão "Tomei" (primário) + botão textual "Pular"
         - TAKEN: ícone de check + botão textual "Desfazer" (chama onMarkTaken para toggle)
         - SKIPPED: ícone bloqueado + botão textual "Desfazer" (chama onMarkSkipped para toggle)
         - Toque no card continua abrindo detalhe do medicamento
- [x] 7.3 Atualizar `TodayScreen` para passar `viewModel::markTaken` e
         `viewModel::markSkipped` para `PeriodSection`
- [x] 7.4 Adicionar `SummaryCard` de "Puladas" ao `SummaryRow` (modelo já tem `skippedCount`)
- [x] 7.5 Adicionar strings em `res/values/strings.xml`:
         `today_action_taken` ("Tomei"), `today_action_skip` ("Pular"),
         `today_action_undo` ("Desfazer"), `today_summary_skipped` ("Puladas")

## 8. UI — RecognitionScreen

- [x] 8.1 Atualizar `ConfidentResult` para receber `onMarkTaken: () -> Unit` e
         `intakeRegistered: Boolean`; exibir botão "Tomei" quando `!intakeRegistered`
         e texto "✓ Registrado" quando `intakeRegistered`
- [x] 8.2 Atualizar `ResultContent` para passar `viewModel::markTakenFromRecognition` e
         `uiState.intakeRegistered` para `ConfidentResult`
- [x] 8.3 Quando `uiState.pendingDosesToday.size > 1`, exibir `ModalBottomSheet` com a
         lista de horários pendentes; toque num horário chama `viewModel.markTakenForDose(dose)`;
         fechar o bottom sheet sem escolher cancela sem efeito
- [x] 8.4 Adicionar string `recognition_action_taken` ("Tomei") e
         `recognition_intake_registered` ("Registrado")

## 9. Testes

- [x] 9.1 Criar `MarkIntakeTakenUseCaseTest`:
         - Sem log existente → insert com TAKEN
         - Log existente TAKEN → delete (toggle off)
         - Log existente SKIPPED → update para TAKEN
         - Sobrecarga ad-hoc → insert com scheduleTimeId = null
- [x] 9.2 Criar `MarkIntakeSkippedUseCaseTest`:
         - Sem log existente → insert com SKIPPED
         - Log existente SKIPPED → delete (toggle off)
         - Log existente TAKEN → update para SKIPPED
- [x] 9.3 Criar `GetPendingDosesTodayForMedicationUseCaseTest`:
         - Medicamento com 1 dose hoje sem log → retorna 1 dose
         - Medicamento com dose TAKEN hoje → retorna 0 doses
         - Medicamento sem horário hoje → retorna 0 doses
- [x] 9.4 Adicionar ao `TodayViewModelTest` (ou criar):
         - `markTaken` dispara use case com dose e data selecionada
         - `markSkipped` idem
- [x] 9.5 Adicionar ao `RecognitionViewModelTest` (ou criar):
         - `markTakenFromRecognition` com 0 doses → chama ad-hoc; `intakeRegistered = true`
         - `markTakenFromRecognition` com 1 dose → chama use case com dose; `intakeRegistered = true`
         - `markTakenFromRecognition` com N doses → `pendingDosesToday` populado; `intakeRegistered = false`
- [x] 9.6 Rodar `./gradlew :app:testDebugUnitTest` e confirmar 0 falhas

## 10. Documentação

- [x] 10.1 Atualizar `CHANGELOG.md` com entrada F5 em "Não lançado"
- [ ] 10.2 Executar `openspec archive --change add-intake-tracking`
