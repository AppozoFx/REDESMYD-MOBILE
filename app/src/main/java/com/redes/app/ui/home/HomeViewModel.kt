package com.redes.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.session.MobileBootstrap
import com.redes.app.data.session.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(isBackendConfigured = sessionRepository.isConfigured)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        observeCachedSession()
        observeSelectedRole()
        observeAuthState()
    }

    fun refreshBackendSession() {
        val user = _uiState.value.firebaseUser ?: return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    firebaseUser = user,
                    isLoading = true,
                    errorMessage = null,
                    isBackendConfigured = sessionRepository.isConfigured,
                )
            }

            val result = withContext(Dispatchers.IO) {
                sessionRepository.fetchBootstrap()
            }
            result.fold(
                onSuccess = { bootstrap ->
                    applyBootstrap(bootstrap)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage(),
                            isStartupReady = true,
                        )
                    }
                }
            )
        }
    }

    fun markComunicadoSeen(comunicadoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(Dispatchers.IO) {
                sessionRepository.markComunicadoSeen(comunicadoId)
            }
            result.fold(
                onSuccess = { refreshBackendSession() },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toUserMessage(),
                        )
                    }
                }
            )
        }
    }

    fun selectRole(role: String) {
        val normalized = role.trim().uppercase()
        val roles = _uiState.value.backendSession?.roles.orEmpty()
        if (!roles.contains(normalized)) return

        viewModelScope.launch {
            sessionRepository.saveSelectedRole(normalized)
            _uiState.update { it.copy(selectedRole = normalized) }
        }
    }

    fun clearSelectedRole() {
        viewModelScope.launch {
            sessionRepository.saveSelectedRole(null)
            _uiState.update { it.copy(selectedRole = null) }
        }
    }

    private fun applyBootstrap(bootstrap: MobileBootstrap) {
        viewModelScope.launch {
            val currentSelected = _uiState.value.selectedRole
            val roles = bootstrap.session.roles
            val selectedRole = when {
                currentSelected != null && roles.contains(currentSelected) -> currentSelected
                !bootstrap.roleSelectionRequired && bootstrap.defaultRole != null -> bootstrap.defaultRole
                else -> null
            }

            if (selectedRole != _uiState.value.selectedRole) {
                sessionRepository.saveSelectedRole(selectedRole)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    backendSession = bootstrap.session,
                    comunicados = bootstrap.comunicados,
                    requiresComunicadosGate = bootstrap.requiresComunicadosGate,
                    roleSelectionRequired = bootstrap.roleSelectionRequired,
                    defaultRole = bootstrap.defaultRole,
                    selectedRole = selectedRole,
                    errorMessage = null,
                    isUsingCachedSession = false,
                    isStartupReady = true,
                )
            }
        }
    }

    private fun observeCachedSession() {
        viewModelScope.launch {
            sessionRepository.cachedSession.collect { cached ->
                _uiState.update {
                    it.copy(
                        backendSession = cached ?: it.backendSession,
                        isUsingCachedSession = cached != null,
                        isStartupReady = true,
                    )
                }
            }
        }
    }

    private fun observeSelectedRole() {
        viewModelScope.launch {
            sessionRepository.selectedRole.collect { role ->
                _uiState.update { state ->
                    val allowedRoles = state.backendSession?.roles.orEmpty()
                    val safeRole = role?.takeIf { allowedRoles.isEmpty() || allowedRoles.contains(it) }
                    state.copy(selectedRole = safeRole)
                }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        firebaseUser = user,
                        backendSession = if (user == null) null else it.backendSession,
                        errorMessage = null,
                        isLoading = false,
                        isBackendConfigured = sessionRepository.isConfigured,
                        isStartupReady = true,
                    )
                }

                if (user == null) {
                    refreshJob?.cancel()
                    sessionRepository.clearCache()
                    _uiState.update { state ->
                        state.copy(
                            backendSession = null,
                            comunicados = emptyList(),
                            requiresComunicadosGate = false,
                            roleSelectionRequired = false,
                            defaultRole = null,
                            selectedRole = null,
                            errorMessage = null,
                            isLoading = false,
                            isUsingCachedSession = false,
                            isStartupReady = true,
                        )
                    }
                } else {
                    refreshBackendSession()
                }
            }
        }
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(authRepository, sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun Throwable.toUserMessage(): String {
    return localizedMessage ?: "No se pudo consultar el backend."
}
