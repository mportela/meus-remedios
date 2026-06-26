package com.meusremedios.ui.recognition

import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.ml.FeatureSet
import com.meusremedios.data.ml.RecognitionParams
import com.meusremedios.data.ml.TfliteEmbedder
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.domain.model.RecognitionOutcome
import com.meusremedios.domain.model.ScheduledDose
import com.meusremedios.domain.usecase.GetPendingDosesTodayForMedicationUseCase
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import com.meusremedios.domain.usecase.RecognizeMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** Fase do fluxo de reconhecimento. */
enum class RecognitionPhase { IDLE, ANALYZING, RESULT, ERROR }

data class RecognitionUiState(
    val phase: RecognitionPhase = RecognitionPhase.IDLE,
    val outcome: RecognitionOutcome? = null,
    /** Indica se já há uma 1ª foto e o resultado ambíguo permite tentar a 2ª. */
    val canAddSecondPhoto: Boolean = false,
    /** Doses pendentes de hoje para o medicamento reconhecido (preenchido quando N > 1). */
    val pendingDosesToday: List<ScheduledDose> = emptyList(),
    /** Verdadeiro após o usuário registrar uma tomada bem-sucedida. */
    val intakeRegistered: Boolean = false,
    /** Verdadeiro quando a auto-captura está habilitada nas configurações. */
    val autoCaptureEnabled: Boolean = false,
    /** Verdadeiro durante o flash visual de auto-captura. */
    val showCaptureFlash: Boolean = false,
)

