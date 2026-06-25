package com.meusremedios.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val saveSettingsUseCase: SaveSettingsUseCase,
) : ViewModel() {

    val uiState: StateFlow<AppSettings> = settingsRepository.observe()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AppSettings(),
        )

    fun save(settings: AppSettings) {
        viewModelScope.launch {
            saveSettingsUseCase(settings)
        }
    }
}
