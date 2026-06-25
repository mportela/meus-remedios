package com.meusremedios.domain.usecase

import app.cash.turbine.test
import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ObserveMedicationDetailUseCaseTest {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val today: LocalDate = LocalDate.of(2026, 6, 22)
    private val nowInstant: Instant =
        LocalDateTime.of(today, LocalTime.NOON).atZone(zone).toInstant()
    private val clock: Clock = Clock.fixed(nowInstant, zone)

    private val medications = FakeMedicationRepository()
    private val schedules = FakeScheduleRepository()
    private val photos = FakeMedicationPhotoRepository()
    private val intakeLogs = FakeIntakeLogRepository()

    private fun useCase(settings: AppSettings = AppSettings()) =
        ObserveMedicationDetailUseCase(
            medications,
            schedules,
            photos,
            intakeLogs,
            FakeSettingsRepository(settings),
            clock,
        )

    @Test
    fun `agrega medicamento horarios e historico dentro da retencao`() =
        runTest {
            val id = medications.add(Medication(name = "Losartana"))
            schedules.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))
            intakeLogs.seed(
                listOf(
                    IntakeLog(
                        id = 1,
                        medicationId = id,
                        date = today.minusDays(5),
                        scheduledAt = nowInstant,
                        status = IntakeStatus.TAKEN,
                    ),
                    IntakeLog(
                        id = 2,
                        medicationId = id,
                        date = today.minusDays(200),
                        scheduledAt = nowInstant,
                        status = IntakeStatus.TAKEN,
                    ),
                ),
            )

            useCase(AppSettings(historyRetentionDays = 90)).invoke(id).test {
                val detail = awaitItem()!!
                assertEquals("Losartana", detail.medication.name)
                assertEquals(1, detail.schedules.size)
                // O registro de 200 dias atrás é omitido pela retenção de 90 dias.
                assertEquals(1, detail.history.size)
                assertEquals(today.minusDays(5), detail.history.single().date)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `retorna nulo quando medicamento nao existe`() =
        runTest {
            useCase().invoke(999L).test {
                assertNull(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
