package com.redes.app.ui.auth

import com.redes.app.data.auth.AuthUser

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val isAuthResolved: Boolean = false,
    val currentUser: AuthUser? = null,
)
