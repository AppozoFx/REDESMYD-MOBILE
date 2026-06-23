package com.redes.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwitchAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redes.app.R
import com.redes.app.ui.common.preferredPersonName
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesNight
import com.redes.app.ui.theme.RedesNightDeep
import com.redes.app.ui.theme.RedesNightSoft

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

    val displayName = uiState.backendSession
        ?.let { preferredPersonName(it.nombreCorto, it.nombre) }
        ?: uiState.firebaseUser?.email
        ?: "—"
    val initial = displayName.firstOrNull()?.uppercaseChar() ?: '?'

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
                        colors = listOf(Color(0x2230518C), Color.Transparent),
                        radius = 900f,
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            HorizontalDivider(color = Color(0x12FFFFFF))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // ── Perfil ──
                ProfileCard(initial = initial, name = displayName, role = role)

                Spacer(modifier = Modifier.height(10.dp))

                // ── Cuenta ──
                SettingsSectionLabel("Cuenta")
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Outlined.Person,
                        label = stringResource(R.string.settings_profile_action),
                        onClick = onProfileClick,
                    )
                    if (canChangeRole) {
                        HorizontalDivider(
                            color = Color(0x0EFFFFFF),
                            modifier = Modifier.padding(start = 66.dp),
                        )
                        SettingsRow(
                            icon = Icons.Outlined.SwitchAccount,
                            label = stringResource(R.string.settings_change_role_action),
                            onClick = onChangeRoleClick,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Información ──
                SettingsSectionLabel("Información")
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        label = stringResource(R.string.settings_password_title),
                        subtitle = stringResource(R.string.settings_password_body),
                        showChevron = false,
                    )
                    if (canSwitchCuadrilla) {
                        HorizontalDivider(
                            color = Color(0x0EFFFFFF),
                            modifier = Modifier.padding(start = 66.dp),
                        )
                        SettingsRow(
                            icon = Icons.Outlined.Groups,
                            label = stringResource(R.string.settings_switch_cuadrilla_title),
                            subtitle = stringResource(R.string.settings_switch_cuadrilla_body),
                            showChevron = false,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Cerrar sesión ──
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Outlined.Logout,
                        iconTint = Color(0xFFE06060),
                        labelColor = Color(0xFFE06060),
                        label = stringResource(R.string.logout_button),
                        showChevron = false,
                        onClick = onLogoutClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(initial: Char, name: String, role: String) {
    Card(
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
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(RedesAccent.copy(alpha = 0.18f)),
            ) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RedesAccent,
                )
            }
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                if (role.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = roleDisplayName(role),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        fontWeight = FontWeight.Medium,
        color = Color(0xFF6B80A0),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC11131A)),
        border = BorderStroke(1.dp, Color(0x14FFFFFF)),
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color = RedesAccent,
    labelColor: Color = Color.White,
    label: String,
    subtitle: String? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = labelColor,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B80A0),
                )
            }
        }
        if (showChevron && onClick != null) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4A6080),
                modifier = Modifier.size(18.dp),
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
