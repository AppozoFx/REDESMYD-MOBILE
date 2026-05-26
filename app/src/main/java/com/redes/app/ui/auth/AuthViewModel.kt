package com.redes.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.redes.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                errorMessage = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

    fun signIn() {
        val snapshot = _uiState.value
        val email = snapshot.email.trim()
        val password = snapshot.password

        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    emailError = null,
                    passwordError = null,
                )
            }

            val result = authRepository.signIn(email, password)
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = result.exceptionOrNull()?.toUserMessage(),
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.update {
            it.copy(
                password = "",
                errorMessage = null,
            )
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isAuthResolved = true,
                        isSubmitting = false,
                        errorMessage = null,
                        password = if (user != null) "" else it.password,
                    )
                }
            }
        }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Ingresa tu correo."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Correo no valido."
        }
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return "Ingresa tu contrasena."
        if (password.length < 6) return "La contrasena debe tener al menos 6 caracteres."
        return null
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun Throwable.toUserMessage(): String {
    return when ((this as? FirebaseAuthException)?.errorCode) {
        "ERROR_INVALID_EMAIL" -> "El correo no es valido."
        "ERROR_INVALID_CREDENTIAL",
        "ERROR_WRONG_PASSWORD",
        "ERROR_USER_NOT_FOUND" -> "Credenciales incorrectas."
        "ERROR_USER_DISABLED" -> "Tu usuario esta deshabilitado."
        "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Intenta mas tarde."
        "ERROR_NETWORK_REQUEST_FAILED" -> "No hay conexion disponible."
        else -> localizedMessage ?: "No se pudo iniciar sesion."
    }
}
