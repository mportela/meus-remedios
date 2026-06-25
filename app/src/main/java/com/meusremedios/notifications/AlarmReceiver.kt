package com.meusremedios.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.meusremedios.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Recebe alarmes exatos e exibe a notificação de lembrete de dose. */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: return
        val timeLabel = intent.getStringExtra(EXTRA_TIME_LABEL) ?: return
        val scheduleTimeId = intent.getLongExtra(EXTRA_SCHEDULE_TIME_ID, -1L)
        val scheduledAt = intent.getStringExtra(EXTRA_SCHEDULED_AT) ?: ""
        val date = intent.getStringExtra(EXTRA_DATE) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, medicationId.toInt())

        val takenPi = buildActionPendingIntent(
            context, notificationId, notificationId + 1,
            NotificationActionReceiver.ACTION_MARK_TAKEN,
            medicationId, medicationName, scheduleTimeId, timeLabel, scheduledAt, date,
        )
        val snoozePi = buildActionPendingIntent(
            context, notificationId, notificationId + 2,
            NotificationActionReceiver.ACTION_SNOOZE,
            medicationId, medicationName, scheduleTimeId, timeLabel, scheduledAt, date,
        )
        val confirmPi = buildOpenRecognitionPendingIntent(context, notificationId)

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_dose_title))
            .setContentText(
                context.getString(R.string.notification_dose_text, medicationName, timeLabel),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.notification_action_taken), takenPi)
            .addAction(0, context.getString(R.string.notification_action_confirm), confirmPi)
            .addAction(0, context.getString(R.string.notification_action_snooze), snoozePi)
            .build()

        notificationHelper.notify(notificationId, notification)
    }

    private fun buildActionPendingIntent(
        context: Context,
        notificationId: Int,
        requestCode: Int,
        action: String,
        medicationId: Long,
        medicationName: String,
        scheduleTimeId: Long,
        timeLabel: String,
        scheduledAt: String,
        date: String,
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_SCHEDULE_TIME_ID, scheduleTimeId)
            putExtra(EXTRA_TIME_LABEL, timeLabel)
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(EXTRA_DATE, date)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildOpenRecognitionPendingIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_OPEN_RECOGNITION
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            notificationId + 3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_TIME_LABEL = "extra_time_label"
        const val EXTRA_SCHEDULE_TIME_ID = "extra_schedule_time_id"
        const val EXTRA_SCHEDULED_AT = "extra_scheduled_at"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
