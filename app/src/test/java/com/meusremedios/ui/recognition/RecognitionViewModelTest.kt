package com.meusremedios.ui.recognition

import com.meusremedios.MainDispatcherRule
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.model.RecognitionOutcome
import com.meusremedios.domain.usecase.FakeFeatureExtractor
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationImageStore
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.RecognizeMedicationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RecognitionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val medications = FakeMedicationRepository()
    private val photos = FakeMedicationPhotoRepository()
    private val imageStore = FakeMedicationImageStore()

    private suspend fun seedMatchingMedication(): Long {
        val id = medications.add(Medication(name = "Losartana"))
        photos.add(
            MedicationPhoto(
                medicationId = id,
                filePath = "/files/$id.jpg",
                embedding = null,
                dominantColorLab = floatArrayOf(50f, 0f, 0f),
                aspectRatio = 1f,
                side = PhotoSide.FRONT,
            ),
        )
        return id
    }

    private fun viewModel(): RecognitionViewModel {
        val extractor = FakeFeatureExtractor(
            PhotoFeatures(embedding = null, dominantColorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f),
        )
        val useCase = RecognizeMedicationUseCase(photos, medications, extractor)
        return RecognitionViewModel(imageStore, useCase)
    }

    @Test
    fun `captura confiante leva a resultado com medicamento`() = runTest {
        val id = seedMatchingMedication()
        val vm = viewModel()

        vm.prepareCapture { }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        vm.onCaptured()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(RecognitionPhase.RESULT, state.phase)
        val outcome = state.outcome
        assertTrue(outcome is RecognitionOutcome.Confident)
        assertEquals(id, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }

    @Test
    fun `reiniciar volta ao estado inicial e descarta temporarios`() = runTest {
        seedMatchingMedication()
        val vm = viewModel()
        vm.prepareCapture { }
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
        vm.onCaptured()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.reset()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertEquals(RecognitionPhase.IDLE, vm.uiState.value.phase)
        assertTrue(imageStore.deleted.isNotEmpty())
    }
}
