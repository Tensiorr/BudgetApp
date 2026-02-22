package com.tensiorr.budgetapp.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility for installing APK files via Android system installer.
 */
object ApkInstaller {

    /**
     * Triggers APK installation via system installer.
     *
     * @param context Application context
     * @param apkFile APK file to install
     */
    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) {
            Log.e(TAG, "APK file does not exist: ${apkFile.absolutePath}")
            return
        }

        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(intent)
            Log.d(TAG, "Install activity started")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to install APK", e)
        }
    }

    /**
     * Verifies that APK package name matches current app.
     *
     * @param context Application context
     * @param apkFile APK file to verify
     * @return true if package name matches
     */
    fun verifyApkPackage(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) {
            return false
        }

        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            }

            val apkPackageName = packageInfo?.packageName
            val currentPackageName = context.packageName

            if (apkPackageName != currentPackageName) {
                Log.w(TAG, "Package mismatch: $apkPackageName != $currentPackageName")
                return false
            }

            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify APK", e)
            false
        }
    }

    private const val TAG = "ApkInstaller"
}