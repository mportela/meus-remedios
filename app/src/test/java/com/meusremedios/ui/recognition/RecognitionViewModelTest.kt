package com.meusremedios.ui.recognition

import android.net.Uri
import com.meusremedios.MainDispatcherRule
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.model.RecognitionOutcome
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.usecase.FakeFeatureExtractor
import com.meusremedios.domain.usecase.FakeIntakeLogRepository
import com.meusremedios.domain.usecase.FakeMedicationImageStore
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.GetPendingDosesTodayForMedicationUseCase
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import com.meusremedios.domain.usecase.RecognizeMedicationUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class RecognitionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 6, 24)
    private val clock = Clock.fixed(
        LocalDateTime.of(date, LocalTime.of(10, 0)).toInstant(ZoneOffset.UTC),
        ZoneOffset.UTC,
    )

    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val intakeLogs = FakeIntakeLogRepository()
    private val photos = FakeMedicationPhotoRepository()
    private val imageStore = FakeMedicationImageStore()

    private suspend fun seedMatchingMedication(addSchedule: Boolean = false): Long {
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
        if (addSchedule) {
            schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        }
        return id
    }

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
    fun `captura confiante leva a resultado com medicamento`() = runTest {
        val id = seedMatchingMedication(addSchedule = false)
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

    @Test
    fun `imagem da galeria leva a resultado com medicamento`() = runTest {
        val id = seedMatchingMedication()
        val vm = viewModel()

        vm.onGalleryPicked(Uri.parse("content://test/foto.jpg"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(RecognitionPhase.RESULT, state.phase)
        val outcome = state.outcome
        assertTrue(outcome is RecognitionOutcome.Confident)
        assertEquals(id, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `markTakenFromRecognition com 0 doses - registra ad-hoc e intakeRegistered true`() = runTest {
        val id = seedMatchingMedication(addSchedule = false) // sem horários
        val vm = viewModel()
        vm.onGalleryPicked(Uri.parse("content://test/foto.jpg"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.markTakenFromRecognition()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.intakeRegistered)
        val log = intakeLogs.observeByDate(date).first().singleOrNull()
        assertEquals(id, log?.medicationId)
        assertTrue(log?.scheduleTimeId == null)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `markTakenFromRecognition com 1 dose - marca direto e intakeRegistered true`() = runTest {
        val id = seedMatchingMedication(addSchedule = true)
        val vm = viewModel()
        vm.onGalleryPicked(Uri.parse("content://test/foto.jpg"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.markTakenFromRecognition()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.intakeRegistered)
        assertEquals(1, intakeLogs.observeByDate(date).first().size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `markTakenFromRecognition com N doses - popula pendingDosesToday sem marcar`() = runTest {
        val id = seedMatchingMedication(addSchedule = false)
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(14, 0)))
        val vm = viewModel()
        vm.onGalleryPicked(Uri.parse("content://test/foto.jpg"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        vm.markTakenFromRecognition()
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.intakeRegistered)
        assertEquals(2, vm.uiState.value.pendingDosesToday.size)
        assertEquals(0, intakeLogs.observeByDate(date).first().size)
    }
}
