package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.SettingsRepository
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class CleanupOldIntakesUseCase
    @Inject
    constructor(
        private val intakeLogRepository: IntakeLogRepository,
        private val settingsRepository: SettingsRepository,
        private val clock: Clock,
    ) {
        suspend operator fun invoke() {
            val settings = settingsRepository.get()
            val today = LocalDate.now(clock)
            val cutoff = today.minusDays(settings.historyRetentionDays.toLong())
            intakeLogRepository.deleteOlderThan(cutoff)
        }
    }
