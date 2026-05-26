package com.redes.app.ui.home

import com.redes.app.data.auth.AuthUser
import com.redes.app.data.session.MobileComunicado
import com.redes.app.data.session.MobileSession

data class HomeUiState(
    val firebaseUser: AuthUser? = null,
    val backendSession: MobileSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBackendConfigured: Boolean = false,
    val isUsingCachedSession: Boolean = false,
    val isStartupReady: Boolean = false,
    val comunicados: List<MobileComunicado> = emptyList(),
    val requiresComunicadosGate: Boolean = false,
    val roleSelectionRequired: Boolean = false,
    val defaultRole: String? = null,
    val selectedRole: String? = null,
)

val HomeUiState.blockingComunicados: List<MobileComunicado>
    get() = comunicados.filter { it.isBlocking }

val HomeUiState.needsRoleSelection: Boolean
    get() = backendSession != null &&
        !requiresComunicadosGate &&
        roleSelectionRequired &&
        selectedRole == null
