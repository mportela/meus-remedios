package com.meusremedios.domain.usecase

import app.cash.turbine.test
import com.meusremedios.domain.model.DayPeriod
import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ObserveDailyReportUseCaseTest {

    private val zone: ZoneId = ZoneId.systemDefault()
    private val date: LocalDate = LocalDate.of(2026, 6, 22)
    private val nowInstant: Instant =
        LocalDateTime.of(date, LocalTime.of(10, 0)).atZone(zone).toInstant()
    private val clock: Clock = Clock.fixed(nowInstant, zone)

    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val intakeLogs = FakeIntakeLogRepository()

    private fun useCase() = ObserveDailyReportUseCase(medications, schedules, intakeLogs, clock)

    @Test
    fun `deriva status atrasado e pendente conforme horario`() = runTest {
        val id = medications.add(Medication(name = "Losartana"))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(20, 0)))

        useCase().invoke(date).test {
            val report = awaitItem()
            assertEquals(2, report.doses.size)
            assertEquals(DoseStatus.LATE, report.doses[0].status)
            assertEquals(DoseStatus.PENDING, report.doses[1].status)
            assertEquals(1, report.lateCount)
            assertEquals(1, report.pendingCount)
            assertEquals(0, report.takenCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dose com registro tomado aparece como tomada`() = runTest {
        val id = medications.add(Medication(name = "AAS"))
        val scheduleId = schedules.add(
            ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)),
        )
        intakeLogs.seed(
            listOf(
                IntakeLog(
                    id = 1,
                    medicationId = id,
                    scheduleTimeId = scheduleId,
                    date = date,
                    scheduledAt = nowInstant,
                    takenAt = nowInstant,
                    status = IntakeStatus.TAKEN,
                ),
            ),
        )

        useCase().invoke(date).test {
            val report = awaitItem()
            assertEquals(DoseStatus.TAKEN, report.doses.single().status)
            assertEquals(1, report.takenCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `agrupa doses por periodo do dia`() = runTest {
        val id = medications.add(Medication(name = "Vitamina"))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(7, 0)))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(13, 0)))
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(21, 0)))

        useCase().invoke(date).test {
            val report = awaitItem()
            val periods = report.byPeriod().map { it.first }
            assertEquals(listOf(DayPeriod.MORNING, DayPeriod.AFTERNOON, DayPeriod.NIGHT), periods)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `exclui horario que nao cai no dia da semana`() = runTest {
        val id = medications.add(Medication(name = "Antibiótico"))
        val otherDayBit = 1 shl (date.dayOfWeek.value % 7) // bit de outro dia
        schedules.add(
            ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0), daysOfWeekMask = otherDayBit),
        )

        useCase().invoke(date).test {
            val report = awaitItem()
            assertTrue(report.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `exclui medicamento fora do intervalo de uso`() = runTest {
        val id = medications.add(
            Medication(name = "Curativo", startDate = date.plusDays(1)),
        )
        schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

        useCase().invoke(date).test {
            val report = awaitItem()
            assertTrue(report.isEmpty)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
