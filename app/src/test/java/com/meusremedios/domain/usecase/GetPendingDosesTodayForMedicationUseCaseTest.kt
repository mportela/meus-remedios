package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class GetPendingDosesTodayForMedicationUseCaseTest {

    private val date = LocalDate.of(2026, 6, 24) // quarta-feira (dayOfWeek=3, bit 2)
    private val clock = Clock.fixed(
        LocalDateTime.of(date, LocalTime.of(10, 0)).toInstant(ZoneOffset.UTC),
        ZoneOffset.UTC,
    )

    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val intakeLogs = FakeIntakeLogRepository()

    private val useCase = GetPendingDosesTodayForMedicationUseCase(
        medicationRepository = medications,
        scheduleRepository = schedules,
        intakeLogRepository = intakeLogs,
        clock = clock,
    )

    @Test
    fun `medicamento com 1 dose hoje sem log - retorna 1 dose`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

        val result = useCase(id)

        assertEquals(1, result.size)
        assertEquals(id, result.single().medicationId)
    }

    @Test
    fun `medicamento com dose TAKEN hoje - retorna 0 doses`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        val schedId = schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        intakeLogs.seed(
            listOf(
                IntakeLog(
                    id = 1L,
                    medicationId = id,
                    scheduleTimeId = schedId,
                    date = date,
                    scheduledAt = Instant.parse("2026-06-24T08:00:00Z"),
                    takenAt = Instant.parse("2026-06-24T08:05:00Z"),
                    status = IntakeStatus.TAKEN,
                ),
            ),
        )

        val result = useCase(id)

        assertEquals(0, result.size)
    }

    @Test
    fun `medicamento sem horarios hoje - retorna 0 doses`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        // horário só na segunda (bit 0), mas hoje é quarta (bit 2)
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0), daysOfWeekMask = 0b000_0001))

        val result = useCase(id)

        assertEquals(0, result.size)
    }
}
