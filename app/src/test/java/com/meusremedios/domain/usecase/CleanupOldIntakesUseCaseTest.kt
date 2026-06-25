package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CleanupOldIntakesUseCaseTest {

    private lateinit var intakeLogRepository: FakeIntakeLogRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var useCase: CleanupOldIntakesUseCase

    // "hoje" fixo: 2026-06-24
    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-06-24T12:00:00Z"),
        ZoneId.of("UTC"),
    )

    private fun makeLog(date: LocalDate) = IntakeLog(
        medicationId = 1L,
        date = date,
        scheduledAt = Instant.parse("2026-01-01T08:00:00Z"),
        status = IntakeStatus.TAKEN,
    )

    @Before
    fun setUp() {
        intakeLogRepository = FakeIntakeLogRepository()
        settingsRepository = FakeSettingsRepository(AppSettings(historyRetentionDays = 90))
        useCase = CleanupOldIntakesUseCase(intakeLogRepository, settingsRepository, fixedClock)
    }

    @Test
    fun `remove logs anteriores ao corte`() = runTest {
        // hoje = 2026-06-24; corte = 2026-03-26 (90 dias atrás)
        val cutoff = LocalDate.of(2026, 3, 26)
        intakeLogRepository.seed(
            listOf(
                makeLog(cutoff.minusDays(1)),  // antes do corte → deve ser removido
                makeLog(cutoff),               // exatamente no corte → permanece
                makeLog(LocalDate.of(2026, 6, 24)), // hoje → permanece
            ),
        )

        useCase()

        val remaining = intakeLogRepository.snapshot()
        assertEquals(2, remaining.size)
        assert(remaining.none { it.date.isBefore(cutoff) })
    }

    @Test
    fun `nao remove logs dentro do periodo`() = runTest {
        val today = LocalDate.of(2026, 6, 24)
        val logs = (0..10).map { makeLog(today.minusDays(it.toLong())) }
        intakeLogRepository.seed(logs)

        useCase()

        assertEquals(11, intakeLogRepository.snapshot().size)
    }

    @Test
    fun `respeita configuracao de retencao de 30 dias`() = runTest {
        settingsRepository.update(AppSettings(historyRetentionDays = 30))
        val today = LocalDate.of(2026, 6, 24)
        // corte = 2026-05-25
        intakeLogRepository.seed(
            listOf(
                makeLog(today.minusDays(31)), // deve ser removido
                makeLog(today.minusDays(30)), // permanece (no corte)
                makeLog(today),               // permanece
            ),
        )

        useCase()

        assertEquals(2, intakeLogRepository.snapshot().size)
    }
}
