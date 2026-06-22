package com.meusremedios.ui.medications.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.usecase.DeleteMedicationUseCase
import com.meusremedios.domain.usecase.GetMedicationUseCase
import com.meusremedios.domain.usecase.MedicationValidationError
import com.meusremedios.domain.usecase.SaveMedicationResult
import com.meusremedios.domain.usecase.SaveMedicationUseCase
import com.meusremedios.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Estado editável do formulário de medicamento. */
data class MedicationFormUiState(
    val id: Long = 0L,
    val name: String = "",
    val dosage: String = "",
    val notes: String = "",
    val periodType: PeriodType = PeriodType.CONTINUOUS,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val remindersEnabled: Boolean = true,
    val schedules: List<ScheduleTime> = emptyList(),
    val validationError: MedicationValidationError? = null,
    val isLoading: Boolean = true,
) {
    val isEditing: Boolean get() = id != 0L
}

@HiltViewModel
class MedicationFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMedication: GetMedicationUseCase,
    private val saveMedication: SaveMedicationUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
) : ViewModel() {

    private val medicationId: Long = savedStateHandle.get<Long>(Routes.ARG_MEDICATION_ID) ?: 0L
    private var createdAt: Instant = Instant.now()

    private val _uiState = MutableStateFlow(MedicationFormUiState(id = medicationId))
    val uiState: StateFlow<MedicationFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<MedicationFormEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (medicationId != 0L) {
            loadMedication(medicationId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadMedication(id: Long) {
        viewModelScope.launch {
            val loaded = getMedication(id)
            if (loaded == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            val medication = loaded.medication
            createdAt = medication.createdAt
            _uiState.value = MedicationFormUiState(
                id = medication.id,
                name = medication.name,
                dosage = medication.dosage.orEmpty(),
                notes = medication.notes.orEmpty(),
                periodType = medication.periodType,
                startDate = medication.startDate,
                endDate = medication.endDate,
                remindersEnabled = medication.remindersEnabled,
                schedules = loaded.schedules,
                isLoading = false,
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, validationError = null)
    }

    fun onDosageChange(value: String) {
        _uiState.value = _uiState.value.copy(dosage = value)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onPeriodTypeChange(value: PeriodType) {
        _uiState.value = if (value == PeriodType.CONTINUOUS) {
            _uiState.value.copy(
                periodType = value,
                startDate = null,
                endDate = null,
                validationError = null,
            )
        } else {
            _uiState.value.copy(periodType = value)
        }
    }

    fun onStartDateChange(value: LocalDate?) {
        _uiState.value = _uiState.value.copy(startDate = value, validationError = null)
    }

    fun onEndDateChange(value: LocalDate?) {
        _uiState.value = _uiState.value.copy(endDate = value, validationError = null)
    }

    fun onRemindersChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(remindersEnabled = enabled)
    }

    fun addSchedule(time: LocalTime, daysOfWeekMask: Int = ScheduleTime.ALL_DAYS_MASK) {
        val schedule = ScheduleTime(
            medicationId = medicationId,
            timeOfDay = time,
            daysOfWeekMask = daysOfWeekMask,
        )
        _uiState.value = _uiState.value.copy(schedules = _uiState.value.schedules + schedule)
    }

    fun removeSchedule(index: Int) {
        val current = _uiState.value.schedules
        if (index in current.indices) {
            _uiState.value = _uiState.value.copy(
                schedules = current.toMutableList().apply { removeAt(index) },
            )
        }
    }

    fun updateScheduleDays(index: Int, daysOfWeekMask: Int) {
        val current = _uiState.value.schedules
        if (index in current.indices) {
            _uiState.value = _uiState.value.copy(
                schedules = current.toMutableList().apply {
                    this[index] = this[index].copy(daysOfWeekMask = daysOfWeekMask)
                },
            )
        }
    }

    fun save() {
        val state = _uiState.value
        val medication = Medication(
            id = state.id,
            name = state.name,
            dosage = state.dosage.trim().ifBlank { null },
            notes = state.notes.trim().ifBlank { null },
            periodType = state.periodType,
            startDate = if (state.periodType == PeriodType.RANGED) state.startDate else null,
            endDate = if (state.periodType == PeriodType.RANGED) state.endDate else null,
            remindersEnabled = state.remindersEnabled,
            createdAt = createdAt,
        )
        viewModelScope.launch {
            when (val result = saveMedication(medication, state.schedules)) {
                is SaveMedicationResult.Success -> _events.send(MedicationFormEvent.Saved)
                is SaveMedicationResult.Invalid ->
                    _uiState.value = _uiState.value.copy(validationError = result.error)
            }
        }
    }

    fun delete() {
        val state = _uiState.value
        if (state.id == 0L) return
        viewModelScope.launch {
            deleteMedication(
                Medication(
                    id = state.id,
                    name = state.name,
                    createdAt = createdAt,
                ),
            )
            _events.send(MedicationFormEvent.Deleted)
        }
    }
}

/** Eventos one-shot do formulário. */
sealed interface MedicationFormEvent {
    data object Saved : MedicationFormEvent
    data object Deleted : MedicationFormEvent
}
