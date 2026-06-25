package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.AppSettings
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveSettingsUseCaseTest {

    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase
    private lateinit var useCase: SaveSettingsUseCase

    @Before
    fun setUp() {
        settingsRepository = FakeSettingsRepository(AppSettings(remindersGlobal = true, reminderLeadMinutes = 5))
        rescheduleAllAlarmsUseCase = mockk(relaxed = true)
        useCase = SaveSettingsUseCase(settingsRepository, rescheduleAllAlarmsUseCase)
    }

    @Test
    fun `chama reschedule quando remindersGlobal muda`() = runTest {
        val newSettings = AppSettings(remindersGlobal = false, reminderLeadMinutes = 5)

        useCase(newSettings)

        coVerify(exactly = 1) { rescheduleAllAlarmsUseCase() }
    }

    @Test
    fun `chama reschedule quando reminderLeadMinutes muda`() = runTest {
        val newSettings = AppSettings(remindersGlobal = true, reminderLeadMinutes = 15)

        useCase(newSettings)

        coVerify(exactly = 1) { rescheduleAllAlarmsUseCase() }
    }

    @Test
    fun `nao chama reschedule quando apenas autoCapture muda`() = runTest {
        val newSettings = AppSettings(remindersGlobal = true, reminderLeadMinutes = 5, autoCapture = true)

        useCase(newSettings)

        coVerify(exactly = 0) { rescheduleAllAlarmsUseCase() }
    }

    @Test
    fun `persiste as novas configuracoes`() = runTest {
        val newSettings = AppSettings(historyRetentionDays = 180, autoCapture = true)

        useCase(newSettings)

        assert(settingsRepository.get() == newSettings)
    }
}
