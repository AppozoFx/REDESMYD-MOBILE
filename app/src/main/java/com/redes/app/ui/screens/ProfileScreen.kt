package com.redes.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.redes.app.BuildConfig
import com.redes.app.R
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.common.preferredPersonName
import com.redes.app.ui.home.HomeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: HomeUiState,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.profile_title),
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_section_account),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text("UID: ${uiState.firebaseUser?.uid ?: "sin datos"}")
                    Text("Correo: ${uiState.firebaseUser?.email ?: "sin datos"}")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_section_access),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "Nombre: ${
                            uiState.backendSession?.let { preferredPersonName(it.nombreCorto, it.nombre) }
                                ?: "sin datos"
                        }"
                    )
                    Text("Estado acceso: ${uiState.backendSession?.estadoAcceso ?: "sin datos"}")
                    Text("Admin: ${if (uiState.backendSession?.isAdmin == true) "si" else "no"}")
                    Text("Rol activo: ${uiState.selectedRole ?: "sin datos"}")
                    Text("Roles: ${uiState.backendSession?.roles?.joinToString().orEmpty().ifBlank { "sin datos" }}")
                    Text("Areas: ${uiState.backendSession?.areas?.joinToString().orEmpty().ifBlank { "sin datos" }}")
                    Text("Permisos: ${uiState.backendSession?.permissions?.joinToString().orEmpty().ifBlank { "sin datos" }}")
                    if (uiState.isUsingCachedSession) {
                        Text("Fuente: cache local")
                    } else if (uiState.backendSession != null) {
                        Text("Fuente: backend")
                    } else {
                        Text("Fuente: sin perfil backend")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.profile_base_url_label),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(BuildConfig.API_BASE_URL.ifBlank { "sin configurar" })
                    Text("Entorno: ${BuildConfig.API_ENVIRONMENT}")
                }
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Volver al inicio")
            }
        }
    }
}
