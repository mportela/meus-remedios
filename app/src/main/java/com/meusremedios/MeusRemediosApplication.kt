package com.meusremedios

import android.app.Application
import android.graphics.Bitmap
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.meusremedios.data.ml.TfliteEmbedder
import com.meusremedios.domain.usecase.MigratePhotoFeaturesUseCase
import com.meusremedios.domain.usecase.RescheduleAllAlarmsUseCase
import com.meusremedios.notifications.NotificationChannels
import com.meusremedios.work.RetentionWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application root do app. Habilita o grafo de dependências do Hilt para todas
 * as camadas (ui / domain / data / notifications).
 */
@HiltAndroidApp
class MeusRemediosApplication : Application() {
    @Inject lateinit var tfliteEmbedder: TfliteEmbedder

    @Inject lateinit var migratePhotoFeaturesUseCase: MigratePhotoFeaturesUseCase

    @Inject lateinit var rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
        warmUpTflite()
        runPhotoMigration()
        rescheduleAlarms()
        scheduleRetentionWorker()
    }

    // Dispara a carga de libtensorflowlite.so e a criação do Interpreter em
    // background no startup, evitando que a primeira foto cadastrada bloqueie
    // a main thread durante a inferência.
    private fun warmUpTflite() {
        appScope.launch {
            val dummy = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            runCatching { tfliteEmbedder.embed(dummy) }
            dummy.recycle()
        }
    }

    private fun runPhotoMigration() {
        appScope.launch(Dispatchers.IO) {
            runCatching { migratePhotoFeaturesUseCase() }
        }
    }

    private fun rescheduleAlarms() {
        appScope.launch(Dispatchers.IO) {
            runCatching { rescheduleAllAlarmsUseCase() }
        }
    }

    private fun scheduleRetentionWorker() {
        runCatching {
            val retentionWork =
                PeriodicWorkRequestBuilder<RetentionWorker>(
                    1,
                    TimeUnit.DAYS,
                ).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "retention",
                ExistingPeriodicWorkPolicy.KEEP,
                retentionWork,
            )
        }
    }
}
