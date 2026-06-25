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

class MarkIntakeSkippedUseCaseTest {
    private val clock = Clock.fixed(Instant.parse("2026-06-24T10:00:00Z"), ZoneOffset.UTC)
    private val date = LocalDate.of(2026, 6, 24)
    private val scheduledAt = Instant.parse("2026-06-24T08:00:00Z")

    private val repo = FakeIntakeLogRepository()
    private val useCase = MarkIntakeSkippedUseCase(repo, clock)

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
    fun `sem log existente - insere SKIPPED`() =
        runTest {
            useCase(dose, date)
            val log = repo.observeByDate(date).first().single()
            assertEquals(IntakeStatus.SKIPPED, log.status)
            assertNull(log.takenAt)
        }

    @Test
    fun `log SKIPPED existente - deleta (toggle off)`() =
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
            assertEquals(0, repo.observeByDate(date).first().size)
        }

    @Test
    fun `log TAKEN existente - atualiza para SKIPPED e remove takenAt`() =
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
            val log = repo.observeByDate(date).first().single()
            assertEquals(IntakeStatus.SKIPPED, log.status)
            assertNull(log.takenAt)
        }
}
