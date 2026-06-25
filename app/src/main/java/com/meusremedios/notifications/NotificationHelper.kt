package com.meusremedios.notifications

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.meusremedios.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
    private val notificationManager: NotificationManagerCompat,
) {
    fun notify(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)
    }

    fun cancel(id: Int) {
        notificationManager.cancel(id)
    }

    /** Agenda alarme para uma dose, criando o PendingIntent internamente. */
    fun scheduleDoseAlarm(
        requestCode: Int,
        triggerAtMillis: Long,
        medicationId: Long,
        medicationName: String,
        scheduleTimeId: Long,
        timeLabel: String,
        scheduledAt: String,
        date: String,
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(AlarmReceiver.EXTRA_SCHEDULE_TIME_ID, scheduleTimeId)
            putExtra(AlarmReceiver.EXTRA_TIME_LABEL, timeLabel)
            putExtra(AlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(AlarmReceiver.EXTRA_DATE, date)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, requestCode)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        scheduleExact(triggerAtMillis, pi)
    }

    /** Agenda o alarme diário de meia-noite para reagendar os alarmes do dia seguinte. */
    fun scheduleMidnightAlarm(triggerAtMillis: Long) {
        val intent = Intent(context, BootReceiver::class.java).apply {
            action = BootReceiver.ACTION_MIDNIGHT_RESCHEDULE
        }
        val pi = PendingIntent.getBroadcast(
            context,
            MIDNIGHT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        scheduleExact(triggerAtMillis, pi)
    }

    /** Cancela o alarme de dose identificado pelo requestCode. */
    fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    /** Cria e retorna um PendingIntent de abertura da tela de reconhecimento. */
    fun buildOpenRecognitionIntent(notificationId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.NAVIGATE_TO_RECOGNITION, true)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getActivity(
            context,
            notificationId + OPEN_RECOGNITION_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms()
        else true

    private fun scheduleExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    companion object {
        const val MIDNIGHT_REQUEST_CODE = Int.MAX_VALUE
        private const val OPEN_RECOGNITION_OFFSET = 10000
    }
}
