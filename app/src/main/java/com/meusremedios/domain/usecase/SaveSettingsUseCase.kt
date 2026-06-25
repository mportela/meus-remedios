package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.domain.model.AppSettings
import javax.inject.Inject

class SaveSettingsUseCase
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase,
    ) {
        suspend operator fun invoke(newSettings: AppSettings) {
            val current = settingsRepository.get()
            settingsRepository.update(newSettings)

            val remindersChanged =
                current.remindersGlobal != newSettings.remindersGlobal ||
                    current.reminderLeadMinutes != newSettings.reminderLeadMinutes

            if (remindersChanged) {
                rescheduleAllAlarmsUseCase()
            }
        }
    }
