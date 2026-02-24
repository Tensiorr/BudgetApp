package com.tensiorr.budgetapp.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.tensiorr.budgetapp.BuildConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling Firebase Authentication operations.
 *
 * Supports:
 * - Email/Password authentication
 * - Google Sign-In via One Tap
 * - Auth state observation
 */
class AuthRepository(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    /**
     * Current authenticated user, null if not logged in.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Flow emitting auth state changes.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    /**
     * Sign in with email and password.
     *
     * @param email User email
     * @param password User password
     * @return Result with FirebaseUser on success, exception on failure
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Logowanie nie powiodło się"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register new user with email and password.
     *
     * @param email User email
     * @param password User password
     * @return Result with FirebaseUser on success, exception on failure
     */
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Rejestracja nie powiodła się"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Begin Google Sign-In flow using One Tap.
     *
     * @return Result with IntentSender on success, exception on failure
     */
    suspend fun beginGoogleSignIn(): Result<IntentSender> {
        return try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()

            val result = oneTapClient.beginSignIn(signInRequest).await()
            Result.success(result.pendingIntent.intentSender)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    /**
     * Complete Google Sign-In with the Intent from One Tap.
     *
     * @param intent Result intent from One Tap UI
     * @return Result with FirebaseUser on success, exception on failure
     */
    suspend fun signInWithGoogle(intent: Intent): Result<FirebaseUser> {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
                ?: return Result.failure(Exception("Brak tokenu Google"))

            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            authResult.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Logowanie przez Google nie powiodło się"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user.
     */
    fun signOut() {
        auth.signOut()
        oneTapClient.signOut()
    }

    /**
     * Send password reset email.
     *
     * @param email User email
     * @return Result with Unit on success, exception on failure
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}