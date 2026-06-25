package com.meusremedios.notifications

import android.content.Context
import android.content.Intent
import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.ScheduledDose
import com.meusremedios.domain.usecase.MarkIntakeTakenUseCase
import com.meusremedios.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** Lógica de negócio das ações de notificação, separada do BroadcastReceiver para testabilidade. */
@Singleton
class NotificationActionHandler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val markIntakeTakenUseCase: MarkIntakeTakenUseCase,
        private val notificationHelper: NotificationHelper,
    ) {
        suspend fun handleMarkTaken(
            medicationId: Long,
            medicationName: String,
            scheduleTimeId: Long,
            timeLabel: String,
            scheduledAtIso: String,
            dateIso: String,
            notificationId: Int,
        ) {
            val date = LocalDate.parse(dateIso)
            if (scheduleTimeId == -1L) {
                markIntakeTakenUseCase(medicationId, date)
            } else {
                val dose =
                    ScheduledDose(
                        medicationId = medicationId,
                        medicationName = medicationName,
                        scheduleTimeId = scheduleTimeId,
                        time = LocalTime.parse(timeLabel),
                        scheduledAt = runCatching { Instant.parse(scheduledAtIso) }.getOrElse { Instant.now() },
                        status = DoseStatus.PENDING,
                    )
                markIntakeTakenUseCase(dose, date)
            }
            notificationHelper.cancel(notificationId)
        }

        fun handleSnooze(
            notificationId: Int,
            medicationId: Long,
            medicationName: String,
            scheduleTimeId: Long,
            timeLabel: String,
            scheduledAt: String,
            date: String,
        ) {
            val triggerAtMillis = System.currentTimeMillis() + SNOOZE_DURATION_MS
            notificationHelper.scheduleDoseAlarm(
                requestCode = notificationId,
                triggerAtMillis = triggerAtMillis,
                medicationId = medicationId,
                medicationName = medicationName,
                scheduleTimeId = scheduleTimeId,
                timeLabel = timeLabel,
                scheduledAt = scheduledAt,
                date = date,
            )
            notificationHelper.cancel(notificationId)
        }

        fun handleOpenRecognition(notificationId: Int) {
            notificationHelper.cancel(notificationId)
            val activityIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(MainActivity.NAVIGATE_TO_RECOGNITION, true)
                }
            context.startActivity(activityIntent)
        }

        companion object {
            private const val SNOOZE_DURATION_MS = 2 * 60 * 1000L
        }
    }
