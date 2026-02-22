package com.tensiorr.budgetapp.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility for downloading APK files.
 *
 * Downloads to app-specific external files directory (no storage permission needed).
 */
class ApkDownloader(private val context: Context) {

    /**
     * Downloads APK file from URL with progress callback.
     *
     * @param url Download URL
     * @param onProgress Progress callback (0-100)
     * @return Downloaded APK file
     * @throws Exception if download fails
     */
    suspend fun downloadApk(
        url: String,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {

        val apkDir = File(context.getExternalFilesDir(null), "updates")
        if (!apkDir.exists()) {
            apkDir.mkdirs()
        }

        val apkFile = File(apkDir, "update.apk")

        if (apkFile.exists()) {
            apkFile.delete()
        }

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw Exception("HTTP ${connection.responseCode}")
        }

        val fileSize = connection.contentLength
        var downloadedSize = 0L

        connection.inputStream.use { input ->
            FileOutputStream(apkFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead

                    if (fileSize > 0) {
                        val progress = (downloadedSize * 100 / fileSize).toInt()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }
            }
        }

        connection.disconnect()

        Log.d(TAG, "Downloaded: ${apkFile.absolutePath} (${apkFile.length()} bytes)")
        apkFile
    }

    companion object {
        private const val TAG = "ApkDownloader"
    }
}