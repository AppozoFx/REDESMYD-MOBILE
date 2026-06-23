package com.redes.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NotificationsActive
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redes.app.R
import com.redes.app.data.tecnico.NotifTecnicoItem
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.tecnico.TecnicoUiState
import com.redes.app.ui.theme.RedesAccent
import com.redes.app.ui.theme.RedesNight
import com.redes.app.ui.theme.RedesNightDeep
import com.redes.app.ui.theme.RedesNightSoft
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun tipoLabel(tipo: String) = when (tipo.uppercase()) {
    "CERRAR_RUTA_APROBADA"  -> "Solicitud de cierre aprobada"
    "ATENCION_ATENDIDA"     -> "Solicitud de atención atendida"
    "REQUIERE_ATENCION"     -> "Requiere atención"
    "ORDEN_NUEVA"              -> "Nueva orden asignada"
    "ORDEN_QUITADA"            -> "Orden retirada"
    "ESTADO_ORDEN"             -> "Cambio de estado"
    "TRAMO_ALERTA"             -> "Alerta de tramo"
    "ORDENES_ACTUALIZADAS"     -> "Órdenes actualizadas"
    "CIERRE_RUTA_RECORDATORIO" -> "Cierra tu ruta"
    else -> tipo
}

private fun formatNotifTime(millis: Long?): String {
    if (millis == null) return ""
    val diff = System.currentTimeMillis() - millis
    val min = diff / 60_000
    return when {
        min < 1 -> "hace unos segundos"
        min < 60 -> "hace ${min} min"
        min < 1440 -> "hace ${min / 60} h"
        else -> SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(millis))
    }
}

private fun dayLabel(millis: Long): String {
    val itemCal = Calendar.getInstance().also { it.timeInMillis = millis }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }

    fun Calendar.isSameDayAs(other: Calendar) =
        get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)

    return when {
        itemCal.isSameDayAs(todayCal) -> "Hoy"
        itemCal.isSameDayAs(yesterdayCal) -> "Ayer"
        else -> SimpleDateFormat("EEE d MMM", Locale("es")).format(Date(millis))
            .replaceFirstChar { it.uppercaseChar() }
    }
}

@Composable
fun NotificationsScreen(
    uiState: HomeUiState,
    tecnicoUiState: TecnicoUiState? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val notifItems = tecnicoUiState?.notifItems ?: emptyList()

    // Agrupar alertas de gestión por día
    val notifByDay: List<Pair<String, List<NotifTecnicoItem>>> = notifItems
        .groupBy { item -> item.creadoAt?.let { dayLabel(it) } ?: "Sin fecha" }
        .entries
        .toList()
        .sortedByDescending { (_, items) -> items.maxOfOrNull { it.creadoAt ?: 0L } }
        .map { (label, items) -> label to items }

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
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            }
            HorizontalDivider(color = Color(0x12FFFFFF))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── Alertas de gestión ──
                item {
                    NotifSectionHeader(
                        title = "Alertas de gestión",
                        icon = Icons.Outlined.NotificationsActive,
                        unreadCount = notifItems.count { !it.leido },
                    )
                }

                if (notifItems.isEmpty()) {
                    item { NotifEmptyState("Sin alertas de gestión") }
                } else {
                    notifByDay.forEach { (dayLabel, dayItems) ->
                        item(key = "day_$dayLabel") {
                            NotifDayLabel(label = dayLabel)
                        }
                        items(dayItems, key = { it.id }) { item ->
                            NotifTecnicoCard(item = item)
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        color = Color(0x10FFFFFF),
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                // ── Comunicados ──
                item {
                    NotifSectionHeader(
                        title = "Comunicados",
                        icon = Icons.Outlined.Campaign,
                    )
                }
                if (uiState.comunicados.isEmpty()) {
                    item { NotifEmptyState(stringResource(R.string.notifications_empty)) }
                } else {
                    items(uiState.comunicados, key = { it.id }) { comunicado ->
                        ComunicadoCard(
                            titulo = comunicado.titulo,
                            obligatorio = comunicado.obligatorio,
                            persistencia = comunicado.persistencia,
                            cuerpo = comunicado.cuerpo,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifSectionHeader(
    title: String,
    icon: ImageVector,
    unreadCount: Int = 0,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 2.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(RedesAccent.copy(alpha = 0.12f)),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RedesAccent,
                modifier = Modifier.size(15.dp),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
        if (unreadCount > 0) {
            Surface(
                color = RedesAccent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(99.dp),
            ) {
                Text(
                    text = "$unreadCount sin leer",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                    color = RedesAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun NotifDayLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
        fontWeight = FontWeight.Medium,
        color = Color(0xFF6B80A0),
        modifier = Modifier.padding(start = 2.dp, top = 4.dp, bottom = 2.dp),
    )
}

@Composable
private fun NotifEmptyState(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x0AFFFFFF))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = Color(0xFF4A6080),
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF94A3B8),
        )
    }
}

@Composable
private fun NotifTecnicoCard(item: NotifTecnicoItem) {
    val (badgeBg, badgeFg) = when (item.tipo.uppercase()) {
        "CERRAR_RUTA_APROBADA", "ATENCION_ATENDIDA", "ORDEN_NUEVA", "ORDENES_ACTUALIZADAS" ->
            Color(0x1A3FB68A) to Color(0xFF6BDBA6)
        "ORDEN_QUITADA" ->
            Color(0x1AE06060) to Color(0xFFE09898)
        "CIERRE_RUTA_RECORDATORIO" ->
            Color(0x1AE06060) to Color(0xFFE09898)
        "TRAMO_ALERTA" ->
            Color(0x1A4D9DE0) to Color(0xFF7AB8E0)
        else ->
            Color(0x1AE0A84D) to Color(0xFFE0C07A)
    }

    // Unread: borde más visible y un punto de color en el header
    val isUnread = !item.leido
    val cardBorder = if (isUnread)
        BorderStroke(1.dp, RedesAccent.copy(alpha = 0.35f))
    else
        BorderStroke(1.dp, Color(0x14FFFFFF))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) Color(0xE011131A) else Color(0xAA11131A)
        ),
        border = cardBorder,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(99.dp),
                ) {
                    Text(
                        text = tipoLabel(item.tipo),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeFg,
                    )
                }
                // Punto azul para no leídos
                if (isUnread) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(RedesAccent),
                    )
                }
            }
            Text(
                text = item.titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUnread) Color.White else Color(0xCCFFFFFF),
            )
            if (item.mensaje.isNotBlank()) {
                Text(
                    text = item.mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                )
            }
            if (item.creadoAt != null) {
                Text(
                    text = formatNotifTime(item.creadoAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4A6080),
                )
            }
        }
    }
}

@Composable
private fun ComunicadoCard(
    titulo: String,
    obligatorio: Boolean,
    persistencia: String,
    cuerpo: String,
) {
    val (badgeBg, badgeFg) = if (obligatorio) {
        Color(0x1AE06060) to Color(0xFFE09898)
    } else {
        Color(0x1A4D9DE0) to Color(0xFF7AB8E0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC11131A)),
        border = BorderStroke(1.dp, Color(0x14FFFFFF)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(99.dp),
                ) {
                    Text(
                        text = if (obligatorio) "Obligatorio" else "Informativo",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeFg,
                    )
                }
                if (persistencia.isNotBlank()) {
                    Text(
                        text = persistencia,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4A6080),
                    )
                }
            }
            Text(
                text = titulo.ifBlank { "Comunicado" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            if (cuerpo.isNotBlank()) {
                Text(
                    text = cuerpo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCFD8E6),
                )
            }
        }
    }
}
