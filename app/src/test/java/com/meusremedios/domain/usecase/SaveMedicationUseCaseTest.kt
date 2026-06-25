package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.ScheduleTime
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SaveMedicationUseCaseTest {
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var scheduleRepository: FakeScheduleRepository
    private lateinit var rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase
    private lateinit var useCase: SaveMedicationUseCase

    @Before
    fun setUp() {
        medicationRepository = FakeMedicationRepository()
        scheduleRepository = FakeScheduleRepository()
        rescheduleAllAlarmsUseCase = mockk(relaxed = true)
        useCase = SaveMedicationUseCase(medicationRepository, scheduleRepository, rescheduleAllAlarmsUseCase)
    }

    @Test
    fun `rejects blank name`() =
        runTest {
            val result = useCase(Medication(name = "   "), emptyList())

            assertEquals(
                SaveMedicationResult.Invalid(MedicationValidationError.BLANK_NAME),
                result,
            )
            assertTrue(medicationRepository.snapshot().isEmpty())
        }

    @Test
    fun `rejects end date before start date`() =
        runTest {
            val medication =
                Medication(
                    name = "Losartana",
                    periodType = PeriodType.RANGED,
                    startDate = LocalDate.of(2026, 1, 10),
                    endDate = LocalDate.of(2026, 1, 5),
                )

            val result = useCase(medication, emptyList())

            assertEquals(
                SaveMedicationResult.Invalid(MedicationValidationError.END_DATE_BEFORE_START_DATE),
                result,
            )
            assertTrue(medicationRepository.snapshot().isEmpty())
        }

    @Test
    fun `creates medication with trimmed name and schedules`() =
        runTest {
            val result =
                useCase(
                    Medication(name = "  Losartana  "),
                    listOf(ScheduleTime(medicationId = 0, timeOfDay = LocalTime.of(8, 0))),
                )

            assertTrue(result is SaveMedicationResult.Success)
            val saved = medicationRepository.snapshot().single()
            assertEquals("Losartana", saved.name)
            val schedule = scheduleRepository.snapshot().single()
            assertEquals(saved.id, schedule.medicationId)
            assertEquals(LocalTime.of(8, 0), schedule.timeOfDay)
        }

    @Test
    fun `calls RescheduleAllAlarmsUseCase after saving medication`() =
        runTest {
            useCase(Medication(name = "Losartana"), emptyList())

            coVerify { rescheduleAllAlarmsUseCase() }
        }

    @Test
    fun `updates medication and reconciles schedules`() =
        runTest {
            val id = medicationRepository.add(Medication(name = "Losartana"))
            val keep = ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0))
            val remove = ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(20, 0))
            val keepId = scheduleRepository.add(keep)
            scheduleRepository.add(remove)

            val result =
                useCase(
                    Medication(id = id, name = "Losartana 50mg"),
                    listOf(
                        keep.copy(id = keepId, timeOfDay = LocalTime.of(9, 0)),
                        ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(12, 0)),
                    ),
                )

            assertTrue(result is SaveMedicationResult.Success)
            assertEquals("Losartana 50mg", medicationRepository.snapshot().single().name)
            val times = scheduleRepository.snapshot().map { it.timeOfDay }.sorted()
            assertEquals(listOf(LocalTime.of(9, 0), LocalTime.of(12, 0)), times)
        }
}
