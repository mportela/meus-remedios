package com.meusremedios.ui.medications.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.SearchMedicationsUseCase
import com.meusremedios.ui.theme.MeusRemediosTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Testes Compose/Robolectric de [MedicationListScreen].
 * Verifica que a tela renderiza medicamentos injetados via fake repository.
 */
@RunWith(RobolectricTestRunner::class)
class MedicationListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val fakeRepo = FakeMedicationRepository()

    private fun viewModel() = MedicationListViewModel(SearchMedicationsUseCase(fakeRepo))

    @Test
    fun listScreen_rendersMedicationsFromRepository() {
        runBlocking {
            fakeRepo.add(Medication(name = "Amoxicilina"))
            fakeRepo.add(Medication(name = "Dorflex"))
        }

        composeRule.setContent {
            MeusRemediosTheme {
                MedicationListScreen(
                    onAddMedication = {},
                    onOpenMedication = {},
                    viewModel = viewModel(),
                )
            }
        }

        composeRule.onNodeWithText("Amoxicilina").assertIsDisplayed()
        composeRule.onNodeWithText("Dorflex").assertIsDisplayed()
    }

    @Test
    fun listScreen_rendersEmptyState_whenNoMedications() {
        composeRule.setContent {
            MeusRemediosTheme {
                MedicationListScreen(
                    onAddMedication = {},
                    onOpenMedication = {},
                    viewModel = viewModel(),
                )
            }
        }

        composeRule.onNodeWithText("Amoxicilina").assertDoesNotExist()
    }
}
