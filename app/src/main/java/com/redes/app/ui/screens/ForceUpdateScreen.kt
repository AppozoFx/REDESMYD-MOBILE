package com.redes.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesNight
import com.redes.app.ui.theme.RedesNightDeep
import com.redes.app.ui.theme.RedesNightSoft

private const val APP_PACKAGE = "com.redesmyd.mobile"

@Composable
fun ForceUpdateScreen(
    versionNominal: String,
    mensaje: String,
    modifier: Modifier = Modifier,
) {
    // Bloquear el botón atrás — no hay salida sin actualizar
    BackHandler(enabled = true) { }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RedesNight, RedesNightSoft, RedesNightDeep)
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Ícono
            Surface(
                shape = CircleShape,
                color = RedesAccent.copy(alpha = 0.15f),
                modifier = Modifier.size(88.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                        tint = RedesAccent,
                        modifier = Modifier.size(44.dp),
                    )
                }
            }

            // Título
            Text(
                text = "Actualización requerida",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            // Versión mínima si está disponible
            if (versionNominal.isNotBlank()) {
                Surface(
                    color = RedesAccent.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "Versión mínima: $versionNominal",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = RedesAccent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // Mensaje
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCFD8E6),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Botón Play Store
            Button(
                onClick = { openPlayStore(context) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RedesAccent),
            ) {
                Icon(
                    imageVector = Icons.Outlined.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Actualizar ahora",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun openPlayStore(context: android.content.Context) {
    val playStoreUri = Uri.parse("market://details?id=$APP_PACKAGE")
    val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$APP_PACKAGE")
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, playStoreUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
