package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.domain.model.Medication
import com.meusremedios.notifications.NotificationHelper
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class ScheduleAlarmsForTodayUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        val settings = settingsRepository.get()
        if (!settings.remindersGlobal) return

        val today = LocalDate.now(clock)
        val weekdayBit = 1 shl (today.dayOfWeek.value - 1)
        val nowMillis = clock.millis()

        val medications = medicationRepository.observeAll().first()
        val schedules = scheduleRepository.getAll()
        val activeMedications = medications
            .filter { it.remindersEnabled && it.isActiveOn(today) }
            .associateBy { it.id }

        for (schedule in schedules) {
            val medication = activeMedications[schedule.medicationId] ?: continue
            if (schedule.daysOfWeekMask and weekdayBit == 0) continue

            val triggerAt = LocalDateTime.of(today, schedule.timeOfDay)
                .minusMinutes(settings.reminderLeadMinutes.toLong())
                .atZone(clock.zone)
                .toInstant()
                .toEpochMilli()

            if (triggerAt <= nowMillis) continue

            val timeLabel = schedule.timeOfDay.toString()
            val requestCode = schedule.id.toInt()
            val scheduledAtIso = LocalDateTime.of(today, schedule.timeOfDay)
                .toInstant(ZoneOffset.UTC)
                .toString()

            notificationHelper.scheduleDoseAlarm(
                requestCode = requestCode,
                triggerAtMillis = triggerAt,
                medicationId = medication.id,
                medicationName = medication.name,
                scheduleTimeId = schedule.id,
                timeLabel = timeLabel,
                scheduledAt = scheduledAtIso,
                date = today.toString(),
            )
        }
    }

    private fun Medication.isActiveOn(date: LocalDate): Boolean {
        val afterStart = startDate?.let { !date.isBefore(it) } ?: true
        val beforeEnd = endDate?.let { !date.isAfter(it) } ?: true
        return afterStart && beforeEnd
    }
}
