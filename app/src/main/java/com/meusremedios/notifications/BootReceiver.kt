package com.meusremedios.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meusremedios.domain.usecase.RescheduleAllAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reagenda todos os alarmes após boot ou à meia-noite. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != ACTION_MIDNIGHT_RESCHEDULE) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                rescheduleAllAlarmsUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MIDNIGHT_RESCHEDULE = "com.meusremedios.ACTION_MIDNIGHT_RESCHEDULE"
    }
}
