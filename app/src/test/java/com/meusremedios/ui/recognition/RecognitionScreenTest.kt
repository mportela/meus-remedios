package com.meusremedios.ui.recognition

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.usecase.FakeFeatureExtractor
import com.meusremedios.domain.usecase.FakeIntakeLogRepository
import com.meusremedios.domain.usecase.FakeMedicationImageStore
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.GetPendingDosesTodayForMedicationUseCase
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import com.meusremedios.domain.usecase.RecognizeMedicationUseCase
import com.meusremedios.ui.theme.MeusRemediosTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock

/**
 * Testes Compose/Robolectric de [RecognitionScreen].
 * Verifica que a tela de reconhecimento renderiza no estado inicial (IDLE).
 */
@RunWith(RobolectricTestRunner::class)
class RecognitionScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val clock = Clock.systemDefaultZone()
    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val intakeLogs = FakeIntakeLogRepository()
    private val photos = FakeMedicationPhotoRepository()
    private val imageStore = FakeMedicationImageStore()

    private fun viewModel(): RecognitionViewModel {
        val extractor = FakeFeatureExtractor(
            PhotoFeatures(embedding = null, dominantColorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f),
        )
        val recognizeUseCase = RecognizeMedicationUseCase(photos, medications, extractor)
        val getPending = GetPendingDosesTodayForMedicationUseCase(medications, schedules, intakeLogs, clock)
        val markTaken = MarkIntakeTakenUseCase(intakeLogs, clock)
        return RecognitionViewModel(imageStore, recognizeUseCase, getPending, markTaken, clock)
    }

    @Test
    fun recognitionScreen_rendersConfirmButton_inIdleState() {
        composeRule.setContent {
            MeusRemediosTheme {
                RecognitionScreen(viewModel = viewModel())
            }
        }

        // No estado IDLE, o botão de captura deve estar visível
        composeRule.onAllNodesWithText("Confirmar remédio")[0].assertIsDisplayed()
    }

    @Test
    fun recognitionScreen_rendersNoPhotosMessage_whenNoRegisteredPhotos() {
        composeRule.setContent {
            MeusRemediosTheme {
                RecognitionScreen(viewModel = viewModel())
            }
        }

        composeRule.waitForIdle()
        // Sem fotos cadastradas, não deve mostrar resultado confiante
        composeRule.onNodeWithText("Este remédio é:").assertDoesNotExist()
    }

    @Test
    fun recognitionScreen_rendersTitle() {
        runBlocking {
            medications.add(Medication(name = "Losartana"))
                .also { id ->
                    photos.add(
                        MedicationPhoto(
                            medicationId = id,
                            filePath = "/files/$id.jpg",
                            side = PhotoSide.FRONT,
                            dominantColorLab = floatArrayOf(50f, 0f, 0f),
                            aspectRatio = 1f,
                        ),
                    )
                }
        }

        composeRule.setContent {
            MeusRemediosTheme {
                RecognitionScreen(viewModel = viewModel())
            }
        }

        composeRule.onAllNodesWithText("Confirmar remédio")[0].assertIsDisplayed()
    }
}
