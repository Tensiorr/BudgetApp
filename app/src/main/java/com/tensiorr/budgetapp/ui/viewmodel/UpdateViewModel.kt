package com.tensiorr.budgetapp.ui.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tensiorr.budgetapp.data.preferences.UpdatePreferences
import com.tensiorr.budgetapp.data.repository.UpdateRepository
import com.tensiorr.budgetapp.domain.model.AppUpdate
import com.tensiorr.budgetapp.util.ApkDownloader
import com.tensiorr.budgetapp.util.ApkInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel for managing app updates.
 *
 * Handles:
 * - Checking for updates (silent and manual)
 * - Downloading APK with progress
 * - Installing APK
 * - Permission management
 */
class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val updateRepository = UpdateRepository()
    private val updatePreferences = UpdatePreferences(application)
    private val apkDownloader = ApkDownloader(application)

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    /**
     * Checks for updates silently (no "up to date" dialog).
     * Used on app startup.
     */
    fun checkForUpdateSilently() {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Checking

            try {
                val update = updateRepository.checkForUpdate()

                _uiState.value = if (update != null) {
                    UpdateUiState.UpdateAvailable(update)
                } else {
                    UpdateUiState.Idle
                }

                updatePreferences.updateLastCheckTimestamp()

            } catch (e: Exception) {
                Log.e(TAG, "Silent update check failed", e)
                _uiState.value = UpdateUiState.Idle
            }
        }
    }

    /**
     * Checks for updates with UI feedback (shows "up to date" dialog).
     * Used when user manually triggers check.
     */
    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Checking

            try {
                val update = updateRepository.checkForUpdate()

                _uiState.value = if (update != null) {
                    UpdateUiState.UpdateAvailable(update)
                } else {
                    UpdateUiState.NoUpdateAvailable
                }

                updatePreferences.updateLastCheckTimestamp()

            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
                _uiState.value = UpdateUiState.Error(
                    e.message ?: "Błąd sprawdzania aktualizacji"
                )
            }
        }
    }

    /**
     * Downloads and prepares update for installation.
     */
    fun downloadAndInstall(update: AppUpdate) {
        viewModelScope.launch {
            try {
                _uiState.value = UpdateUiState.Downloading(0)

                val apkFile = apkDownloader.downloadApk(update.downloadUrl) { progress ->
                    _uiState.value = UpdateUiState.Downloading(progress)
                }

                if (!ApkInstaller.verifyApkPackage(getApplication(), apkFile)) {
                    _uiState.value = UpdateUiState.Error("Nieprawidłowy plik APK")
                    return@launch
                }

                _uiState.value = UpdateUiState.ReadyToInstall(apkFile)

            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                _uiState.value = UpdateUiState.Error(
                    e.message ?: "Błąd pobierania"
                )
            }
        }
    }

    /**
     * Installs the downloaded APK.
     *
     * @return true if installation started, false if permission needed
     */
    fun installApk(apkFile: File): Boolean {
        if (!canRequestPackageInstalls()) {
            _uiState.value = UpdateUiState.NeedsInstallPermission(apkFile)
            return false
        }

        ApkInstaller.installApk(getApplication(), apkFile)
        return true
    }

    /**
     * Dismisses the update notification for this version.
     */
    fun dismissUpdate(update: AppUpdate) {
        viewModelScope.launch {
            updatePreferences.dismissVersion(update.versionCode)
            _uiState.value = UpdateUiState.Idle
        }
    }

    /**
     * Resets UI state to idle.
     */
    fun resetState() {
        _uiState.value = UpdateUiState.Idle
    }

    /**
     * Checks if app has permission to install packages (Android 8.0+).
     */
    private fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    companion object {
        private const val TAG = "UpdateViewModel"
    }
}

/**
 * UI state for update flow.
 */
sealed class UpdateUiState {
    data object Idle : UpdateUiState()
    data object Checking : UpdateUiState()
    data object NoUpdateAvailable : UpdateUiState()
    data class UpdateAvailable(val update: AppUpdate) : UpdateUiState()
    data class Downloading(val progress: Int) : UpdateUiState()
    data class ReadyToInstall(val apkFile: File) : UpdateUiState()
    data class NeedsInstallPermission(val apkFile: File) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}