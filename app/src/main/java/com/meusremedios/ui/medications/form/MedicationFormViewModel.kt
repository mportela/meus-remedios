package com.meusremedios.ui.medications.form

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.ml.FeatureSet
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.usecase.AddMedicationPhotoUseCase
import com.meusremedios.domain.usecase.CheckPhotoCollisionUseCase
import com.meusremedios.domain.usecase.DeleteMedicationUseCase
import com.meusremedios.domain.usecase.GetMedicationUseCase
import com.meusremedios.domain.usecase.MedicationValidationError
import com.meusremedios.domain.usecase.ObserveMedicationPhotosUseCase
import com.meusremedios.domain.usecase.RemoveMedicationPhotoUseCase
import com.meusremedios.domain.usecase.SaveMedicationResult
import com.meusremedios.domain.usecase.SaveMedicationUseCase
import com.meusremedios.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Foto ainda não persistida, aguardando o salvamento do medicamento. */
data class PendingPhoto(
    val tempPath: String,
    val side: PhotoSide,
)

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
    val photos: List<MedicationPhoto> = emptyList(),
    val pendingPhotos: List<PendingPhoto> = emptyList(),
    val validationError: MedicationValidationError? = null,
    val isLoading: Boolean = true,
) {
    val isEditing: Boolean get() = id != 0L
}

@HiltViewModel
class MedicationFormViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val getMedication: GetMedicationUseCase,
        private val saveMedication: SaveMedicationUseCase,
        private val deleteMedication: DeleteMedicationUseCase,
        private val observeMedicationPhotos: ObserveMedicationPhotosUseCase,
        private val addMedicationPhoto: AddMedicationPhotoUseCase,
        private val removeMedicationPhoto: RemoveMedicationPhotoUseCase,
        private val imageStore: MedicationImageStore,
        private val checkPhotoCollision: CheckPhotoCollisionUseCase,
        private val featureExtractor: FeatureExtractor,
    ) : ViewModel() {
        private val medicationId: Long = savedStateHandle.get<Long>(Routes.ARG_MEDICATION_ID) ?: 0L
        private var createdAt: Instant = Instant.now()
        private val removedPhotos = mutableListOf<MedicationPhoto>()
        private var cameraTempPath: String? = null

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
                val photos = observeMedicationPhotos(id).first()
                _uiState.value =
                    MedicationFormUiState(
                        id = medication.id,
                        name = medication.name,
                        dosage = medication.dosage.orEmpty(),
                        notes = medication.notes.orEmpty(),
                        periodType = medication.periodType,
                        startDate = medication.startDate,
                        endDate = medication.endDate,
                        remindersEnabled = medication.remindersEnabled,
                        schedules = loaded.schedules,
                        photos = photos,
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
            _uiState.value =
                if (value == PeriodType.CONTINUOUS) {
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

        fun addSchedule(
            time: LocalTime,
            daysOfWeekMask: Int = ScheduleTime.ALL_DAYS_MASK,
        ) {
            val schedule =
                ScheduleTime(
                    medicationId = medicationId,
                    timeOfDay = time,
                    daysOfWeekMask = daysOfWeekMask,
                )
            _uiState.value = _uiState.value.copy(schedules = _uiState.value.schedules + schedule)
        }

        fun removeSchedule(index: Int) {
            val current = _uiState.value.schedules
            if (index in current.indices) {
                _uiState.value =
                    _uiState.value.copy(
                        schedules = current.toMutableList().apply { removeAt(index) },
                    )
            }
        }

        fun updateScheduleDays(
            index: Int,
            daysOfWeekMask: Int,
        ) {
            val current = _uiState.value.schedules
            if (index in current.indices) {
                _uiState.value =
                    _uiState.value.copy(
                        schedules =
                            current.toMutableList().apply {
                                this[index] = this[index].copy(daysOfWeekMask = daysOfWeekMask)
                            },
                    )
            }
        }

        fun addPendingPhoto(
            tempPath: String,
            side: PhotoSide,
        ) {
            _uiState.value =
                _uiState.value.copy(
                    pendingPhotos = _uiState.value.pendingPhotos + PendingPhoto(tempPath, side),
                )
        }

        fun removePendingPhoto(index: Int) {
            val current = _uiState.value.pendingPhotos
            if (index in current.indices) {
                _uiState.value =
                    _uiState.value.copy(
                        pendingPhotos = current.toMutableList().apply { removeAt(index) },
                    )
            }
        }

        fun removePhoto(photo: MedicationPhoto) {
            removedPhotos += photo
            _uiState.value =
                _uiState.value.copy(
                    photos = _uiState.value.photos.filterNot { it.id == photo.id },
                )
        }

        /** Cria o arquivo de saída para a câmera e entrega o [Uri] via [onReady]. */
        fun prepareCameraCapture(onReady: (Uri) -> Unit) {
            viewModelScope.launch {
                val target = imageStore.createCameraTarget()
                cameraTempPath = target.tempPath
                onReady(target.uri)
            }
        }

        /** Confirma a foto capturada pela câmera, associando-a ao [side] escolhido. */
        fun onCameraCaptured(side: PhotoSide) {
            cameraTempPath?.let { path ->
                addPendingPhoto(path, side)
                cameraTempPath = null
            }
        }

        /** Copia a imagem selecionada da galeria e a adiciona como pendente. */
        fun onGalleryPicked(
            uri: Uri,
            side: PhotoSide,
        ) {
            viewModelScope.launch {
                val tempPath = imageStore.stage(uri)
                addPendingPhoto(tempPath, side)
            }
        }

        fun save() {
            viewModelScope.launch {
                val state = _uiState.value
                // Verificar colisão visual antes de persistir
                if (state.pendingPhotos.isNotEmpty()) {
                    _uiState.value = state.copy(isLoading = true)
                    val queryFeatures =
                        state.pendingPhotos.mapNotNull { pending ->
                            val f = featureExtractor.extract(pending.tempPath)
                            f.embedding?.let {
                                FeatureSet(
                                    embedding = it,
                                    colorLab = f.dominantColorLab,
                                    aspectRatio = f.aspectRatio,
                                    imprintText = f.imprintText,
                                )
                            }
                        }
                    if (queryFeatures.isNotEmpty()) {
                        val collisions = checkPhotoCollision(queryFeatures, state.id)
                        if (collisions.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            _events.send(
                                MedicationFormEvent.CollisionWarning(collisions.first().medicationName),
                            )
                            return@launch
                        }
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                persistSave()
            }
        }

        fun saveIgnoringCollision() {
            viewModelScope.launch { persistSave() }
        }

        private suspend fun persistSave() {
            val state = _uiState.value
            val medication =
                Medication(
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
            when (val result = saveMedication(medication, state.schedules)) {
                is SaveMedicationResult.Success -> {
                    removedPhotos.forEach { removeMedicationPhoto(it) }
                    removedPhotos.clear()
                    state.pendingPhotos.forEach { pending ->
                        addMedicationPhoto(result.medicationId, pending.tempPath, pending.side)
                    }
                    _events.send(MedicationFormEvent.Saved)
                }
                is SaveMedicationResult.Invalid ->
                    _uiState.value = _uiState.value.copy(validationError = result.error)
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

    data class CollisionWarning(val candidateName: String) : MedicationFormEvent
}
