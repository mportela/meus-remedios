package com.meusremedios.ui.medications.list

import app.cash.turbine.test
import com.meusremedios.MainDispatcherRule
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.usecase.FakeMedicationRepository
import com.meusremedios.domain.usecase.SearchMedicationsUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MedicationListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMedicationRepository()

    private fun viewModel() = MedicationListViewModel(SearchMedicationsUseCase(repository))

    @Test
    fun `emits all medications ordered by name`() = runTest {
        repository.add(Medication(name = "Paracetamol"))
        repository.add(Medication(name = "Aspirina"))

        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(listOf("Aspirina", "Paracetamol"), state.medications.map { it.name })
        }
    }

    @Test
    fun `filters medications by query`() = runTest {
        repository.add(Medication(name = "Paracetamol"))
        repository.add(Medication(name = "Aspirina"))
        val vm = viewModel()

        vm.uiState.test {
            awaitItem() // estado inicial
            vm.onQueryChange("para")
            val filtered = awaitItem()
            assertEquals(listOf("Paracetamol"), filtered.medications.map { it.name })
        }
    }
}
