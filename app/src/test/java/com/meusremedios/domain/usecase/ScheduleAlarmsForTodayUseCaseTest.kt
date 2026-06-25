package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.notifications.NotificationHelper
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class ScheduleAlarmsForTodayUseCaseTest {
    private lateinit var medicationRepository: FakeMedicationRepository
    private lateinit var scheduleRepository: FakeScheduleRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var useCase: ScheduleAlarmsForTodayUseCase

    /** Clock fixado em 2026-06-24T07:00:00Z — antes dos horários de teste (08:00 e 12:00). */
    private val fixedClock: Clock =
        Clock.fixed(
            Instant.parse("2026-06-24T07:00:00Z"),
            ZoneId.of("UTC"),
        )

    @Before
    fun setUp() {
        medicationRepository = FakeMedicationRepository()
        scheduleRepository = FakeScheduleRepository()
        settingsRepository = FakeSettingsRepository(AppSettings(remindersGlobal = true, reminderLeadMinutes = 0))
        notificationHelper = mockk(relaxed = true)
        useCase =
            ScheduleAlarmsForTodayUseCase(
                medicationRepository,
                scheduleRepository,
                settingsRepository,
                notificationHelper,
                fixedClock,
            )
    }

    @Test
    fun `schedules alarm for active medication with reminders enabled`() =
        runTest {
            val id = medicationRepository.add(Medication(name = "Losartana", remindersEnabled = true))
            scheduleRepository.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

            useCase()

            coVerify(exactly = 1) { notificationHelper.scheduleDoseAlarm(any(), any(), any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun `skips scheduling when remindersGlobal is false`() =
        runTest {
            settingsRepository.update(AppSettings(remindersGlobal = false))
            val id = medicationRepository.add(Medication(name = "Losartana", remindersEnabled = true))
            scheduleRepository.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

            useCase()

            verify(exactly = 0) { notificationHelper.scheduleDoseAlarm(any(), any(), any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun `skips scheduling when medication remindersEnabled is false`() =
        runTest {
            val id = medicationRepository.add(Medication(name = "Losartana", remindersEnabled = false))
            scheduleRepository.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(8, 0)))

            useCase()

            verify(exactly = 0) { notificationHelper.scheduleDoseAlarm(any(), any(), any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun `skips scheduling when time has already passed`() =
        runTest {
            val id = medicationRepository.add(Medication(name = "Losartana", remindersEnabled = true))
            // 06:00 < fixedClock 07:00 → already passed
            scheduleRepository.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(6, 0)))

            useCase()

            verify(exactly = 0) { notificationHelper.scheduleDoseAlarm(any(), any(), any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun `respects reminderLeadMinutes offset`() =
        runTest {
            settingsRepository.update(AppSettings(remindersGlobal = true, reminderLeadMinutes = 30))
            val id = medicationRepository.add(Medication(name = "Losartana", remindersEnabled = true))
            // Dose at 07:20 → trigger at 06:50 (before clock 07:00) → should be skipped
            scheduleRepository.add(ScheduleTime(medicationId = id, timeOfDay = LocalTime.of(7, 20)))

            useCase()

            verify(exactly = 0) { notificationHelper.scheduleDoseAlarm(any(), any(), any(), any(), any(), any(), any(), any()) }
        }
}
