package com.meusremedios.ui.medications.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.meusremedios.MainDispatcherRule
import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.usecase.FakeIntakeLogRepository
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.FakeSettingsRepository
import com.meusremedios.domain.usecase.ObserveMedicationDetailUseCase
import com.meusremedios.ui.navigation.Routes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class MedicationDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-06-22T12:00:00Z"), ZoneId.of("UTC"))

    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val photos = FakeMedicationPhotoRepository()
    private val intakeLogs = FakeIntakeLogRepository()

    private fun viewModel(id: Long): MedicationDetailViewModel {
        val useCase =
            ObserveMedicationDetailUseCase(
                medications,
                schedules,
                photos,
                intakeLogs,
                FakeSettingsRepository(AppSettings()),
                clock,
            )
        val handle = SavedStateHandle(mapOf(Routes.ARG_MEDICATION_ID to id))
        return MedicationDetailViewModel(handle, useCase)
    }

    @Test
    fun `expoe detalhe do medicamento`() =
        runTest {
            val id = medications.add(Medication(name = "Losartana", dosage = "50 mg"))
            schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

            val vm = viewModel(id)
            vm.uiState.test {
                var state = awaitItem()
                while (state.isLoading) {
                    state = awaitItem()
                }
                assertEquals("Losartana", state.detail?.medication?.name)
                assertEquals(1, state.detail?.schedules?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
