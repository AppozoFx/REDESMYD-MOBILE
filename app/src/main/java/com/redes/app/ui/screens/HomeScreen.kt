package com.redes.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.redes.app.R
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.common.preferredPersonName
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesBlue
import com.redes.app.ui.theme.RedesCardDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onProfileClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onChangeRoleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.home_title),
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RedesCardDark
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        color = Color(0x1639D98A),
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_status_validated),
                            color = Color(0xFF7EF0A8),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.home_welcome_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.home_welcome_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCFD8E6),
                    )
                    SessionLine(
                        label = stringResource(R.string.home_active_role_label),
                        value = uiState.selectedRole ?: uiState.defaultRole ?: "sin datos",
                    )
                    SessionLine(
                        label = stringResource(R.string.home_user_label),
                        value = uiState.firebaseUser?.email ?: uiState.firebaseUser?.uid ?: "sin datos",
                    )
                    SessionLine(
                        label = stringResource(R.string.home_roles_label),
                        value = uiState.backendSession?.roles?.joinToString().orDash(),
                    )
                    SessionLine(
                        label = stringResource(R.string.home_areas_label),
                        value = uiState.backendSession?.areas?.joinToString().orDash(),
                    )
                }
            }

            Button(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedesBlue,
                    contentColor = Color.White,
                ),
            ) {
                Text(text = stringResource(R.string.home_primary_action))
            }

            if ((uiState.backendSession?.roles?.size ?: 0) > 1) {
                Button(
                    onClick = onChangeRoleClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = stringResource(R.string.home_change_role_action))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_status_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    when {
                        !uiState.isBackendConfigured -> {
                            Text(
                                text = stringResource(R.string.backend_not_configured),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        uiState.isLoading -> {
                            Text(
                                text = stringResource(R.string.backend_loading),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            CircularProgressIndicator()
                        }

                        uiState.backendSession != null -> {
                            if (uiState.isUsingCachedSession) {
                                Text("Mostrando ultimo perfil almacenado localmente.")
                            }
                            Surface(
                                color = Color(0x122A87FF),
                                shape = RoundedCornerShape(999.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.home_status_backend_ready),
                                    color = RedesAccent,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                            SessionLine(
                                "Nombre",
                                preferredPersonName(
                                    uiState.backendSession.nombreCorto,
                                    uiState.backendSession.nombre,
                                ),
                            )
                            SessionLine("UID", uiState.backendSession.uid)
                            SessionLine("Email", uiState.backendSession.email ?: "sin datos")
                            SessionLine(
                                label = stringResource(R.string.home_permissions_label),
                                value = uiState.backendSession.permissions.joinToString().ifBlank { "sin datos" },
                            )
                        }

                        uiState.errorMessage != null -> {
                            Surface(
                                color = Color(0x18F87171),
                                shape = RoundedCornerShape(999.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.home_status_backend_pending),
                                    color = Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        else -> {
                            Text(stringResource(R.string.home_status_backend_pending))
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_next_step_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.home_next_step_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Button(
                onClick = onRefreshClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.isBackendConfigured,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = stringResource(R.string.home_secondary_action))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(text = stringResource(R.string.logout_button))
            }
        }
    }
}

@Composable
private fun SessionLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF94A3B8),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
    }
}

private fun String?.orDash(): String = this?.ifBlank { "sin datos" } ?: "sin datos"
