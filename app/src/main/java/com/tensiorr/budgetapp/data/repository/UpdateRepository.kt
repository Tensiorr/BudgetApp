package com.tensiorr.budgetapp.data.repository

import android.util.Log
import com.tensiorr.budgetapp.BuildConfig
import com.tensiorr.budgetapp.data.api.GitHubApi
import com.tensiorr.budgetapp.data.api.RetrofitInstance
import com.tensiorr.budgetapp.domain.model.AppUpdate
import com.tensiorr.budgetapp.util.VersionComparator

/**
 * Repository for handling app update checks via GitHub Releases API.
 */
class UpdateRepository(
    private val gitHubApi: GitHubApi = RetrofitInstance.gitHubApi
) {

    /**
     * Checks if an app update is available.
     *
     * @return AppUpdate if newer version available, null otherwise
     * @throws Exception if API call fails
     */
    suspend fun checkForUpdate(): AppUpdate? {
        try {
            val release = gitHubApi.getLatestRelease(
                owner = GitHubApi.REPO_OWNER,
                repo = GitHubApi.REPO_NAME
            )

            if (release.prerelease) {
                Log.d(TAG, "Skipping pre-release ${release.tagName}")
                return null
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val latestVersion = release.tagName.removePrefix("v")

            if (!VersionComparator.isUpdateAvailable(currentVersion, latestVersion)) {
                Log.d(TAG, "No update: current=$currentVersion, latest=$latestVersion")
                return null
            }

            val apkAsset = release.assets.firstOrNull { asset ->
                asset.name.endsWith(".apk", ignoreCase = true)
            }

            if (apkAsset == null) {
                Log.e(TAG, "No APK found in release ${release.tagName}")
                return null
            }

            Log.d(TAG, "Update available: $currentVersion → $latestVersion")

            return AppUpdate(
                version = latestVersion,
                versionCode = parseVersionCode(latestVersion),
                changelog = release.body,
                downloadUrl = apkAsset.browserDownloadUrl,
                apkSizeBytes = apkAsset.size.toLong(),
                publishedAt = release.publishedAt
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error checking for update", e)
            throw e
        }
    }

    /**
     * Converts version string to numeric version code.
     *
     * Example: "0.4.0" → 400, "1.2.3" → 10203
     */
    private fun parseVersionCode(version: String): Long {
        val parts = version.removePrefix("v").split(".")
        val major = parts.getOrNull(0)?.toLongOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toLongOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toLongOrNull() ?: 0

        return (major * 10000) + (minor * 100) + patch
    }

    companion object {
        private const val TAG = "UpdateRepository"
    }
}