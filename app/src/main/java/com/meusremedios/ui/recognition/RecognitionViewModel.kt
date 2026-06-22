package com.meusremedios.ui.recognition

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.domain.model.RecognitionOutcome
import com.meusremedios.domain.usecase.RecognizeMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fase do fluxo de reconhecimento. */
enum class RecognitionPhase { IDLE, ANALYZING, RESULT, ERROR }

data class RecognitionUiState(
    val phase: RecognitionPhase = RecognitionPhase.IDLE,
    val outcome: RecognitionOutcome? = null,
    /** Indica se já há uma 1ª foto e o resultado ambíguo permite tentar a 2ª. */
    val canAddSecondPhoto: Boolean = false,
)

@HiltViewModel
class RecognitionViewModel @Inject constructor(
    private val imageStore: MedicationImageStore,
    private val recognizeMedication: RecognizeMedicationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecognitionUiState())
    val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

    private val queryPaths = mutableListOf<String>()
    private var pendingTempPath: String? = null

    /** Cria o arquivo de saída para a câmera e entrega o [Uri] via [onReady]. */
    fun prepareCapture(onReady: (Uri) -> Unit) {
        viewModelScope.launch {
            val target = imageStore.createCameraTarget()
            pendingTempPath = target.tempPath
            onReady(target.uri)
        }
    }

    /** Confirma a captura da câmera e dispara a análise com todas as fotos. */
    fun onCaptured() {
        val path = pendingTempPath ?: return
        pendingTempPath = null
        queryPaths += path
        analyze()
    }

    private fun analyze() {
        _uiState.update { it.copy(phase = RecognitionPhase.ANALYZING) }
        viewModelScope.launch {
            runCatching { recognizeMedication(queryPaths.toList()) }
                .onSuccess { outcome ->
                    _uiState.update {
                        it.copy(
                            phase = RecognitionPhase.RESULT,
                            outcome = outcome,
                            canAddSecondPhoto = outcome is RecognitionOutcome.Ambiguous &&
                                queryPaths.size < MAX_QUERY_PHOTOS,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(phase = RecognitionPhase.ERROR, outcome = null, canAddSecondPhoto = false)
                    }
                }
        }
    }

    /** Reinicia o fluxo, descartando as fotos de consulta temporárias. */
    fun reset() {
        val paths = queryPaths.toList()
        queryPaths.clear()
        pendingTempPath = null
        _uiState.value = RecognitionUiState()
        viewModelScope.launch {
            paths.forEach { imageStore.delete(it) }
        }
    }

    private companion object {
        const val MAX_QUERY_PHOTOS = 2
    }
}
