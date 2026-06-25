package com.meusremedios.ui.medications.form

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import com.meusremedios.domain.usecase.AddMedicationPhotoUseCase
import com.meusremedios.domain.usecase.DeleteMedicationUseCase
import com.meusremedios.domain.usecase.FakeFeatureExtractor
import com.meusremedios.domain.usecase.FakeMedicationImageStore
import com.meusremedios.domain.usecase.FakeMedicationPhotoRepository
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.FakeScheduleRepository
import com.meusremedios.domain.usecase.GetMedicationUseCase
import com.meusremedios.domain.usecase.ObserveMedicationPhotosUseCase
import com.meusremedios.domain.usecase.RemoveMedicationPhotoUseCase
import com.meusremedios.domain.usecase.SaveMedicationUseCase
import com.meusremedios.ui.navigation.Routes
import com.meusremedios.ui.theme.MeusRemediosTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Testes Compose/Robolectric de [MedicationFormScreen].
 * Verifica que o formulário de cadastro renderiza sem erros com fakes.
 */
@RunWith(RobolectricTestRunner::class)
class MedicationFormScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val medicationRepository = FakeMedicationRepository()
    private val scheduleRepository = FakeScheduleRepository()
    private val photoRepository = FakeMedicationPhotoRepository()
    private val imageStore = FakeMedicationImageStore()

    private fun viewModel() = MedicationFormViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_MEDICATION_ID to 0L)),
        getMedication = GetMedicationUseCase(medicationRepository, scheduleRepository),
        saveMedication = SaveMedicationUseCase(medicationRepository, scheduleRepository, mockk(relaxed = true)),
        deleteMedication = DeleteMedicationUseCase(medicationRepository),
        observeMedicationPhotos = ObserveMedicationPhotosUseCase(photoRepository),
        addMedicationPhoto = AddMedicationPhotoUseCase(imageStore, FakeFeatureExtractor(), photoRepository),
        removeMedicationPhoto = RemoveMedicationPhotoUseCase(imageStore, photoRepository),
        imageStore = imageStore,
    )

    @Test
    fun formScreen_rendersNameField() {
        composeRule.setContent {
            MeusRemediosTheme {
                MedicationFormScreen(
                    onDone = {},
                    viewModel = viewModel(),
                )
            }
        }

        // Campo de nome deve estar visível na tela
        composeRule.onNodeWithText("Nome do remédio").assertIsDisplayed()
    }
}
