package com.redes.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SwitchAccount
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redes.app.BuildConfig
import com.redes.app.R
import com.redes.app.ui.common.preferredPersonName
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesNight
import com.redes.app.ui.theme.RedesNightDeep
import com.redes.app.ui.theme.RedesNightSoft

@Composable
fun ProfileScreen(
    uiState: HomeUiState,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = uiState.backendSession
    val displayName = session?.let { preferredPersonName(it.nombreCorto, it.nombre) }
        ?: uiState.firebaseUser?.email
        ?: "—"
    val email = session?.email ?: uiState.firebaseUser?.email ?: "—"
    val role = uiState.selectedRole ?: uiState.defaultRole ?: ""
    val roles = session?.roles.orEmpty()
    val areas = session?.areas.orEmpty()
    val permissions = session?.permissions.orEmpty()
    val estadoAcceso = session?.estadoAcceso ?: "—"
    val isAdmin = session?.isAdmin == true
    val uid = uiState.firebaseUser?.uid

    val parts = displayName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val initials = when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}"
        parts.size == 1 -> "${parts[0].first()}"
        else -> "?"
    }.uppercase()

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
            // ── Header ──
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
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onAlertsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Alertas",
                        tint = Color(0xFF94A3B8),
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configuración",
                        tint = Color(0xFF94A3B8),
                    )
                }
            }
            HorizontalDivider(color = Color(0x12FFFFFF))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 28.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ── Hero ──
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x404D9DE0), Color.Transparent)
                                )
                            )
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(RedesAccent.copy(alpha = 0.18f)),
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = RedesAccent,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge.copy(letterSpacing = (-0.2).sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Role + admin badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (role.isNotBlank()) {
                        ProfileChip(
                            text = roleDisplayName(role),
                            color = roleAccentColor(role),
                        )
                    }
                    if (isAdmin) {
                        ProfileChip(
                            text = "Admin",
                            color = Color(0xFFE0A84D),
                            icon = Icons.Outlined.AdminPanelSettings,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ── Datos personales ──
                ProfileSectionLabel(stringResource(R.string.profile_section_account))
                ProfileCard {
                    ProfileRow(
                        icon = Icons.Outlined.AlternateEmail,
                        label = "Correo",
                    ) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                        )
                    }
                    ProfileDivider()
                    ProfileRow(
                        icon = Icons.Outlined.Shield,
                        label = "Estado de acceso",
                    ) {
                        AccessStateBadge(estadoAcceso)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Roles y Acceso ──
                ProfileSectionLabel(stringResource(R.string.profile_section_access))
                ProfileCard {
                    ProfileRow(
                        icon = Icons.Outlined.SwitchAccount,
                        label = "Roles asignados",
                    ) {
                        if (roles.isEmpty()) {
                            Text("—", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
                        } else {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                roles.forEach { r ->
                                    ProfileChip(
                                        text = roleDisplayName(r),
                                        color = roleAccentColor(r),
                                    )
                                }
                            }
                        }
                    }
                    if (areas.isNotEmpty()) {
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.Map,
                            label = "Áreas",
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                areas.forEach { area ->
                                    ProfileChip(
                                        text = area,
                                        color = Color(0xFF4DCCE0),
                                    )
                                }
                            }
                        }
                    }
                    if (permissions.isNotEmpty()) {
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.VpnKey,
                            label = "Permisos",
                        ) {
                            Text(
                                text = "${permissions.size} permisos activos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Sesión técnica ──
                ProfileSectionLabel(stringResource(R.string.profile_base_url_label))
                ProfileCard {
                    ProfileRow(
                        icon = if (uiState.isUsingCachedSession) Icons.Outlined.CloudOff else Icons.Outlined.Cloud,
                        iconTint = if (uiState.isUsingCachedSession) Color(0xFFE0A84D) else Color(0xFF3FB68A),
                        label = "Fuente de datos",
                    ) {
                        Text(
                            text = if (uiState.isUsingCachedSession) "Caché local" else "Backend",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.isUsingCachedSession) Color(0xFFE0A84D) else Color(0xFF3FB68A),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    ProfileDivider()
                    ProfileRow(
                        icon = Icons.Outlined.Code,
                        label = "Entorno",
                    ) {
                        Text(
                            text = BuildConfig.API_ENVIRONMENT.replaceFirstChar { it.uppercaseChar() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                        )
                    }
                    if (uid != null) {
                        ProfileDivider()
                        ProfileRow(
                            icon = Icons.Outlined.Fingerprint,
                            label = "ID de usuario",
                        ) {
                            Text(
                                text = if (uid.length > 14) "${uid.take(14)}…" else uid,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = null),
                                color = Color(0xFF94A3B8),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────

@Composable
private fun ProfileSectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        fontWeight = FontWeight.Medium,
        color = Color(0xFF6B80A0),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 6.dp),
    )
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
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
private fun ProfileDivider() {
    HorizontalDivider(
        color = Color(0x0EFFFFFF),
        modifier = Modifier.padding(start = 66.dp),
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    iconTint: Color = RedesAccent,
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6B80A0),
            )
            Spacer(modifier = Modifier.height(3.dp))
            content()
        }
    }
}

@Composable
private fun ProfileChip(
    text: String,
    color: Color,
    icon: ImageVector? = null,
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(99.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
            )
        }
    }
}

@Composable
private fun AccessStateBadge(estadoAcceso: String) {
    val (color, label) = when (estadoAcceso.uppercase()) {
        "HABILITADO" -> Color(0xFF3FB68A) to "Habilitado"
        "INHABILITADO" -> Color(0xFFE06060) to "Inhabilitado"
        "SUSPENDIDO" -> Color(0xFFE0A84D) to "Suspendido"
        else -> Color(0xFF94A3B8) to estadoAcceso.lowercase().replaceFirstChar { it.uppercaseChar() }
    }
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(99.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
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

private fun roleAccentColor(role: String): Color = when (role.uppercase()) {
    "TECNICO" -> Color(0xFF4D9DE0)
    "COORDINADOR" -> Color(0xFF3FB68A)
    "SUPERVISOR" -> Color(0xFF9B7AE0)
    "ADMIN" -> Color(0xFFE06060)
    "GERENCIA" -> Color(0xFFE0A84D)
    "SEGURIDAD" -> Color(0xFF4DCCE0)
    else -> Color(0xFF4D9DE0)
}
