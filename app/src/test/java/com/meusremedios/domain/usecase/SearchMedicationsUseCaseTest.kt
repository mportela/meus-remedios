package com.meusremedios.domain.usecase

import app.cash.turbine.test
import com.meusremedios.domain.model.Medication
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchMedicationsUseCaseTest {
    private lateinit var repository: FakeMedicationRepository
    private lateinit var observe: ObserveMedicationsUseCase
    private lateinit var search: SearchMedicationsUseCase

    @Before
    fun setUp() {
        repository = FakeMedicationRepository()
        observe = ObserveMedicationsUseCase(repository)
        search = SearchMedicationsUseCase(repository)
    }

    @Test
    fun `observe returns medications ordered by name`() =
        runTest {
            repository.add(Medication(name = "Paracetamol"))
            repository.add(Medication(name = "Aspirina"))

            observe().test {
                assertEquals(listOf("Aspirina", "Paracetamol"), awaitItem().map { it.name })
            }
        }

    @Test
    fun `blank query returns full list`() =
        runTest {
            repository.add(Medication(name = "Paracetamol"))
            repository.add(Medication(name = "Aspirina"))

            search("   ").test {
                assertEquals(2, awaitItem().size)
            }
        }

    @Test
    fun `query filters by name case-insensitively`() =
        runTest {
            repository.add(Medication(name = "Paracetamol"))
            repository.add(Medication(name = "Aspirina"))

            search("para").test {
                assertEquals(listOf("Paracetamol"), awaitItem().map { it.name })
            }
        }
}
