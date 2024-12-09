package com.jonathan.mymaps.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jonathan.mymaps.data.MapRepository

class MapsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val mapRepository: MapRepository
): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            mapRepository.syncMaps()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}