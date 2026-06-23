@file:OptIn(ExperimentalMaterial3Api::class)

package com.redes.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.redes.app.data.supervisor.*
import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.supervisor.SupervisorTab
import com.redes.app.ui.supervisor.SupervisorUiState

private val BrandBlue = Color(0xFF30518C)
private val BrandBlueDark = Color(0xFF1F3154)
private val GarantiaGold = Color(0xFFFFD54F)
private val SupervisadaGreen = Color(0xFF10B981)

// ─── Estado por estado de orden ────────────────────────────────────────────────
private fun estadoColor(estado: String): Color {
    val norm = estado.uppercase().trim()
    return when {
        norm.contains("FINALIZ") -> Color(0xFF1D4ED8)
        norm.contains("CANCEL") -> Color(0xFFDC2626)
        norm.contains("CAMINO") -> Color(0xFF7C3AED)
        norm.contains("INICI") -> Color(0xFF10B981)
        norm.contains("AGEND") -> Color(0xFF1E293B)
        else -> Color(0xFF475569)
    }
}

// ─── Bottom Navigation ─────────────────────────────────────────────────────────
@Composable
private fun SupervisorBottomNav(
    selected: SupervisorTab,
    onSelect: (SupervisorTab) -> Unit,
) {
    NavigationBar {
        listOf(
            SupervisorTab.INICIO to (Icons.Default.Home to "Inicio"),
            SupervisorTab.ORDENES to (Icons.Default.List to "Órdenes"),
            SupervisorTab.STOCK to (Icons.Default.Inventory to "Stock"),
            SupervisorTab.MAPA to (Icons.Default.Map to "Mapa"),
        ).forEach { (tab, pair) ->
            val (icon, label) = pair
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) },
            )
        }
    }
}

