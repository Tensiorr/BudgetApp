package com.tensiorr.budgetapp.domain.model

/**
 * Domain model representing an available app update.
 *
 * @property version Version string (e.g., "0.4.0")
 * @property versionCode Numeric version code for comparison
 * @property changelog Release notes (Markdown format)
 * @property downloadUrl Direct APK download URL
 * @property apkSizeBytes APK file size in bytes
 * @property publishedAt ISO 8601 timestamp of release
 */
data class AppUpdate(
    val version: String,
    val versionCode: Long,
    val changelog: String,
    val downloadUrl: String,
    val apkSizeBytes: Long,
    val publishedAt: String
) {
    /**
     * Returns human-readable file size (e.g., "8.2 MB").
     */
    fun getFormattedSize(): String {
        val mb = apkSizeBytes / (1024.0 * 1024.0)
        return "%.1f MB".format(mb)
    }
}