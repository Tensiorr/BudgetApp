package com.tensiorr.budgetapp.util

import android.content.Context
import androidx.work.*
import com.tensiorr.budgetapp.data.worker.FullSyncWorker
import com.tensiorr.budgetapp.data.worker.SyncWorker
import java.util.concurrent.TimeUnit

/**
 * Manager for scheduling and managing background sync operations.
 */
object SyncManager {

    /**
     * Schedule periodic sync (every 1 hour).
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
    }

    /**
     * Cancel periodic sync.
     */
    fun cancelPeriodicSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.WORK_NAME)
    }

    /**
     * Trigger immediate one-time sync.
     */
    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
    }

    /**
     * Trigger immediate FULL sync (upload + download).
     */
    fun triggerFullSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<FullSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
    }
}