// ─── INICIO Tab — sin scroll ──────────────────────────────────────────────────
@Composable
private fun SupervisorHomeTab(
    state: SupervisorUiState,
    onRefresh: () -> Unit,
    onIniciarRuta: () -> Unit,
    onCerrarRuta: () -> Unit,
    onIniciarRefrigerio: () -> Unit,
    onFinRefrigerio: () -> Unit,
    onShowCuadrillasModal: () -> Unit,
    onDismissJornadaError: () -> Unit,
) {
    val home = state.home
    if (state.isInitialLoading && home == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlue)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Header de perfil ─────────────────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.06f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Avatar circular con inicial
                Box(
                    modifier = Modifier.size(44.dp).background(BrandBlue, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (home?.supervisor?.nombreCorto?.firstOrNull() ?: home?.supervisor?.nombre?.firstOrNull() ?: 'S').uppercaseChar().toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = home?.supervisor?.nombreCorto?.ifBlank { home.supervisor.nombre } ?: "Supervisor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(20.dp), color = BrandBlue.copy(alpha = 0.15f)) {
                            Text("SUPERVISOR", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, color = BrandBlue, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
                        }
                        Text(state.jornada?.ymd ?: todayLimaYmdSup(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
                // Placa del vehículo
                val placa = home?.supervisor?.vehiculoPlaca?.ifBlank { null }
                if (placa != null) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.DirectionsCar, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color(0xFF475569))
                            Text(placa, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }
                    }
                }
            }
        }

        // ── Tarjeta jornada ────────────────────────────────────────────────────
        JornadaCard(
            state = state,
            onIniciarRuta = onIniciarRuta,
            onCerrarRuta = onCerrarRuta,
            onIniciarRefrigerio = onIniciarRefrigerio,
            onFinRefrigerio = onFinRefrigerio,
        )

        // ── Error jornada ──────────────────────────────────────────────────────
        if (state.jornadaError != null) {
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFEF2F2), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))) {
                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    Text(state.jornadaError, fontSize = 12.sp, color = Color(0xFFDC2626), modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismissJornadaError, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                    }
                }
            }
        }

        // ── KPIs 2×2 ──────────────────────────────────────────────────────────
        if (home != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiChip("${home.totales.ordenes}", "Órdenes", BrandBlue, Modifier.weight(1f))
                KpiChip("${home.totales.garantias}", "Garantías", Color(0xFF92400E), Modifier.weight(1f))
                KpiChip("${home.totales.finalizadas}", "Finalizadas", SupervisadaGreen, Modifier.weight(1f))
                KpiChip("${home.totales.pendientes}", "Pendientes", Color(0xFFB45309), Modifier.weight(1f))
            }
        }

        // ── Regiones — fila horizontal scrollable ──────────────────────────────
        if (home != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Regiones hoy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), letterSpacing = 0.6.sp)
                if (home.regionesHoy.isEmpty()) {
                    Text("Sin regiones asignadas hoy", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                } else {
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(home.regionesHoy) { region -> RegionChip(region, BrandBlue) }
                    }
                }
            }
        }

        // ── Cuadrillas — count + botón modal ──────────────────────────────────
        if (home != null) {
            Card(
                onClick = onShowCuadrillasModal,
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(10.dp), color = SupervisadaGreen.copy(alpha = 0.12f)) {
                            Text("${home.cuadrillasHoy.size}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SupervisadaGreen)
                        }
                        Text("cuadrillas hoy", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Ver detalle", fontSize = 12.sp, color = BrandBlue)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

}

@Composable
private fun JornadaCard(
    state: SupervisorUiState,
    onIniciarRuta: () -> Unit,
    onCerrarRuta: () -> Unit,
    onIniciarRefrigerio: () -> Unit,
    onFinRefrigerio: () -> Unit,
) {
    val jornada = state.jornada
    val loading = state.isJornadaLoading || state.isGettingLocation

    val (bgColor, estadoLabel) = when (jornada?.estado) {
        com.redes.app.data.supervisor.JornadaEstado.EN_RUTA -> SupervisadaGreen.copy(alpha = 0.08f) to "En ruta"
        com.redes.app.data.supervisor.JornadaEstado.EN_REFRIGERIO -> Color(0xFFF59E0B).copy(alpha = 0.08f) to "En refrigerio"
        com.redes.app.data.supervisor.JornadaEstado.FINALIZADA -> Color(0xFF64748B).copy(alpha = 0.08f) to "Jornada finalizada"
        else -> Color(0xFFF1F5F9) to "Sin iniciar"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Fila estado + horas
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val dotColor = when (jornada?.estado) {
                        com.redes.app.data.supervisor.JornadaEstado.EN_RUTA -> SupervisadaGreen
                        com.redes.app.data.supervisor.JornadaEstado.EN_REFRIGERIO -> Color(0xFFF59E0B)
                        com.redes.app.data.supervisor.JornadaEstado.FINALIZADA -> Color(0xFF64748B)
                        else -> Color(0xFFCBD5E1)
                    }
                    Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(dotColor))
                    Text(estadoLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    val estadoRuta = state.estadoRuta
                    if (estadoRuta != null) {
                        val badgeColor = if (estadoRuta == "EN_CAMPO") SupervisadaGreen else Color(0xFF64748B)
                        Surface(shape = RoundedCornerShape(20.dp), color = badgeColor.copy(alpha = 0.12f)) {
                            Text(if (estadoRuta == "EN_CAMPO") "En campo" else "Ruta cerrada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = badgeColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (jornada?.horaInicio != null) {
                    Text(
                        buildString {
                            append(jornada.horaInicio)
                            if (jornada.horaFin != null) append(" – ${jornada.horaFin}")
                        },
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }

            // Refrigerio info
            if (jornada?.horaInicioRefrigerio != null) {
                Text(
                    "Refrigerio: ${jornada.horaInicioRefrigerio}${jornada.horaFinRefrigerio?.let { " – $it" } ?: ""}",
                    fontSize = 11.sp, color = Color(0xFFF59E0B),
                )
            }

            // Botones de acción
            if (loading) {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrandBlue, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isGettingLocation) "Obteniendo ubicación..." else "Procesando...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                when (jornada?.estado) {
                    null, com.redes.app.data.supervisor.JornadaEstado.SIN_INICIAR -> {
                        Button(
                            onClick = onIniciarRuta,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Iniciar Ruta", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    com.redes.app.data.supervisor.JornadaEstado.EN_RUTA -> {
                        val yaUsóRefrigerio = jornada?.horaInicioRefrigerio != null
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!yaUsóRefrigerio) {
                                OutlinedButton(
                                    onClick = onIniciarRefrigerio,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                ) {
                                    Icon(Icons.Default.FreeBreakfast, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFF59E0B))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Refrigerio", fontSize = 12.sp, color = Color(0xFFF59E0B))
                                }
                            }
                            Button(
                                onClick = onCerrarRuta,
                                modifier = if (yaUsóRefrigerio) Modifier.fillMaxWidth() else Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cerrar Ruta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    com.redes.app.data.supervisor.JornadaEstado.EN_REFRIGERIO -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onFinRefrigerio,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            ) {
                                Icon(Icons.Default.FreeBreakfast, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Fin Refrigerio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onCerrarRuta,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cerrar Ruta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    com.redes.app.data.supervisor.JornadaEstado.FINALIZADA -> {
                        Surface(shape = RoundedCornerShape(10.dp), color = SupervisadaGreen.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SupervisadaGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Jornada finalizada", color = SupervisadaGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun todayLimaYmdSup(): String =
    java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima")).toLocalDate().toString()

@Composable
private fun KpiChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(1.dp), modifier = modifier) {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

// ─── Banner de actualización ───────────────────────────────────────────────────
@Composable
private fun OrdenesUpdateBanner(state: SupervisorUiState) {
    val info = state.ordersData?.updateInfo ?: return
    val text = info.at?.let { "Órdenes actualizadas: ${formatBannerDateTimeSup(it)}" }
        ?: "Órdenes: sin registro reciente de actualización"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEAF4FF),
        contentColor = Color(0xFF0F3D68),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFBFDBFE))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatBannerDateTimeSup(iso: String): String {
    return runCatching {
        val instant = java.time.Instant.parse(iso)
        val date = instant.atZone(java.time.ZoneId.of("America/Lima"))
        date.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm | dd-MM-yyyy"))
    }.getOrDefault(iso)
}

// ─── ORDENES Tab ───────────────────────────────────────────────────────────────
@Composable
private fun SupervisorOrdenesTab(
    state: SupervisorUiState,
    onOrderClick: (String) -> Unit,
    onToggleGarantias: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        OrdenesUpdateBanner(state)
        // Controls
        Column(Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousDay) { Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior") }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.selectedYmd, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("${state.orders.size} registros", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                }
                IconButton(onClick = onNextDay) { Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente") }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterButton(
                    text = "Órdenes",
                    selected = !state.showGarantias,
                    onClick = { if (state.showGarantias) onToggleGarantias() },
                    modifier = Modifier.weight(1f),
                )
                FilterButton(
                    text = "Garantías",
                    selected = state.showGarantias,
                    onClick = { if (!state.showGarantias) onToggleGarantias() },
                    modifier = Modifier.weight(1f),
                    selectedColor = GarantiaGold,
                )
            }
        }
        HorizontalDivider()

        if (state.isOrdersLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
            return@Column
        }

        if (state.orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (state.showGarantias) "Sin garantías para esta fecha" else "Sin órdenes para esta fecha", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp)
            }
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.orders, key = { it.id }) { order ->
                SupervisorOrderCard(order = order, onClick = { onOrderClick(order.id) })
            }
        }
    }
}

@Composable
private fun SupervisorOrderCard(order: SupervisorOrderSummary, onClick: () -> Unit) {
    val borderColor = when {
        order.isGarantia -> GarantiaGold
        else -> Color.Transparent
    }
    val statusColor = estadoColor(order.estado)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().border(if (order.isGarantia) 2.dp else 0.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Estado dot
            Box(Modifier.size(10.dp).clip(CircleShape).background(statusColor).align(Alignment.Top).padding(top = 4.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${order.fechaProgramadaHm.takeIf { it.isNotBlank() }?.let { "$it · " } ?: ""}${order.cliente}",
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    if (order.isGarantia) {
                        Text("GARANTÍA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E),
                            modifier = Modifier.background(GarantiaGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.1f)) {
                    Text(order.estado.ifBlank { "Sin estado" }, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                if (order.direccion.isNotBlank()) {
                    Text(order.direccion, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (order.cuadrillaNombre.isNotBlank()) {
                        Text("🚐 ${order.cuadrillaNombre}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                    }
                    if (order.region.isNotBlank()) {
                        Text("· ${order.region}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (order.hasSupervision) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(SupervisadaGreen))
                        Text("Supervisada", fontSize = 11.sp, color = SupervisadaGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ─── STOCK Tab (placeholder) ───────────────────────────────────────────────────
@Composable
private fun SupervisorStockTab() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            Text("Stock del Supervisor", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text("Próximamente disponible", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

// ─── MAPA Tab ─────────────────────────────────────────────────────────────────
@SuppressLint("MissingPermission")
@Composable
private fun SupervisorMapaTab(
    state: SupervisorUiState,
    onSetMode: (SupervisorMapMode) -> Unit,
    onSelectCuadrilla: (String?) -> Unit,
    onRefreshCuadrillas: () -> Unit,
    onRefresh: () -> Unit,
) {
    val context = LocalContext.current
    var selectedMapItem by remember { mutableStateOf<SupervisorMapItem?>(null) }
    var selectedCuadrilla by remember { mutableStateOf<CuadrillaMapa?>(null) }
    var cuadrillaFilter by remember { mutableStateOf("") }
    var estadoFilter by remember { mutableStateOf("") }

    val locationGranted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Auto-refresh cuadrillas cada 30 s en modo CUADRILLAS
    LaunchedEffect(state.mapMode) {
        if (state.mapMode == SupervisorMapMode.CUADRILLAS) {
            while (true) {
                kotlinx.coroutines.delay(30_000L)
                onRefreshCuadrillas()
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.046374, -77.042793), 11f)
    }

    // Computed: items activos según modo
    val activeItems = remember(state.mapMode, state.mapItems, state.allMapItems, cuadrillaFilter, estadoFilter) {
        if (state.mapMode == SupervisorMapMode.CUADRILLAS) {
            state.allMapItems
                .let { list -> if (cuadrillaFilter.isBlank()) list else list.filter { it.cuadrillaNombre.contains(cuadrillaFilter.trim(), ignoreCase = true) } }
                .let { list ->
                    when (estadoFilter) {
                        "INICIADA"   -> list.filter { it.estado.uppercase().let { e -> e.contains("INICI") || e == "EN CAMINO" } }
                        "FINALIZADA" -> list.filter { it.estado.uppercase().contains("FINAL") }
                        "AGENDADA"   -> list.filter { it.estado.uppercase() == "AGENDADA" }
                        else -> list
                    }
                }
        } else {
            state.mapItems
        }
    }

    val hayFiltro = cuadrillaFilter.isNotBlank() || estadoFilter.isNotBlank()

    // Auto-fit bounds
    LaunchedEffect(state.mapItems, state.allMapItems) {
        val items = if (state.mapMode == SupervisorMapMode.CUADRILLAS) state.allMapItems else state.mapItems
        if (items.size >= 2 && !hayFiltro) {
            val bounds = LatLngBounds.builder()
            items.forEach { bounds.include(LatLng(it.lat, it.lng)) }
            runCatching {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80), durationMs = 700)
            }
        } else if (items.size == 1) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(items[0].lat, items[0].lng), 13f), durationMs = 700)
        }
    }

    // Zoom a filtrado activo en CUADRILLAS
    LaunchedEffect(activeItems) {
        if (state.mapMode == SupervisorMapMode.CUADRILLAS && hayFiltro && activeItems.isNotEmpty()) {
            if (activeItems.size == 1) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(activeItems[0].lat, activeItems[0].lng), 15f))
            } else {
                val bounds = LatLngBounds.builder()
                activeItems.forEach { bounds.include(LatLng(it.lat, it.lng)) }
                runCatching { cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80), durationMs = 700) }
            }
        }
    }

    // Auto-fit cuadrillas al cambiar a modo CUADRILLAS
    LaunchedEffect(state.cuadrillasMapa, state.mapMode) {
        if (state.mapMode != SupervisorMapMode.CUADRILLAS) return@LaunchedEffect
        val cuads = state.cuadrillasMapa.filter { it.lat != null && it.lng != null }
        if (cuads.size >= 2) {
            val bounds = LatLngBounds.builder()
            cuads.forEach { bounds.include(LatLng(it.lat!!, it.lng!!)) }
            runCatching {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80), durationMs = 700)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        OrdenesUpdateBanner(state)
        // Selector de modo — SegmentedButtonRow (3 modos)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = state.mapMode == SupervisorMapMode.MIS_ORDENES,
                    onClick = { if (state.mapMode != SupervisorMapMode.MIS_ORDENES) onSetMode(SupervisorMapMode.MIS_ORDENES) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                ) { Text("Mis órdenes", fontSize = 12.sp) }
                SegmentedButton(
                    selected = state.mapMode == SupervisorMapMode.GARANTIAS,
                    onClick = { if (state.mapMode != SupervisorMapMode.GARANTIAS) onSetMode(SupervisorMapMode.GARANTIAS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                ) { Text("Garantías", fontSize = 12.sp) }
                SegmentedButton(
                    selected = state.mapMode == SupervisorMapMode.CUADRILLAS,
                    onClick = { if (state.mapMode != SupervisorMapMode.CUADRILLAS) onSetMode(SupervisorMapMode.CUADRILLAS) },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                ) { Text("Cuadrillas", fontSize = 12.sp) }
            }
            IconButton(onClick = { if (state.mapMode == SupervisorMapMode.CUADRILLAS) onRefreshCuadrillas() else onRefresh() }) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar mapa")
            }
        }

        // Filtros modo CUADRILLAS (igual que Coordinador)
        if (state.mapMode == SupervisorMapMode.CUADRILLAS) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = cuadrillaFilter,
                    onValueChange = { cuadrillaFilter = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filtrar órdenes por cuadrilla", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (cuadrillaFilter.isNotBlank()) {
                        { IconButton(onClick = { cuadrillaFilter = "" }) { Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp)) } }
                    } else null,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelSmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("AGENDADA" to "Agendada", "INICIADA" to "Iniciada", "FINALIZADA" to "Finalizada").forEach { (key, label) ->
                        FilterChip(
                            selected = estadoFilter == key,
                            onClick = { estadoFilter = if (estadoFilter == key) "" else key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (estadoFilter == key) { { Icon(Icons.Outlined.Check, null, modifier = Modifier.size(14.dp)) } } else null,
                        )
                    }
                    if (cuadrillaFilter.isNotBlank() || estadoFilter.isNotBlank()) {
                        TextButton(onClick = { cuadrillaFilter = ""; estadoFilter = "" }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                            Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Mapa
        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationGranted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = locationGranted, zoomControlsEnabled = false),
                onMapClick = { selectedMapItem = null; selectedCuadrilla = null },
            ) {
                // Marcadores de órdenes (todos los modos)
                activeItems.forEach { item ->
                    key(item.id) {
                        MarkerComposable(
                            keys = arrayOf(item.estado, item.isGarantia, item.hasSupervision),
                            state = rememberMarkerState(position = LatLng(item.lat, item.lng)),
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            onClick = { selectedMapItem = item; selectedCuadrilla = null; true },
                        ) {
                            SupervisorOrderMarkerIcon(estado = item.estado, isGarantia = item.isGarantia, supervisada = item.hasSupervision)
                        }
                    }
                }

                // Marcadores de cuadrillas (modo CUADRILLAS — igual que Coordinador)
                if (state.mapMode == SupervisorMapMode.CUADRILLAS) {
                    val filteredCuads = state.cuadrillasMapa.filter { cuad ->
                        cuad.lat != null && cuad.lng != null &&
                            (cuadrillaFilter.isBlank() || cuad.nombre.contains(cuadrillaFilter, ignoreCase = true))
                    }
                    filteredCuads.forEach { cuad ->
                        key("cuad-${cuad.id}") {
                            MarkerComposable(
                                keys = arrayOf(cuad.estadoActual),
                                state = rememberMarkerState(position = LatLng(cuad.lat!!, cuad.lng!!)),
                                anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                                onClick = { selectedCuadrilla = cuad; selectedMapItem = null; true },
                            ) {
                                CuadrillaMarkerIcon(cuad.estadoActual)
                            }
                        }
                    }
                }
            }

            // Popup orden seleccionada
            selectedMapItem?.let { item ->
                SupervisorMapOrderPopup(
                    item = item,
                    onDismiss = { selectedMapItem = null },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            // Popup cuadrilla seleccionada (reutiliza el del Técnico/Coordinador)
            selectedCuadrilla?.let { cuad ->
                MapCuadrillaPopup(
                    cuadrilla = cuad,
                    onDismiss = { selectedCuadrilla = null },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            // Estado vacío
            val showEmpty = !state.isMapLoading && !state.isCuadrillasMapaLoading && activeItems.isEmpty()
            if (showEmpty) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = when {
                            hayFiltro -> "Sin órdenes con ese filtro"
                            state.mapMode == SupervisorMapMode.GARANTIAS -> "Sin garantías con coordenadas"
                            else -> "Sin órdenes con coordenadas"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (state.isCuadrillasMapaLoading || (state.mapMode == SupervisorMapMode.CUADRILLAS && state.isMapLoading)) {
                CenteredLoadingOverlay(modifier = Modifier.fillMaxSize(), message = "Cargando cuadrillas...")
            } else if (state.isMapLoading) {
                CenteredLoadingOverlay(modifier = Modifier.fillMaxSize(), message = "Actualizando mapa...")
            }
        }
    }
}

@Composable
private fun SupervisorOrderMarkerIcon(estado: String, isGarantia: Boolean, supervisada: Boolean) {
    val (bgColor, icon) = when (estado.trim().uppercase()) {
        "FINALIZADA"            -> Color(0xFF1565C0) to Icons.Outlined.CheckCircle
        "CANCELADA", "ANULADA" -> Color(0xFFB71C1C) to Icons.Outlined.Block
        "EN CAMINO"             -> Color(0xFF6A1B9A) to Icons.Outlined.NearMe
        "INICIADA"              -> Color(0xFF1B5E20) to Icons.Outlined.Build
        "AGENDADA"              -> Color(0xFF424242) to Icons.Outlined.Schedule
        else                    -> Color(0xFF00695C) to Icons.Outlined.Place
    }
    val ringColor = when {
        supervisada  -> Color(0xFF10B981) // verde supervisada
        isGarantia   -> GarantiaGold      // dorado garantía
        else         -> Color.White
    }
    val ringWidth = if (isGarantia || supervisada) 3.dp else 2.dp
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(bgColor, CircleShape)
            .border(ringWidth, ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SupervisorMapOrderPopup(
    item: SupervisorMapItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.cliente.ifBlank { item.ordenId }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (item.isGarantia) {
                            Surface(shape = RoundedCornerShape(6.dp), color = GarantiaGold.copy(alpha = 0.2f)) {
                                Text("GARANTÍA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E), modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                            }
                        }
                    }
                    if (item.estado.isNotBlank()) Text(item.estado, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (item.cuadrillaNombre.isNotBlank()) Text("🚐 ${item.cuadrillaNombre}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.region.isNotBlank()) Text("📍 ${item.region}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (item.hasSupervision) {
                        Text("✓ Supervisada", style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                }
            }
            if (item.direccion.isNotBlank()) {
                Text(item.direccion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = {
                    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${item.lat},${item.lng}")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) {
                Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Abrir en Google Maps")
            }
        }
    }
}

// ─── Shell principal ───────────────────────────────────────────────────────────
@Composable
fun SupervisorShellScreen(
    uiState: SupervisorUiState,
    onTabSelected: (SupervisorTab) -> Unit,
    onRefreshClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    onToggleGarantias: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onSetMapMode: (SupervisorMapMode) -> Unit,
    onSelectCuadrilla: (String?) -> Unit,
    onRefreshCuadrillasMapa: () -> Unit,
    onCloseRouteClick: () -> Unit,
    onIniciarRuta: () -> Unit,
    onConfirmarCerrarRuta: () -> Unit,
    onDismissCerrarRuta: () -> Unit,
    onIniciarRefrigerio: () -> Unit,
    onFinRefrigerio: () -> Unit,
    onShowCuadrillasModal: () -> Unit,
    onHideCuadrillasModal: () -> Unit,
    onDismissJornadaError: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAlertsClick: () -> Unit = {},
    onDismissAlertas: () -> Unit = {},
) {
    val home = uiState.home

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Supervisor",
                notifCount = uiState.alertCount,
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            SupervisorBottomNav(selected = uiState.selectedTab, onSelect = onTabSelected)
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshClick,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when (uiState.selectedTab) {
                SupervisorTab.INICIO -> SupervisorHomeTab(
                    state = uiState,
                    onRefresh = onRefreshClick,
                    onIniciarRuta = onIniciarRuta,
                    onCerrarRuta = onCloseRouteClick,
                    onIniciarRefrigerio = onIniciarRefrigerio,
                    onFinRefrigerio = onFinRefrigerio,
                    onShowCuadrillasModal = onShowCuadrillasModal,
                    onDismissJornadaError = onDismissJornadaError,
                )
                SupervisorTab.ORDENES -> SupervisorOrdenesTab(
                    state = uiState,
                    onOrderClick = onOrderClick,
                    onToggleGarantias = onToggleGarantias,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
                    onRefresh = onRefreshClick,
                )
                SupervisorTab.STOCK -> SupervisorStockTab()
                SupervisorTab.MAPA -> SupervisorMapaTab(
                    state = uiState,
                    onSetMode = onSetMapMode,
                    onSelectCuadrilla = onSelectCuadrilla,
                    onRefreshCuadrillas = onRefreshCuadrillasMapa,
                    onRefresh = onRefreshClick,
                )
            }

            // Error snackbar
            if (uiState.errorMessage != null) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = {}) { Text("OK") } },
                ) {
                    Text(uiState.errorMessage, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    // ── Diálogo confirmar cerrar ruta ──────────────────────────────────────────
    if (uiState.showCerrarRutaConfirm) {
        AlertDialog(
            onDismissRequest = onDismissCerrarRuta,
            icon = { Icon(Icons.Default.Stop, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Cerrar Ruta", fontWeight = FontWeight.Bold) },
            text = { Text("Al cerrar ruta se registrará la hora de fin de tu jornada y se detendrá el tracking de ubicación.\n\n¿Confirmas?") },
            confirmButton = {
                Button(
                    onClick = onConfirmarCerrarRuta,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Sí, cerrar ruta", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = onDismissCerrarRuta) { Text("Cancelar") }
            },
        )
    }

    // ── Modal cuadrillas ──────────────────────────────────────────────────────
    if (uiState.showCuadrillasModal && uiState.home != null) {
        AlertDialog(
            onDismissRequest = onHideCuadrillasModal,
            title = { Text("Cuadrillas de hoy (${uiState.home.cuadrillasHoy.size})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.home.cuadrillasHoy.forEach { cuad ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(cuad.nombre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${cuad.ordenesTotal} órdenes · ${cuad.garantiasTotal} garantías", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                            }
                            if (cuad.estadoActual.isNotBlank()) {
                                val c = if (cuad.estadoActual == "EN_RUTA") Color(0xFF7C3AED) else SupervisadaGreen
                                Surface(shape = RoundedCornerShape(8.dp), color = c.copy(alpha = 0.1f)) {
                                    Text(cuad.estadoActual.replace("_", " "), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), fontSize = 10.sp, color = c, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    if (uiState.home.cuadrillasHoy.isEmpty()) {
                        Text("Sin cuadrillas asignadas hoy", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            },
            confirmButton = { TextButton(onClick = onHideCuadrillasModal) { Text("Cerrar") } },
        )
    }

    // ── Diálogo alertas ───────────────────────────────────────────────────────
    if (uiState.showAlertas) {
        AlertDialog(
            onDismissRequest = onDismissAlertas,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                    Text("Alertas de garantías", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (uiState.alertas.isEmpty()) {
                    Text("Sin alertas pendientes.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.alertas.take(10).forEach { alerta ->
                            val (bgColor, textColor, icon) = when (alerta.tipo) {
                                "GARANTIA_NUEVA"  -> Triple(Color(0xFFFFFBEB), Color(0xFF92400E), Icons.Default.AddAlert)
                                "GARANTIA_ESTADO" -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Default.Update)
                                "GARANTIA_TRAMO"  -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Icons.Default.Warning)
                                else              -> Triple(Color(0xFFF8FAFC), Color(0xFF475569), Icons.Default.Notifications)
                            }
                            Surface(shape = RoundedCornerShape(10.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                    Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(15.dp).padding(top = 2.dp))
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(alerta.titulo, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = textColor)
                                        Text(alerta.mensaje, fontSize = 11.sp, color = textColor.copy(alpha = 0.8f))
                                        val elapsed = (System.currentTimeMillis() - alerta.creadoAt) / 60_000
                                        val timeLabel = when {
                                            elapsed < 1   -> "ahora"
                                            elapsed < 60  -> "hace ${elapsed}m"
                                            else          -> "hace ${elapsed / 60}h"
                                        }
                                        Text(timeLabel, fontSize = 10.sp, color = textColor.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissAlertas) { Text("Cerrar") }
            },
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), letterSpacing = 0.8.sp)
            content()
        }
    }
}

@Composable
private fun StatChip(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.8f))
    }
}

@Composable
private fun RegionChip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.1f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FlowRow(items: List<String>, chip: @Composable (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { chip(it) }
    }
}

@Composable
private fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, selectedColor: Color = BrandBlue) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
    }
}
