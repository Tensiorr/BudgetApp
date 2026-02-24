package com.tensiorr.budgetapp.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.tensiorr.budgetapp.data.database.AppDatabase
import com.tensiorr.budgetapp.data.preferences.UserPreferences
import com.tensiorr.budgetapp.data.repository.AuthRepository
import com.tensiorr.budgetapp.util.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val context = application.applicationContext

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _googleSignInIntentSender = MutableStateFlow<IntentSender?>(null)
    val googleSignInIntentSender: StateFlow<IntentSender?> = _googleSignInIntentSender.asStateFlow()

    private val _hasGuestData = MutableStateFlow(false)
    val hasGuestData: StateFlow<Boolean> = _hasGuestData.asStateFlow()

    private val _showGuestDataDialog = MutableStateFlow(false)
    val showGuestDataDialog: StateFlow<Boolean> = _showGuestDataDialog.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user.uid, user.email)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email i hasło nie mogą być puste")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithEmail(email, password)
                .onFailure { exception ->
                    _authState.value = AuthState.Error(getErrorMessage(exception))
                }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email i hasło nie mogą być puste")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.registerWithEmail(email, password)
                .onFailure { exception ->
                    _authState.value = AuthState.Error(getErrorMessage(exception))
                }
        }
    }

    fun beginGoogleSignIn() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            authRepository.beginGoogleSignIn()
                .onSuccess { intentSender ->
                    _googleSignInIntentSender.value = intentSender
                }
                .onFailure { exception ->
                    Log.e("AuthViewModel", "Google Sign-In failed", exception)
                    _authState.value = AuthState.Error(getErrorMessage(exception))
                }
        }
    }

    fun signInWithGoogle(intent: Intent) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _googleSignInIntentSender.value = null

            authRepository.signInWithGoogle(intent)
                .onFailure { exception ->
                    _authState.value = AuthState.Error(getErrorMessage(exception))
                }
        }
    }

    fun signOut(clearLocalData: Boolean = true) {
        authRepository.signOut()

        if (clearLocalData) {
            viewModelScope.launch {
                try {
                    AppDatabase.clearAllData(context)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to clear local data", e)
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Podaj adres email")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _authState.value = AuthState.Error("Link do resetowania hasła został wysłany na email")
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(getErrorMessage(exception))
                }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Nieprawidłowy email lub hasło"
            is FirebaseAuthInvalidUserException -> "Użytkownik nie istnieje"
            is FirebaseAuthUserCollisionException -> "Konto z tym emailem już istnieje"
            is FirebaseAuthWeakPasswordException -> "Hasło jest zbyt słabe (min. 6 znaków)"
            is FirebaseNetworkException -> "Brak połączenia z internetem"
            else -> exception.message ?: "Wystąpił nieznany błąd"
        }
    }

    suspend fun beginGoogleSignInBlocking(onIntentReady: (IntentSender) -> Unit) {
        authRepository.beginGoogleSignIn()
            .onSuccess { intentSender ->
                onIntentReady(intentSender)
            }
            .onFailure { exception ->
                Log.e("AuthViewModel", "Google Sign-In failed", exception)
                _authState.value = AuthState.Error(getErrorMessage(exception))
            }
    }

    suspend fun checkGuestHasData(): Boolean {
        return try {
            val db = AppDatabase.getDatabase(context)
            val transactionCount = db.transactionDao().getTransactionCount()
            val hasData = transactionCount > 0
            _hasGuestData.value = hasData
            hasData
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to check guest data", e)
            false
        }
    }

    fun showGuestDataDialog() {
        _showGuestDataDialog.value = true
    }

    fun hideGuestDataDialog() {
        _showGuestDataDialog.value = false
    }

    fun handleGuestData(keepData: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                if (!keepData) {
                    AppDatabase.clearAllData(context)
                }

                val preferences = UserPreferences(context)
                preferences.clearGuestToLoginFlag()

                hideGuestDataDialog()
                onComplete()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to handle guest data", e)
                _authState.value = AuthState.Error("Błąd podczas obsługi danych: ${e.message}")
            }
        }
    }
}