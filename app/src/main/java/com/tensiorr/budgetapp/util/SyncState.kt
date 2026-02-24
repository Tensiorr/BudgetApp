package com.tensiorr.budgetapp.util

/**
 * Represents the current synchronization state.
 */
sealed class SyncState {
    /**
     * Not syncing, idle state.
     */
    data object Idle : SyncState()

    /**
     * Currently syncing data.
     * @param progress Current progress (0.0 to 1.0)
     * @param message Description of current sync operation
     */
    data class Syncing(val progress: Float = 0f, val message: String = "") : SyncState()

    /**
     * Sync completed successfully.
     * @param timestamp When sync completed
     */
    data class Success(val timestamp: Long = System.currentTimeMillis()) : SyncState()

    /**
     * Sync failed with error.
     * @param message Error message
     */
    data class Error(val message: String) : SyncState()
}