package com.meusremedios.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meusremedios.domain.usecase.CleanupOldIntakesUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RetentionWorkerEntryPoint {
    fun cleanupOldIntakesUseCase(): CleanupOldIntakesUseCase
}

class RetentionWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            RetentionWorkerEntryPoint::class.java,
        )
        entryPoint.cleanupOldIntakesUseCase()()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
