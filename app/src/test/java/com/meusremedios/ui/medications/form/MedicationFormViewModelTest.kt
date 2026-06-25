package com.meusremedios.ui.medications.form

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.meusremedios.MainDispatcherRule
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.usecase.AddMedicationPhotoUseCase
import com.meusremedios.domain.usecase.CheckPhotoCollisionUseCase
import com.meusremedios.domain.usecase.DeleteMedicationUseCase
import com.meusremedios.domain.usecase.FakeFeatureExtractor
import com.meusremedios.domain.usecase.FakeMedicationImageStore
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.GetMedicationUseCase
import com.meusremedios.domain.usecase.MedicationValidationError
import com.meusremedios.domain.usecase.ObserveMedicationPhotosUseCase
import com.meusremedios.domain.usecase.RemoveMedicationPhotoUseCase
import com.meusremedios.domain.usecase.SaveMedicationUseCase
import com.meusremedios.ui.navigation.Routes
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class MedicationFormViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val medicationRepository = FakeMedicationRepository()
    private val scheduleRepository = FakeScheduleRepository()
    private val photoRepository = FakeMedicationPhotoRepository()
    private val imageStore = FakeMedicationImageStore()

    private fun viewModel(
        id: Long = 0L,
        featureExtractor: FakeFeatureExtractor = FakeFeatureExtractor(),
    ): MedicationFormViewModel =
        MedicationFormViewModel(
            savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_MEDICATION_ID to id)),
            getMedication = GetMedicationUseCase(medicationRepository, scheduleRepository),
            saveMedication = SaveMedicationUseCase(medicationRepository, scheduleRepository, mockk(relaxed = true)),
            deleteMedication = DeleteMedicationUseCase(medicationRepository),
            observeMedicationPhotos = ObserveMedicationPhotosUseCase(photoRepository),
            addMedicationPhoto =
                AddMedicationPhotoUseCase(
                    imageStore,
                    FakeFeatureExtractor(),
                    photoRepository,
                ),
            removeMedicationPhoto = RemoveMedicationPhotoUseCase(imageStore, photoRepository),
            imageStore = imageStore,
            checkPhotoCollision = CheckPhotoCollisionUseCase(photoRepository, medicationRepository),
            featureExtractor = featureExtractor,
        )

    @Test
    fun `save with blank name sets validation error`() =
        runTest {
            val vm = viewModel()

            vm.save()

            assertEquals(
                MedicationValidationError.BLANK_NAME,
                vm.uiState.value.validationError,
            )
            assertTrue(medicationRepository.snapshot().isEmpty())
        }

    @Test
    fun `save valid medication emits Saved event and persists`() =
        runTest {
            val vm = viewModel()
            vm.onNameChange("Losartana")
            vm.addSchedule(LocalTime.of(8, 0))

            vm.events.test {
                vm.save()
                assertEquals(MedicationFormEvent.Saved, awaitItem())
            }
            assertEquals("Losartana", medicationRepository.snapshot().single().name)
            assertEquals(1, scheduleRepository.snapshot().size)
        }

    @Test
    fun `loads existing medication for editing`() =
        runTest {
            val id = medicationRepository.add(Medication(name = "Aspirina"))
            scheduleRepository.add(
                com.meusremedios.domain.model.ScheduleTime(
                    medicationId = id,
                    timeOfDay = LocalTime.of(7, 30),
                ),
            )

            val vm = viewModel(id)

            val state = vm.uiState.value
            assertEquals("Aspirina", state.name)
            assertTrue(state.isEditing)
            assertEquals(1, state.schedules.size)
        }

    @Test
    fun `switching to continuous clears dates`() =
        runTest {
            val vm = viewModel()
            vm.onPeriodTypeChange(PeriodType.RANGED)
            vm.onStartDateChange(LocalDate.of(2026, 1, 1))
            vm.onEndDateChange(LocalDate.of(2026, 2, 1))

            vm.onPeriodTypeChange(PeriodType.CONTINUOUS)

            assertNull(vm.uiState.value.startDate)
            assertNull(vm.uiState.value.endDate)
        }

    @Test
    fun `save with pending photo similar to existing emits CollisionWarning`() =
        runTest {
            // Seed: medicamento já cadastrado com embedding idêntico
            val embedding = FloatArray(4) { 1f }
            val existingId = medicationRepository.add(Medication(name = "Caltrat"))
            photoRepository.add(
                MedicationPhoto(
                    medicationId = existingId,
                    filePath = "/f/existing.jpg",
                    side = PhotoSide.FRONT,
                    embedding = embedding,
                    dominantColorLab = floatArrayOf(50f, 0f, 0f),
                    aspectRatio = 1f,
                ),
            )

            // Extrator retorna embedding idêntico para a pending photo
            val extractor =
                FakeFeatureExtractor(
                    PhotoFeatures(
                        embedding = embedding,
                        dominantColorLab = floatArrayOf(50f, 0f, 0f),
                        aspectRatio = 1f,
                    ),
                )
            val vm = viewModel(featureExtractor = extractor)
            vm.onNameChange("Novo Remédio")
            vm.addPendingPhoto("/tmp/staged.jpg", PhotoSide.FRONT)

            vm.events.test {
                vm.save()
                val event = awaitItem()
                assertTrue(event is MedicationFormEvent.CollisionWarning)
                assertEquals("Caltrat", (event as MedicationFormEvent.CollisionWarning).candidateName)
                cancelAndIgnoreRemainingEvents()
            }
            // Medicamento NÃO deve ter sido persistido
            assertEquals(1, medicationRepository.snapshot().size) // apenas o Caltrat existente
        }

    @Test
    fun `saveIgnoringCollision persists after collision warning`() =
        runTest {
            val embedding = FloatArray(4) { 1f }
            val existingId = medicationRepository.add(Medication(name = "Caltrat"))
            photoRepository.add(
                MedicationPhoto(
                    medicationId = existingId,
                    filePath = "/f/existing.jpg",
                    side = PhotoSide.FRONT,
                    embedding = embedding,
                    dominantColorLab = floatArrayOf(50f, 0f, 0f),
                    aspectRatio = 1f,
                ),
            )
            val extractor =
                FakeFeatureExtractor(
                    PhotoFeatures(
                        embedding = embedding,
                        dominantColorLab = floatArrayOf(50f, 0f, 0f),
                        aspectRatio = 1f,
                    ),
                )
            val vm = viewModel(featureExtractor = extractor)
            vm.onNameChange("Novo Remédio")
            vm.addPendingPhoto("/tmp/staged.jpg", PhotoSide.FRONT)

            vm.events.test {
                vm.save() // emite CollisionWarning
                awaitItem() // CollisionWarning descartado
                vm.saveIgnoringCollision() // deve persistir
                assertEquals(MedicationFormEvent.Saved, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(2, medicationRepository.snapshot().size)
        }
}
