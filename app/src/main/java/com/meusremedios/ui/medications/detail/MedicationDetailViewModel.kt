package com.meusremedios.ui.medications.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.domain.model.MedicationDetail
import com.meusremedios.domain.usecase.ObserveMedicationDetailUseCase
import com.meusremedios.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MedicationDetailUiState(
    val detail: MedicationDetail? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeMedicationDetail: ObserveMedicationDetailUseCase,
) : ViewModel() {

    val medicationId: Long = savedStateHandle.get<Long>(Routes.ARG_MEDICATION_ID) ?: 0L

    val uiState: StateFlow<MedicationDetailUiState> =
        observeMedicationDetail(medicationId)
            .map { detail -> MedicationDetailUiState(detail = detail, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MedicationDetailUiState(),
            )
}
