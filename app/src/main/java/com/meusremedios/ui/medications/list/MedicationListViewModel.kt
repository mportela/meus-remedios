package com.meusremedios.ui.medications.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.usecase.SearchMedicationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado da tela de lista de medicamentos. */
data class MedicationListUiState(
    val query: String = "",
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MedicationListViewModel @Inject constructor(
    searchMedications: SearchMedicationsUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    val queryState: StateFlow<String> = query.asStateFlow()

    val uiState: StateFlow<MedicationListUiState> =
        query
            .flatMapLatest { q ->
                searchMedications(q).map { medications ->
                    MedicationListUiState(
                        query = q,
                        medications = medications,
                        isLoading = false,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MedicationListUiState(),
            )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun clearQuery() {
        query.value = ""
    }
}
