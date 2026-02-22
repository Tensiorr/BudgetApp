package com.tensiorr.budgetapp.util

/**
 * Utility for comparing semantic version strings.
 *
 * Supports version formats like "0.3.1", "v0.4.0", "1.2.3"
 */
object VersionComparator {

    /**
     * Compares two version strings.
     *
     * @param current Current app version (e.g., "0.3.1")
     * @param new New version from release (e.g., "0.4.0" or "v0.4.0")
     * @return 1 if new > current, 0 if equal, -1 if new < current
     */
    fun compare(current: String, new: String): Int {
        val currentParts = current.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val newParts = new.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(currentParts.size, newParts.size)

        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrNull(i) ?: 0
            val newPart = newParts.getOrNull(i) ?: 0

            when {
                newPart > currentPart -> return 1
                newPart < currentPart -> return -1
            }
        }

        return 0
    }

    /**
     * Checks if an update is available.
     *
     * @param currentVersion Current app version
     * @param latestVersion Latest version from release
     * @return true if update available (latest > current)
     */
    fun isUpdateAvailable(currentVersion: String, latestVersion: String): Boolean {
        return compare(currentVersion, latestVersion) > 0
    }
}