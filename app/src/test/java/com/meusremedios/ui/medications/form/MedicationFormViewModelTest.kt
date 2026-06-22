package com.meusremedios.ui.medications.form

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.meusremedios.MainDispatcherRule
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.usecase.DeleteMedicationUseCase
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.GetMedicationUseCase
import com.meusremedios.domain.usecase.MedicationValidationError
import com.meusremedios.domain.usecase.SaveMedicationUseCase
import com.meusremedios.ui.navigation.Routes
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MedicationFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val medicationRepository = FakeMedicationRepository()
    private val scheduleRepository = FakeScheduleRepository()

    private fun viewModel(id: Long = 0L): MedicationFormViewModel =
        MedicationFormViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_MEDICATION_ID to id)),
            getMedication = GetMedicationUseCase(medicationRepository, scheduleRepository),
            saveMedication = SaveMedicationUseCase(medicationRepository, scheduleRepository),
            deleteMedication = DeleteMedicationUseCase(medicationRepository),
        )

    @Test
    fun `save with blank name sets validation error`() = runTest {
        val vm = viewModel()

        vm.save()

        assertEquals(
            MedicationValidationError.BLANK_NAME,
            vm.uiState.value.validationError,
        )
        assertTrue(medicationRepository.snapshot().isEmpty())
    }

    @Test
    fun `save valid medication emits Saved event and persists`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Losartana")
        vm.addSchedule(LocalTime.of(8, 0))

        vm.events.test {
            vm.save()
            assertEquals(MedicationFormEvent.Saved, awaitItem())
        }
        assertEquals("Losartana", medicationRepository.snapshot().single().name)
        assertEquals(1, scheduleRepository.snapshot().size)
    }

    @Test
    fun `loads existing medication for editing`() = runTest {
        val id = medicationRepository.add(Medication(name = "Aspirina"))
        scheduleRepository.add(
            com.meusremedios.domain.model.ScheduleTime(
                medicationId = id,
                timeOfDay = LocalTime.of(7, 30),
            ),
        )

        val vm = viewModel(id)

        val state = vm.uiState.value
        assertEquals("Aspirina", state.name)
        assertTrue(state.isEditing)
        assertEquals(1, state.schedules.size)
    }

    @Test
    fun `switching to continuous clears dates`() = runTest {
        val vm = viewModel()
        vm.onPeriodTypeChange(PeriodType.RANGED)
        vm.onStartDateChange(LocalDate.of(2026, 1, 1))
        vm.onEndDateChange(LocalDate.of(2026, 2, 1))

        vm.onPeriodTypeChange(PeriodType.CONTINUOUS)

        assertNull(vm.uiState.value.startDate)
        assertNull(vm.uiState.value.endDate)
    }
}