@HiltViewModel
class RecognitionViewModel
    @Inject
    constructor(
        private val imageStore: MedicationImageStore,
        private val recognizeMedication: RecognizeMedicationUseCase,
        private val getPendingDosesForMedication: GetPendingDosesTodayForMedicationUseCase,
        private val markIntakeTakenUseCase: MarkIntakeTakenUseCase,
        private val clock: Clock,
        private val settingsRepository: SettingsRepository,
        private val embedder: TfliteEmbedder,
        private val medicationPhotoRepository: MedicationPhotoRepository,
        private val medicationRepository: MedicationRepository,
    ) : ViewModel() {
        @VisibleForTesting
        internal var computationDispatcher: CoroutineDispatcher = Dispatchers.Default
        private val _uiState = MutableStateFlow(RecognitionUiState())
        val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

        private val _autoCaptureEvents = Channel<Unit>(Channel.CONFLATED)
        val autoCaptureEvents = _autoCaptureEvents.receiveAsFlow()

        private val queryPaths = mutableListOf<String>()
        private var pendingTempPath: String? = null
        private var lastAutoCaptureMs: Long = -AUTO_CAPTURE_COOLDOWN_MS
        private var isProcessingFrame: Boolean = false

        init {
            settingsRepository.observe()
                .onEach { settings ->
                    _uiState.update { it.copy(autoCaptureEnabled = settings.autoCapture) }
                }
                .launchIn(viewModelScope)
        }

        /** Cria o arquivo de saída para a câmera e entrega o [Uri] via [onReady]. */
        fun prepareCapture(onReady: (Uri) -> Unit) {
            viewModelScope.launch {
                val target = imageStore.createCameraTarget()
                pendingTempPath = target.tempPath
                onReady(target.uri)
            }
        }

        /**
         * Cria o arquivo de saída para a auto-captura e o entrega como [File] via
         * [onReady]. Equivalente a [prepareCapture], mas retorna o arquivo direto
         * para o `CameraXPreviewController.capturePhoto` (que escreve o JPEG nele).
         */
        fun prepareAutoCapture(onReady: (File) -> Unit) {
            viewModelScope.launch {
                val target = imageStore.createCameraTarget()
                pendingTempPath = target.tempPath
                onReady(File(target.tempPath))
            }
        }

        /**
         * Chamado quando a auto-captura falha (ex.: `ImageCapture` indisponível).
         * Descarta o alvo pendente e apaga o flash, mantendo a tela em IDLE para que
         * o usuário possa tentar de novo (o botão manual segue disponível).
         */
        fun onAutoCaptureFailed() {
            pendingTempPath = null
            _uiState.update { it.copy(showCaptureFlash = false) }
        }

        /** Confirma a captura da câmera e dispara a análise com todas as fotos. */
        fun onCaptured() {
            val path = pendingTempPath ?: return
            pendingTempPath = null
            queryPaths += path
            analyze()
        }

        /**
         * Usa uma imagem escolhida da galeria como foto de consulta (recurso de
         * desenvolvimento). Copia a [uri] para a área privada e dispara a análise,
         * reaproveitando o mesmo fluxo da câmera.
         */
        fun onGalleryPicked(uri: Uri) {
            _uiState.update { it.copy(phase = RecognitionPhase.ANALYZING) }
            viewModelScope.launch {
                val staged = runCatching { imageStore.stage(uri) }.getOrNull()
                if (staged == null) {
                    _uiState.update {
                        it.copy(phase = RecognitionPhase.ERROR, outcome = null, canAddSecondPhoto = false)
                    }
                    return@launch
                }
                queryPaths += staged
                analyze()
            }
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
                                canAddSecondPhoto =
                                    outcome is RecognitionOutcome.Ambiguous &&
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

        /**
         * Chamado pelo botão "Tomei" na tela de resultado confiante.
         * Determina quantas doses estão pendentes hoje e age conforme:
         * - 0 → registra tomada ad-hoc
         * - 1 → marca a dose diretamente
         * - N > 1 → popula [RecognitionUiState.pendingDosesToday] para exibir seletor na UI
         */
        fun markTakenFromRecognition() {
            val medicationId =
                (uiState.value.outcome as? RecognitionOutcome.Confident)
                    ?.best?.medicationId ?: return
            val today = LocalDate.now(clock)
            viewModelScope.launch {
                runCatching { getPendingDosesForMedication(medicationId) }
                    .onSuccess { doses ->
                        when (doses.size) {
                            0 -> {
                                markIntakeTakenUseCase(medicationId, today)
                                _uiState.update { it.copy(intakeRegistered = true) }
                            }
                            1 -> {
                                markIntakeTakenUseCase(doses.first(), today)
                                _uiState.update { it.copy(intakeRegistered = true) }
                            }
                            else -> _uiState.update { it.copy(pendingDosesToday = doses) }
                        }
                    }
            }
        }

        /** Chamado quando o usuário escolhe um horário no seletor (caso N > 1 doses). */
        fun markTakenForDose(dose: ScheduledDose) {
            val today = LocalDate.now(clock)
            viewModelScope.launch {
                runCatching { markIntakeTakenUseCase(dose, today) }
                    .onSuccess {
                        _uiState.update { it.copy(pendingDosesToday = emptyList(), intakeRegistered = true) }
                    }
            }
        }

        /** Reinicia o fluxo, descartando as fotos de consulta temporárias. */
        fun reset() {
            val paths = queryPaths.toList()
            queryPaths.clear()
            pendingTempPath = null
            isProcessingFrame = false
            _uiState.value = RecognitionUiState(autoCaptureEnabled = _uiState.value.autoCaptureEnabled)
            viewModelScope.launch {
                paths.forEach { imageStore.delete(it) }
            }
        }

        /**
         * Processa um frame ao vivo do preview CameraX.
         * Chamado ~5 fps quando [autoCaptureEnabled] é verdadeiro.
         * Se o embedding do frame tiver score suficiente contra qualquer cadastro,
         * emite evento de auto-captura (com cooldown de [AUTO_CAPTURE_COOLDOWN_MS]).
         */
        fun onFrameReady(bitmap: Bitmap) {
            if (isProcessingFrame) return
            if (_uiState.value.phase != RecognitionPhase.IDLE) return
            val now = SystemClock.elapsedRealtime()
            if (now - lastAutoCaptureMs < AUTO_CAPTURE_COOLDOWN_MS) return

            isProcessingFrame = true
            viewModelScope.launch(computationDispatcher) {
                try {
                    val embedding = embedder.embed(bitmap) ?: return@launch
                    // O frame ao vivo só tem embedding; gate compara apenas embedding
                    // (sem aspectRatio/cor espúrios) contra um limiar próprio do preview.
                    val queryFeature =
                        FeatureSet(
                            embedding = embedding,
                            colorLab = null,
                            aspectRatio = null,
                            imprintText = null,
                        )
                    val registeredPhotos = medicationPhotoRepository.getAll()
                    val hasConfidentMatch =
                        registeredPhotos.any { photo ->
                            val refFeature = FeatureSet(embedding = photo.embedding)
                            com.meusremedios.data.ml.RecognitionScorer.score(
                                query = queryFeature,
                                candidate = refFeature,
                            ) >= RecognitionParams.PREVIEW_EMBEDDING_THRESHOLD
                        }
                    if (hasConfidentMatch) {
                        lastAutoCaptureMs = SystemClock.elapsedRealtime()
                        _autoCaptureEvents.trySend(Unit)
                        _uiState.update { it.copy(showCaptureFlash = true) }
                    }
                } finally {
                    isProcessingFrame = false
                }
            }
        }

        /** Chamado pela UI após exibir o flash de auto-captura. */
        fun onCaptureFlashDone() {
            _uiState.update { it.copy(showCaptureFlash = false) }
        }

        private companion object {
            const val MAX_QUERY_PHOTOS = 2
            const val AUTO_CAPTURE_COOLDOWN_MS = 2_000L
        }
    }
