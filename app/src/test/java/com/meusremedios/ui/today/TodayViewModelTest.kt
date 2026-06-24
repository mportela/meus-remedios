package com.meusremedios.ui.today

import app.cash.turbine.test
import com.meusremedios.MainDispatcherRule
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.usecase.FakeIntakeLogRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.MarkIntakeSkippedUseCase
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import com.meusremedios.domain.usecase.ObserveDailyReportUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone: ZoneId = ZoneId.systemDefault()
    private val date: LocalDate = LocalDate.of(2026, 6, 22)
    private val nowInstant: Instant =
        LocalDateTime.of(date, LocalTime.of(10, 0)).atZone(zone).toInstant()
    private val clock: Clock = Clock.fixed(nowInstant, zone)

    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val intakeLogs = FakeIntakeLogRepository()

    private fun viewModel(): TodayViewModel {
        val observeUseCase = ObserveDailyReportUseCase(medications, schedules, intakeLogs, clock)
        val markTaken = MarkIntakeTakenUseCase(intakeLogs, clock)
        val markSkipped = MarkIntakeSkippedUseCase(intakeLogs, clock)
        return TodayViewModel(observeUseCase, markTaken, markSkipped, clock)
    }

    @Test
    fun `expoe relatorio do dia atual`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

        val vm = viewModel()
        assertEquals(date, vm.today)

        vm.uiState.test {
            // Aguarda o estado carregado.
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.isLoading)
            assertEquals(1, state.report?.doses?.size)
            assertEquals(DoseStatus.LATE, state.report?.doses?.single()?.status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navegar para o proximo dia altera a data selecionada`() = runTest {
        val vm = viewModel()
        vm.goToNextDay()
        assertEquals(date.plusDays(1), vm.selectedDateState.value)
        vm.goToPreviousDay()
        vm.goToPreviousDay()
        assertEquals(date.minusDays(1), vm.selectedDateState.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `markTaken dispara registro TAKEN para a data selecionada`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        val vm = viewModel()

        // aguarda o relatório carregar e obtém a dose
        vm.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.report == null) state = awaitItem()
            val dose = state.report!!.doses.single()
            cancelAndIgnoreRemainingEvents()

            vm.markTaken(dose)
            advanceUntilIdle()

            val log = intakeLogs.observeByDate(date).first().singleOrNull()
            assertEquals(IntakeStatus.TAKEN, log?.status)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `markSkipped dispara registro SKIPPED para a data selecionada`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        val vm = viewModel()

        vm.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.report == null) state = awaitItem()
            val dose = state.report!!.doses.single()
            cancelAndIgnoreRemainingEvents()

            vm.markSkipped(dose)
            advanceUntilIdle()

            val log = intakeLogs.observeByDate(date).first().singleOrNull()
            assertEquals(IntakeStatus.SKIPPED, log?.status)
        }
    }
}
