package com.tensiorr.budgetapp.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.tensiorr.budgetapp.data.repository.FirestoreRepository
import com.tensiorr.budgetapp.data.repository.SyncRepository

/**
 * Background worker for periodic data synchronization.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val syncRepository = SyncRepository(context)
    private val firestoreRepository = FirestoreRepository()

    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "periodic_sync_work"
    }

    override suspend fun doWork(): Result {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: return Result.success()

            val userId = currentUser.uid
            val lastSyncResult = firestoreRepository.getLastSyncTimestamp(userId)

            if (lastSyncResult.isFailure) {
                Log.e(TAG, "Failed to get last sync timestamp", lastSyncResult.exceptionOrNull())
                return Result.retry()
            }

            val syncResult = if (lastSyncResult.getOrNull() == null) {
                syncRepository.performFullSync(userId)
            } else {
                syncRepository.performQuickSync(userId, lastSyncResult.getOrNull()!!)
            }

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Log.e(TAG, "Sync failed", syncResult.exceptionOrNull())
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker exception", e)
            Result.retry()
        }
    }
}