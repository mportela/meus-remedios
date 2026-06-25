package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.notifications.NotificationHelper
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class RescheduleAllAlarmsUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val notificationHelper: NotificationHelper,
    private val scheduleAlarmsForTodayUseCase: ScheduleAlarmsForTodayUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        val schedules = scheduleRepository.getAll()
        schedules.forEach { schedule ->
            notificationHelper.cancelAlarm(schedule.id.toInt())
        }
        scheduleAlarmsForTodayUseCase()
        scheduleMidnightAlarm()
    }

    private fun scheduleMidnightAlarm() {
        val tomorrow = LocalDate.now(clock).plusDays(1)
        val triggerAtMillis = tomorrow.atTime(LocalTime.of(0, 1))
            .atZone(clock.zone)
            .toInstant()
            .toEpochMilli()
        notificationHelper.scheduleMidnightAlarm(triggerAtMillis)
    }
}
