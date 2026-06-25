package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.ScheduledDose
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class MarkIntakeTakenUseCaseTest {
    private val clock = Clock.fixed(Instant.parse("2026-06-24T10:00:00Z"), ZoneOffset.UTC)
    private val date = LocalDate.of(2026, 6, 24)
    private val scheduledAt = Instant.parse("2026-06-24T08:00:00Z")

    private val repo = FakeIntakeLogRepository()
    private val useCase = MarkIntakeTakenUseCase(repo, clock)

    private val dose =
        ScheduledDose(
            medicationId = 1L,
            medicationName = "Losartana",
            scheduleTimeId = 10L,
            time = LocalTime.of(8, 0),
            scheduledAt = scheduledAt,
            status = DoseStatus.PENDING,
        )

    @Test
    fun `sem log existente - insere TAKEN`() =
        runTest {
            useCase(dose, date)

            val logs = repo.snapshot()
            assertEquals(1, logs.size)
            assertEquals(IntakeStatus.TAKEN, logs.single().status)
            assertEquals(1L, logs.single().medicationId)
            assertEquals(10L, logs.single().scheduleTimeId)
        }

    @Test
    fun `log TAKEN existente - deleta (toggle off)`() =
        runTest {
            repo.seed(
                listOf(
                    IntakeLog(
                        id = 1L,
                        medicationId = 1L,
                        scheduleTimeId = 10L,
                        date = date,
                        scheduledAt = scheduledAt,
                        takenAt = Instant.parse("2026-06-24T08:05:00Z"),
                        status = IntakeStatus.TAKEN,
                    ),
                ),
            )
            useCase(dose, date)
            assertEquals(0, repo.snapshot().size)
        }

    @Test
    fun `log SKIPPED existente - atualiza para TAKEN`() =
        runTest {
            repo.seed(
                listOf(
                    IntakeLog(
                        id = 1L,
                        medicationId = 1L,
                        scheduleTimeId = 10L,
                        date = date,
                        scheduledAt = scheduledAt,
                        status = IntakeStatus.SKIPPED,
                    ),
                ),
            )
            useCase(dose, date)
            val log = repo.snapshot().single()
            assertEquals(IntakeStatus.TAKEN, log.status)
            assertEquals(Instant.now(clock), log.takenAt)
        }

    @Test
    fun `sobrecarga ad-hoc - insere com scheduleTimeId nulo`() =
        runTest {
            useCase(medicationId = 42L, date = date)
            val log = repo.snapshot().single()
            assertEquals(42L, log.medicationId)
            assertNull(log.scheduleTimeId)
            assertEquals(IntakeStatus.TAKEN, log.status)
        }

    private suspend fun FakeIntakeLogRepository.snapshot(): List<IntakeLog> = observeByDate(date).first()
}
