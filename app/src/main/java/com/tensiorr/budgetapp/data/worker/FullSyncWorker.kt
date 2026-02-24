package com.tensiorr.budgetapp.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.tensiorr.budgetapp.data.repository.SyncRepository

/**
 * One-time worker for full synchronization (upload + download).
 */
class FullSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val syncRepository = SyncRepository(context)

    companion object {
        const val TAG = "FullSyncWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.success()

            val syncResult = syncRepository.performFullSync(currentUser.uid)

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Log.e(TAG, "Full sync failed", syncResult.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Full sync exception", e)
            Result.retry()
        }
    }
}