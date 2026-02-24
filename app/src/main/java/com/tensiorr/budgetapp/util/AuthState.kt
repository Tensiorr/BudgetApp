package com.tensiorr.budgetapp.util

/**
 * Represents authentication state.
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val userId: String, val email: String?) : AuthState()
    data object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}