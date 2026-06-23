package com.redes.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SwitchAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redes.app.R
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesNight
import com.redes.app.ui.theme.RedesNightDeep
import com.redes.app.ui.theme.RedesNightSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    uiState: HomeUiState,
    onRoleSelected: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val roles = uiState.backendSession?.roles.orEmpty()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RedesNight, RedesNightSoft, RedesNightDeep)
                )
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x2830518C), Color.Transparent),
                        radius = 1000f,
                    )
                )
        )

        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(animationSpec = tween(350)) +
                slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                    initialOffsetY = { 48 },
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header icon — mismo estilo visual que la pantalla de login
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(88.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x304D9DE0), Color.Transparent)
                                )
                            )
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(Color(0x1CFFFFFF)),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwitchAccount,
                            contentDescription = null,
                            tint = RedesAccent,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "REDES",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 7.sp),
                    fontWeight = FontWeight.Light,
                    color = Color(0xFF7A90A8),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.role_selection_title),
                    style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = (-0.3).sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.role_selection_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCFD8E6),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    roles.forEach { role ->
                        RoleCard(
                            role = role,
                            onClick = { onRoleSelected(role) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(44.dp))
                TextButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = null,
                        tint = Color(0xFF6B80A0),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.logout_button),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B80A0),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleCard(
    role: String,
    onClick: () -> Unit,
) {
    val accent = roleAccentColor(role)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC11131A)),
        border = BorderStroke(1.dp, Color(0x14FFFFFF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
            ) {
                Icon(
                    imageVector = roleIcon(role),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = roleDisplayName(role),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                val desc = roleDescription(role)
                if (desc.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4A6080),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun roleDisplayName(role: String): String = when (role.uppercase()) {
    "TECNICO" -> "Técnico de Campo"
    "COORDINADOR" -> "Coordinador"
    "SUPERVISOR" -> "Supervisor"
    "ADMIN" -> "Administrador"
    "GERENCIA" -> "Gerencia"
    "SEGURIDAD" -> "Seguridad"
    else -> role.lowercase().replaceFirstChar { it.uppercaseChar() }
}

private fun roleDescription(role: String): String = when (role.uppercase()) {
    "TECNICO" -> "Gestión de órdenes de trabajo en campo"
    "COORDINADOR" -> "Coordinación de cuadrillas y recursos"
    "SUPERVISOR" -> "Supervisión de operaciones y equipos"
    "ADMIN" -> "Administración y configuración del sistema"
    "GERENCIA" -> "Gestión gerencial e informes"
    "SEGURIDAD" -> "Control y gestión de seguridad"
    else -> ""
}

private fun roleIcon(role: String): ImageVector = when (role.uppercase()) {
    "TECNICO" -> Icons.Outlined.Construction
    "COORDINADOR" -> Icons.Outlined.Dashboard
    "SUPERVISOR" -> Icons.Outlined.ManageAccounts
    "ADMIN" -> Icons.Outlined.AdminPanelSettings
    "GERENCIA" -> Icons.Outlined.BarChart
    "SEGURIDAD" -> Icons.Outlined.Security
    else -> Icons.Outlined.Person
}

private fun roleAccentColor(role: String): Color = when (role.uppercase()) {
    "TECNICO" -> Color(0xFF4D9DE0)
    "COORDINADOR" -> Color(0xFF3FB68A)
    "SUPERVISOR" -> Color(0xFF9B7AE0)
    "ADMIN" -> Color(0xFFE06060)
    "GERENCIA" -> Color(0xFFE0A84D)
    "SEGURIDAD" -> Color(0xFF4DCCE0)
    else -> Color(0xFF4D9DE0)
}
