package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.notifications.NotificationHelper
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RescheduleAllAlarmsUseCaseTest {

    private lateinit var scheduleRepository: FakeScheduleRepository
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var useCase: RescheduleAllAlarmsUseCase
    private lateinit var scheduleAlarmsForTodayUseCase: ScheduleAlarmsForTodayUseCase

    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-06-24T07:00:00Z"),
        ZoneId.of("UTC"),
    )

    @Before
    fun setUp() {
        scheduleRepository = FakeScheduleRepository()
        medicationRepository = FakeMedicationRepository()
        settingsRepository = FakeSettingsRepository(AppSettings(remindersGlobal = true))
        notificationHelper = mockk(relaxed = true)
        scheduleAlarmsForTodayUseCase = ScheduleAlarmsForTodayUseCase(
            medicationRepository,
            scheduleRepository,
            settingsRepository,
            notificationHelper,
            fixedClock,
        )
        useCase = RescheduleAllAlarmsUseCase(
            scheduleRepository,
            notificationHelper,
            scheduleAlarmsForTodayUseCase,
            fixedClock,
        )
    }

    @Test
    fun `cancels all existing alarms before rescheduling`() = runTest {
        val medId = medicationRepository.add(Medication(name = "A"))
        val schedId1 = scheduleRepository.add(ScheduleTime(medicationId = medId, timeOfDay = LocalTime.of(8, 0)))
        val schedId2 = scheduleRepository.add(ScheduleTime(medicationId = medId, timeOfDay = LocalTime.of(12, 0)))

        useCase()

        verify { notificationHelper.cancelAlarm(schedId1.toInt()) }
        verify { notificationHelper.cancelAlarm(schedId2.toInt()) }
    }

    @Test
    fun `schedules midnight alarm after rescheduling`() = runTest {
        useCase()

        verify { notificationHelper.scheduleMidnightAlarm(any()) }
    }

    @Test
    fun `reschedules active doses after cancelling old ones`() = runTest {
        val medId = medicationRepository.add(Medication(name = "A", remindersEnabled = true))
        scheduleRepository.add(ScheduleTime(medicationId = medId, timeOfDay = LocalTime.of(8, 0)))

        useCase()

        coVerify { notificationHelper.scheduleDoseAlarm(any(), any(), any(), any(), any(), any(), any(), any()) }
    }
}
