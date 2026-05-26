package com.redes.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = { androidx.compose.material3.Text(title) },
        actions = {
            IconButton(onClick = onAlertsClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Alertas y notificaciones",
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Cuenta y configuracion",
                )
            }
        },
    )
}
