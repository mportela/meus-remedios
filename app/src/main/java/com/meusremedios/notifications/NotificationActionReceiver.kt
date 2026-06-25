package com.meusremedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Processa as ações de "Tomei", "Confirmar comprimido" e "Lembrar em 2 min" das notificações. */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var handler: NotificationActionHandler

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, 0)

        when (intent.action) {
            ACTION_MARK_TAKEN -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        handler.handleMarkTaken(
                            medicationId = intent.getLongExtra(AlarmReceiver.EXTRA_MEDICATION_ID, -1L),
                            medicationName = intent.getStringExtra(AlarmReceiver.EXTRA_MEDICATION_NAME) ?: "",
                            scheduleTimeId = intent.getLongExtra(AlarmReceiver.EXTRA_SCHEDULE_TIME_ID, -1L),
                            timeLabel = intent.getStringExtra(AlarmReceiver.EXTRA_TIME_LABEL) ?: "00:00",
                            scheduledAtIso = intent.getStringExtra(AlarmReceiver.EXTRA_SCHEDULED_AT) ?: "",
                            dateIso = intent.getStringExtra(AlarmReceiver.EXTRA_DATE) ?: "",
                            notificationId = notificationId,
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_SNOOZE -> handler.handleSnooze(
                notificationId = notificationId,
                medicationId = intent.getLongExtra(AlarmReceiver.EXTRA_MEDICATION_ID, -1L),
                medicationName = intent.getStringExtra(AlarmReceiver.EXTRA_MEDICATION_NAME) ?: "",
                scheduleTimeId = intent.getLongExtra(AlarmReceiver.EXTRA_SCHEDULE_TIME_ID, -1L),
                timeLabel = intent.getStringExtra(AlarmReceiver.EXTRA_TIME_LABEL) ?: "00:00",
                scheduledAt = intent.getStringExtra(AlarmReceiver.EXTRA_SCHEDULED_AT) ?: "",
                date = intent.getStringExtra(AlarmReceiver.EXTRA_DATE) ?: "",
            )
            ACTION_OPEN_RECOGNITION -> handler.handleOpenRecognition(notificationId)
        }
    }

    companion object {
        const val ACTION_MARK_TAKEN = "com.meusremedios.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.meusremedios.ACTION_SNOOZE"
        const val ACTION_OPEN_RECOGNITION = "com.meusremedios.ACTION_OPEN_RECOGNITION"
    }
}
