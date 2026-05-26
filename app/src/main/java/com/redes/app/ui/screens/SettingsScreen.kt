package com.redes.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.redes.app.R
import com.redes.app.ui.common.preferredPersonName
import com.redes.app.ui.home.HomeUiState

@Composable
fun SettingsScreen(
    uiState: HomeUiState,
    onProfileClick: () -> Unit,
    onChangeRoleClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val role = uiState.selectedRole ?: uiState.defaultRole.orEmpty()
    val canChangeRole = (uiState.backendSession?.roles?.size ?: 0) > 1
    val canSwitchCuadrilla = role == "COORDINADOR" || role == "SUPERVISOR"

    Scaffold(modifier = modifier) { innerPadding ->
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
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text("Rol activo: ${role.ifBlank { "sin datos" }}")
                    Text(
                        "Usuario: ${
                            uiState.backendSession?.let { preferredPersonName(it.nombreCorto, it.nombre) }
                                ?: uiState.firebaseUser?.email
                                ?: "sin datos"
                        }"
                    )
                }
            }

            Button(onClick = onProfileClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_profile_action))
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings_password_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(stringResource(R.string.settings_password_body))
                }
            }

            if (canChangeRole) {
                Button(onClick = onChangeRoleClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.settings_change_role_action))
                }
            }

            if (canSwitchCuadrilla) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_switch_cuadrilla_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(stringResource(R.string.settings_switch_cuadrilla_body))
                    }
                }
            }

            Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.logout_button))
            }

            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_back_action))
            }
        }
    }
}
