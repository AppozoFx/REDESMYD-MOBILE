package com.redes.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.redes.app.data.coordinador.*
import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.coordinador.*

/* ================================================================
   SHELL PRINCIPAL
   ================================================================ */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinadorShellScreen(
    uiState: CoordinadorUiState,
    onTabSelected: (CoordinadorTab) -> Unit,
    onRefreshClick: () -> Unit,
    onAlmacenSubTab: (AlmacenSubTab) -> Unit,
    onGestionSubTab: (GestionSubTab) -> Unit,
    onToggleCuadrillasView: () -> Unit,
    onToggleMapaMode: () -> Unit,
    onSelectCuadrilla: (String?) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleCuadrillaExpanded: (String) -> Unit,
    onToggleStockExpanded: (String) -> Unit,
    onToggleAuditoriaExpanded: (String) -> Unit,
    onAuditoriaSustain: (cuadrillaId: String, sn: String, uri: Uri) -> Unit,
    onOrdenClick: (String) -> Unit = {},
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onDateSelected: (String) -> Unit = {},
    onDismissError: () -> Unit = {},
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Coordinador",
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            NavigationBar {
                CoordinadorTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
                        label = { Text(tab.label(), maxLines = 1) },
                    )
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshClick,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.errorMessage != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = onDismissError, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Close, "Cerrar error", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                when (uiState.selectedTab) {
                    CoordinadorTab.INICIO -> CoordInicioTab(
                        uiState = uiState,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        modifier = Modifier.weight(1f),
                    )
                    CoordinadorTab.CUADRILLAS -> CoordCuadrillasTab(
                        uiState = uiState,
                        onToggleView = onToggleCuadrillasView,
                        onToggleMapaMode = onToggleMapaMode,
                        onToggleCuadrillaExpanded = onToggleCuadrillaExpanded,
                        onPreviousDay = onPreviousDay,
                        onNextDay = onNextDay,
                        onDateSelected = onDateSelected,
                        onOrdenClick = onOrdenClick,
                        modifier = Modifier.weight(1f),
                    )
                    CoordinadorTab.ALMACEN -> CoordAlmacenTab(
                        uiState = uiState,
                        onSubTab = onAlmacenSubTab,
                        onToggleStockExpanded = onToggleStockExpanded,
                        onToggleAuditoriaExpanded = onToggleAuditoriaExpanded,
                        onAuditoriaSustain = onAuditoriaSustain,
                        modifier = Modifier.weight(1f),
                    )
                    CoordinadorTab.GESTION -> CoordGestionTab(
                        uiState = uiState,
                        onSubTab = onGestionSubTab,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/* ================================================================
   TAB 1: INICIO — KPIs mensuales
   ================================================================ */
@Composable
private fun CoordInicioTab(
    uiState: CoordinadorUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedCuadrilla by remember { mutableStateOf<String?>(null) }
    val resumen = uiState.resumen

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onPreviousMonth) { Icon(Icons.Outlined.ChevronLeft, "Mes anterior") }
                Text(uiState.selectedYm, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onNextMonth) { Icon(Icons.Outlined.ChevronRight, "Mes siguiente") }
            }
        }

        if (uiState.isResumenLoading) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else if (resumen != null) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard("Instalaciones", resumen.totales.finalizadas.toString(), Icons.Outlined.CheckCircle, Color(0xFF1565C0), Modifier.weight(1f))
                    KpiCard("Garantías", resumen.totales.garantias.toString(), Icons.Outlined.Build, Color(0xFFE65100), Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiCard("CAT6", resumen.totales.cat6.toString(), Icons.Outlined.Cable, Color(0xFF6A1B9A), Modifier.weight(1f))
                    KpiCard("CAT5e", resumen.totales.cat5e.toString(), Icons.Outlined.Cable, Color(0xFF00695C), Modifier.weight(1f))
                    KpiCard("Ventas", resumen.totales.ventas.toString(), Icons.Outlined.Sell, Color(0xFF2E7D32), Modifier.weight(1f))
                }
            }
            item {
                Text("Por cuadrilla", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
            }
            items(resumen.cuadrillas, key = { it.cuadrillaId }) { cuad ->
                CuadrillaKpiRow(
                    cuad = cuad,
                    isExpanded = expandedCuadrilla == cuad.cuadrillaId,
                    onToggle = { expandedCuadrilla = if (expandedCuadrilla == cuad.cuadrillaId) null else cuad.cuadrillaId },
                )
            }
        } else {
            item { Text("Sin datos para este mes.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun KpiCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CuadrillaKpiRow(cuad: CoordinadorCuadrillaKpi, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(cuad.cuadrillaNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${cuad.finalizadas} inst.", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1565C0))
                    if (cuad.garantias > 0) Text("${cuad.garantias} gar.", style = MaterialTheme.typography.labelMedium, color = Color(0xFFE65100))
                    if (cuad.cat6 > 0) Text("CAT6:${cuad.cat6}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF6A1B9A))
                    if (cuad.cat5e > 0) Text("CAT5E:${cuad.cat5e}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF00695C))
                }
                Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(18.dp))
            }
            if (isExpanded && cuad.dias.isNotEmpty()) {
                HorizontalDivider()
                cuad.dias.forEach { dia ->
                    val detalle = buildString {
                        append("${dia.finalizadas} inst")
                        if (dia.garantias > 0) append(" · ${dia.garantias} gar")
                        if (dia.cat6 > 0) append(" · CAT6:${dia.cat6}")
                        if (dia.cat5e > 0) append(" · CAT5E:${dia.cat5e}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dia.ymd.takeLast(5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(detalle, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

/* ================================================================
   TAB 2: CUADRILLAS — Lista + Mapa
   ================================================================ */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordCuadrillasTab(
    uiState: CoordinadorUiState,
    onToggleView: () -> Unit,
    onToggleMapaMode: () -> Unit,
    onToggleCuadrillaExpanded: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (String) -> Unit,
    onOrdenClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(selected = uiState.cuadrillasViewMode == CuadrillasViewMode.LISTA, onClick = { if (uiState.cuadrillasViewMode != CuadrillasViewMode.LISTA) onToggleView() }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Lista") }
                SegmentedButton(selected = uiState.cuadrillasViewMode == CuadrillasViewMode.MAPA, onClick = { if (uiState.cuadrillasViewMode != CuadrillasViewMode.MAPA) onToggleView() }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Mapa") }
            }
        }
        CoordOrdenesUpdateBanner(uiState.cuadrillaData?.updateInfo)
        if (uiState.cuadrillasViewMode == CuadrillasViewMode.LISTA) {
            CoordCuadrillasLista(
                uiState = uiState,
                onToggleExpanded = onToggleCuadrillaExpanded,
                onPreviousDay = onPreviousDay,
                onNextDay = onNextDay,
                onDateSelected = onDateSelected,
                onOrdenClick = onOrdenClick,
            )
        } else {
            CoordCuadrillasMapaView(uiState = uiState, onToggleMapaMode = onToggleMapaMode)
        }
    }
}

@Composable
private fun CoordOrdenesUpdateBanner(updateInfo: com.redes.app.data.coordinador.CoordinadorUpdateInfo?) {
    val info = updateInfo ?: return
    val text = info.at?.let { "Ordenes actualizadas: ${formatBannerDateTime(it)}" }
        ?: "Ordenes: sin registro reciente de actualizacion"
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        color = Color(0xFFEAF4FF),
        contentColor = Color(0xFF0F3D68),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFBFDBFE), MaterialTheme.shapes.large).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordCuadrillasLista(
    uiState: CoordinadorUiState,
    onToggleExpanded: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (String) -> Unit,
    onOrdenClick: (String) -> Unit = {},
) {
    val cuadrillas = uiState.cuadrillaData?.cuadrillas ?: emptyList()
    val today = java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")).toString()
    val isToday = uiState.selectedYmd == today
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        // Selector de fecha
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousDay) { Icon(Icons.Outlined.ChevronLeft, "Día anterior") }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    SelectableDateHeader(ymd = uiState.selectedYmd, onDateSelected = onDateSelected)
                }
                IconButton(onClick = onNextDay, enabled = !isToday) {
                    Icon(Icons.Outlined.ChevronRight, "Día siguiente", tint = if (isToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        if (uiState.isCuadrillasLoading) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        } else {
            items(cuadrillas, key = { it.id }) { cuad ->
                val isExpanded = uiState.expandedCuadrillaId == cuad.id
                CoordCuadrillaExpandableCard(cuad = cuad, isExpanded = isExpanded, onToggle = { onToggleExpanded(cuad.id) }, onOrdenClick = onOrdenClick)
            }
            if (cuadrillas.isEmpty()) item { Text("Sin cuadrillas para esta fecha.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun CoordCuadrillaExpandableCard(cuad: CoordinadorCuadrilla, isExpanded: Boolean, onToggle: () -> Unit, onOrdenClick: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val dotColor = if (cuad.estadoActual == "EN_ORDEN") Color(0xFF2E7D32) else Color(0xFF6A1B9A)
                Box(modifier = Modifier.size(10.dp).background(dotColor, CircleShape).border(1.dp, Color.White, CircleShape))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(cuad.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EstadoRutaChip(cuad.estadoRuta)
                        if (cuad.categoria.isNotBlank()) Text(cuad.categoria, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("${cuad.ordenes.total} órd.", style = MaterialTheme.typography.labelMedium)
                    Text("${cuad.ordenes.finalizadas} fin.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1565C0))
                }
                Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(18.dp))
            }

            // Expanded: lista de órdenes
            if (isExpanded) {
                HorizontalDivider()
                if (cuad.ordenes.items.isEmpty()) {
                    Text("Sin órdenes para hoy.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                } else {
                    cuad.ordenes.items.forEach { orden ->
                        OrdenItemRow(orden = orden, onOrdenClick = onOrdenClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdenItemRow(orden: CoordinadorOrdenItem, onOrdenClick: (String) -> Unit = {}) {
    val (bgColor, fgColor) = estadoColores(orden.estado)
    val isCancelled = orden.estado.contains("CANCEL") || orden.estado.contains("ANUL")
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onOrdenClick(orden.id) }.padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(modifier = Modifier.size(8.dp).offset(y = 5.dp).background(fgColor, CircleShape))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = orden.cliente.ifBlank { orden.ordenId },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (orden.hora.isNotBlank()) Text(orden.hora.take(5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (orden.tipo.isNotBlank()) Text(orden.tipo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(color = bgColor, contentColor = fgColor, shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.wrapContentSize()) {
                Text(orden.estado, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "MESH ${orden.cantMesh}  FONO ${orden.cantFono}  BOX ${orden.cantBox}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isCancelled && orden.motivoCancelacion.isNotBlank()) {
                Text("Motivo: ${orden.motivoCancelacion}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB71C1C))
            }
        }
    }
}

private fun estadoColores(estado: String): Pair<Color, Color> = when {
    estado.contains("FINAL") -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
    estado.contains("CANCEL") || estado.contains("ANUL") -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
    estado == "EN CAMINO" -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
    estado == "INICIADA" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    estado == "AGENDADA" -> Color(0xFFF5F5F5) to Color(0xFF424242)
    else -> Color(0xFFF5F5F5) to Color(0xFF616161)
}

@Composable
private fun EstadoRutaChip(estadoRuta: String) {
    val (bg, fg, label) = when (estadoRuta.uppercase()) {
        "EN_CAMPO" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "En campo")
        "RUTA_CERRADA" -> Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), "Ruta cerrada")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), "Operativa")
    }
    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.extraSmall) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordCuadrillasMapaView(uiState: CoordinadorUiState, onToggleMapaMode: () -> Unit) {
    val context = LocalContext.current
    val locationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(-12.046374, -77.042793), 11f) }

    val mapItems = uiState.mapaItems
    val cuadrillasMapa = uiState.cuadrillasMapa
    val focusManager = LocalFocusManager.current

    // IDs de las cuadrillas propias del coordinador (para modo Mis Órdenes)
    val myCuadrillaIds = remember(uiState.cuadrillaData) {
        uiState.cuadrillaData?.cuadrillas?.map { it.id }?.toSet() ?: emptySet()
    }

    // Filtros — solo aplican en modo CUADRILLAS
    var filtroCuadrilla by remember { mutableStateOf("") }
    var filtroEstado by remember { mutableStateOf("") }
    val hayFiltro = filtroCuadrilla.isNotBlank() || filtroEstado.isNotBlank()

    val filteredMapItems = remember(mapItems, uiState.mapaMode, myCuadrillaIds, filtroCuadrilla, filtroEstado) {
        val base = if (uiState.mapaMode == MapaMode.MIS_ORDENES) {
            mapItems.filter { it.cuadrillaId in myCuadrillaIds }
        } else {
            mapItems
                .let { list -> if (filtroCuadrilla.isBlank()) list else list.filter { it.cuadrillaNombre.contains(filtroCuadrilla.trim(), ignoreCase = true) } }
                .let { list ->
                    when (filtroEstado) {
                        "INICIADA"   -> list.filter { it.estado.uppercase().let { e -> e.contains("INICIA") || e == "EN CAMINO" } }
                        "FINALIZADA" -> list.filter { it.estado.uppercase().contains("FINAL") }
                        "AGENDADA"   -> list.filter { it.estado.uppercase() == "AGENDADA" }
                        else -> list
                    }
                }
        }
        base
    }

    // Zoom a órdenes filtradas (solo cuando hay filtro activo en CUADRILLAS; al limpiar no mueve)
    LaunchedEffect(filteredMapItems) {
        if (uiState.mapaMode == MapaMode.CUADRILLAS && hayFiltro && filteredMapItems.isNotEmpty()) {
            if (filteredMapItems.size == 1) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(filteredMapItems[0].lat, filteredMapItems[0].lng), 15f))
            } else {
                val bounds = LatLngBounds.builder()
                filteredMapItems.forEach { bounds.include(LatLng(it.lat, it.lng)) }
                runCatching { cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80), durationMs = 700) }
            }
        }
    }

    // Zoom inicial cuando carga sin filtros
    LaunchedEffect(mapItems) {
        if (!hayFiltro && mapItems.size >= 2) {
            val bounds = LatLngBounds.builder()
            mapItems.forEach { bounds.include(LatLng(it.lat, it.lng)) }
            runCatching { cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80), durationMs = 700) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(selected = uiState.mapaMode == MapaMode.MIS_ORDENES, onClick = { if (uiState.mapaMode != MapaMode.MIS_ORDENES) onToggleMapaMode() }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Mis órdenes") }
                SegmentedButton(selected = uiState.mapaMode == MapaMode.CUADRILLAS, onClick = { if (uiState.mapaMode != MapaMode.CUADRILLAS) onToggleMapaMode() }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Cuadrillas") }
            }
        }

        // Filtros — solo visibles en modo CUADRILLAS
        if (uiState.mapaMode == MapaMode.CUADRILLAS) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = filtroCuadrilla,
                    onValueChange = { filtroCuadrilla = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filtrar órdenes por cuadrilla", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (filtroCuadrilla.isNotBlank()) {
                        { IconButton(onClick = { filtroCuadrilla = "" }) { Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp)) } }
                    } else null,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelSmall,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("AGENDADA" to "Agendada", "INICIADA" to "Iniciada", "FINALIZADA" to "Finalizada").forEach { (key, label) ->
                        FilterChip(
                            selected = filtroEstado == key,
                            onClick = { filtroEstado = if (filtroEstado == key) "" else key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (filtroEstado == key) { { Icon(Icons.Outlined.Check, null, modifier = Modifier.size(14.dp)) } } else null,
                        )
                    }
                    if (hayFiltro) {
                        TextButton(onClick = { filtroCuadrilla = ""; filtroEstado = "" }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                            Text("Limpiar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        var selectedOrder by remember { mutableStateOf<CoordinadorMapItem?>(null) }
        var selectedCuad by remember { mutableStateOf<CuadrillaMapa?>(null) }

        Box(modifier = Modifier.weight(1f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationGranted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = locationGranted, zoomControlsEnabled = false),
                onMapClick = { focusManager.clearFocus(); selectedOrder = null; selectedCuad = null },
            ) {
                // Órdenes (filtradas en CUADRILLAS, propias en MIS_ORDENES)
                filteredMapItems.forEach { item ->
                    key(item.id) {
                        MarkerComposable(keys = arrayOf(item.estado), state = rememberMarkerState(position = LatLng(item.lat, item.lng)), anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), onClick = { selectedOrder = item; selectedCuad = null; true }) {
                            OrderMarkerIcon(item.estado)
                        }
                    }
                }
                // En modo CUADRILLAS: ubicaciones de cuadrillas (sin filtro)
                if (uiState.mapaMode == MapaMode.CUADRILLAS) {
                    cuadrillasMapa.forEach { cuad ->
                        key("cuad-${cuad.id}") {
                            MarkerComposable(keys = arrayOf(cuad.estadoActual), state = rememberMarkerState(position = LatLng(cuad.lat, cuad.lng)), anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f), onClick = { selectedCuad = cuad; selectedOrder = null; true }) {
                                CuadrillaMarkerIcon(cuad.estadoActual)
                            }
                        }
                    }
                }
            }

            selectedOrder?.let { order ->
                CoordOrderPopup(item = order, onDismiss = { selectedOrder = null }, modifier = Modifier.align(Alignment.BottomCenter))
            }
            selectedCuad?.let { cuad ->
                MapCuadrillaPopup(cuadrilla = cuad, onDismiss = { selectedCuad = null }, modifier = Modifier.align(Alignment.BottomCenter))
            }

            val showEmptyState = !uiState.isMapaLoading && !uiState.isCuadrillasMapaLoading && filteredMapItems.isEmpty()
            if (showEmptyState) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(imageVector = Icons.Outlined.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (hayFiltro) "Sin órdenes con ese filtro" else "Sin órdenes con coordenadas para hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (uiState.isMapaLoading || uiState.isCuadrillasMapaLoading) {
                CenteredLoadingOverlay(modifier = Modifier.fillMaxSize(), message = "Cargando mapa...")
            }
        }
    }
}

@Composable
private fun CoordOrderPopup(item: CoordinadorMapItem, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(modifier = modifier.fillMaxWidth().padding(12.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.cliente.ifBlank { item.ordenId }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(item.cuadrillaNombre, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (item.estado.isNotBlank()) Text(item.estado, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) { Icon(Icons.Outlined.Close, null, modifier = Modifier.size(18.dp)) }
            }
            if (item.direccion.isNotBlank()) Text(item.direccion, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = {
                    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${item.lat},${item.lng}")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Abrir en Google Maps", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/* ================================================================
   TAB 3: ALMACÉN — Stock | Auditoría | Predespacho
   ================================================================ */
@Composable
private fun CoordAlmacenTab(
    uiState: CoordinadorUiState,
    onSubTab: (AlmacenSubTab) -> Unit,
    onToggleStockExpanded: (String) -> Unit,
    onToggleAuditoriaExpanded: (String) -> Unit,
    onAuditoriaSustain: (cuadrillaId: String, sn: String, uri: Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AlmacenSubTab.entries.forEachIndexed { idx, sub ->
                    SegmentedButton(selected = uiState.almacenSubTab == sub, onClick = { onSubTab(sub) }, shape = SegmentedButtonDefaults.itemShape(idx, AlmacenSubTab.entries.size)) {
                        Text(sub.label(), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        when (uiState.almacenSubTab) {
            AlmacenSubTab.STOCK -> CoordStockContent(uiState = uiState, onToggleExpanded = onToggleStockExpanded)
            AlmacenSubTab.AUDITORIA -> CoordAuditoriaContent(uiState = uiState, onToggleExpanded = onToggleAuditoriaExpanded, onSustain = onAuditoriaSustain)
            AlmacenSubTab.PREDESPACHO -> CoordPredespachoContent(uiState)
        }
    }
}

@Composable
private fun CoordStockContent(uiState: CoordinadorUiState, onToggleExpanded: (String) -> Unit) {
    if (uiState.isStockLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        items(uiState.stock, key = { it.cuadrillaId }) { cuad ->
            val isExpanded = uiState.expandedStockCuadrillaId == cuad.cuadrillaId
            Card(modifier = Modifier.fillMaxWidth(), onClick = { onToggleExpanded(cuad.cuadrillaId) }) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(cuad.cuadrillaNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                EquipoChip("ONT", cuad.ont, Color(0xFF1565C0))
                                EquipoChip("MESH", cuad.mesh, Color(0xFFAD1457))
                                EquipoChip("FONO", cuad.fono, Color(0xFF00695C))
                                EquipoChip("BOX", cuad.box, Color(0xFFE65100))
                            }
                        }
                        Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(18.dp))
                    }
                    if (isExpanded) {
                        HorizontalDivider()
                        if (cuad.equipos.isEmpty()) {
                            Text("Sin equipos en stock.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                        } else {
                            val tipoColors = mapOf("ONT" to Color(0xFF1565C0), "MESH" to Color(0xFFAD1457), "FONO" to Color(0xFF00695C), "BOX" to Color(0xFFE65100))
                            cuad.equipos.groupBy { it.tipo }.forEach { (tipo, items) ->
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(color = (tipoColors[tipo] ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f), contentColor = tipoColors[tipo] ?: MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small) {
                                        Text("$tipo · ${items.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                                items.forEach { eq ->
                                    Text(
                                        text = eq.sn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp, top = 1.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (uiState.stock.isEmpty()) item { Text("Sin datos de stock.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun EquipoChip(tipo: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Text("$tipo: $count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CoordAuditoriaContent(
    uiState: CoordinadorUiState,
    onToggleExpanded: (String) -> Unit,
    onSustain: (cuadrillaId: String, sn: String, uri: Uri) -> Unit,
) {
    val context = LocalContext.current
    var sustainTarget by remember { mutableStateOf<Pair<String, String>?>(null) } // cuadrillaId to sn
    var showSustainDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val target = sustainTarget; val uri = pendingCameraUri
        if (success && target != null && uri != null) onSustain(target.first, target.second, uri)
        sustainTarget = null; pendingCameraUri = null; showSustainDialog = false
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val target = sustainTarget
        if (uri != null && target != null) onSustain(target.first, target.second, uri)
        sustainTarget = null; showSustainDialog = false
    }

    if (uiState.isAuditoriaLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        items(uiState.auditoria, key = { it.cuadrillaId }) { cuad ->
            val isExpanded = uiState.expandedAuditoriaCuadrillaId == cuad.cuadrillaId
            Card(modifier = Modifier.fillMaxWidth(), onClick = { onToggleExpanded(cuad.cuadrillaId) }) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(cuad.cuadrillaNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(color = Color(0xFFFFF8E1), contentColor = Color(0xFFE65100), shape = MaterialTheme.shapes.small) {
                                Text("${cuad.pendiente} pend.", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                            Surface(color = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32), shape = MaterialTheme.shapes.small) {
                                Text("${cuad.sustentada} sust.", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(18.dp))
                    }
                    if (isExpanded) {
                        HorizontalDivider()
                        if (cuad.items.isEmpty()) {
                            Text("Sin equipos en auditoría.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            cuad.items.forEach { equipo ->
                                val isSustaining = uiState.auditoriaSustainingSnId == equipo.sn
                                EquipoAuditoriaRow(
                                    equipo = equipo,
                                    isSustaining = isSustaining,
                                    onSustainClick = {
                                        sustainTarget = cuad.cuadrillaId to equipo.sn
                                        showSustainDialog = true
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        if (uiState.auditoria.isEmpty()) item { Text("Sin equipos en auditoría.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showSustainDialog && sustainTarget != null) {
        AlertDialog(
            onDismissRequest = { showSustainDialog = false; sustainTarget = null },
            title = { Text("Sustentar equipo") },
            text = { Text("Selecciona cómo subir la foto de evidencia para: ${sustainTarget!!.second}") },
            confirmButton = {
                TextButton(onClick = {
                    val target = sustainTarget ?: return@TextButton
                    val uri = createStockPhotoUri(context, target.second)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                }) { Text("Cámara") }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) { Text("Galería") }
            },
        )
    }
}

@Composable
private fun EquipoAuditoriaRow(equipo: CoordinadorEquipoAuditoria, isSustaining: Boolean, onSustainClick: () -> Unit) {
    val isPendiente = equipo.estado != "sustentada"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = equipo.sn,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Surface(
            color = Color(0xFFEDE7F6),
            contentColor = Color(0xFF4A148C),
            shape = MaterialTheme.shapes.extraSmall,
            border = BorderStroke(1.dp, Color(0xFF4A148C).copy(alpha = 0.5f)),
        ) {
            Text(equipo.tipo, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        if (isPendiente) {
            Surface(color = Color(0xFFFFF8E1), contentColor = Color(0xFFE65100), shape = MaterialTheme.shapes.extraSmall) {
                Text("pendiente", modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
            if (isSustaining) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.clickable(onClick = onSustainClick),
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.size(12.dp))
                        Text("Sustentar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        } else {
            Surface(color = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32), shape = MaterialTheme.shapes.extraSmall) {
                Text("sustentada", modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CoordPredespachoContent(uiState: CoordinadorUiState) {
    if (uiState.isPredespachoLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    val pred = uiState.predespacho
    if (pred == null || !pred.tienePredespacho) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sin predespacho programado para hoy.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        item { Text("Predespacho · ${pred.ymd}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
        items(pred.rows, key = { it.cuadrillaId }) { row ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(row.cuadrillaNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    // Equipos principales
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        EquipoChip("ONT",  row.ont,  Color(0xFF1565C0))
                        EquipoChip("MESH", row.mesh, Color(0xFFAD1457))
                        EquipoChip("FONO", row.fono, Color(0xFF00695C))
                        EquipoChip("BOX",  row.box,  Color(0xFFE65100))
                    }
                    // Extras: PRECON, bobina residencial, rollo condominio
                    val tieneExtras = row.precon50 > 0 || row.precon100 > 0 || row.precon150 > 0 || row.precon200 > 0 || row.bobinaResi > 0 || row.rolloCondo
                    if (tieneExtras) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            if (row.precon50 > 0)   EquipoChip("PRE 50",  row.precon50,  Color(0xFF5D4037))
                            if (row.precon100 > 0)  EquipoChip("PRE 100", row.precon100, Color(0xFF5D4037))
                            if (row.precon150 > 0)  EquipoChip("PRE 150", row.precon150, Color(0xFF5D4037))
                            if (row.precon200 > 0)  EquipoChip("PRE 200", row.precon200, Color(0xFF5D4037))
                            if (row.bobinaResi > 0) EquipoChip("Bob.Resi", row.bobinaResi, Color(0xFF00838F))
                            if (row.rolloCondo)     EquipoChip("Rollo Condo", 1, Color(0xFF00838F))
                        }
                    }
                    if (row.updatedByName.isNotBlank()) Text("Guardado por: ${row.updatedByName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/* ================================================================
   TAB 4: GESTIÓN — Ventas | Plantillas
   ================================================================ */
@Composable
private fun CoordGestionTab(uiState: CoordinadorUiState, onSubTab: (GestionSubTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                GestionSubTab.entries.forEachIndexed { idx, sub ->
                    SegmentedButton(selected = uiState.gestionSubTab == sub, onClick = { onSubTab(sub) }, shape = SegmentedButtonDefaults.itemShape(idx, GestionSubTab.entries.size)) {
                        Text(sub.label())
                    }
                }
            }
        }
        when (uiState.gestionSubTab) {
            GestionSubTab.VENTAS -> CoordVentasContent(uiState)
            GestionSubTab.PLANTILLAS -> CoordPlantillasContent(uiState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoordVentasContent(uiState: CoordinadorUiState) {
    var selectedVenta by remember { mutableStateOf<CoordinadorVenta?>(null) }
    if (uiState.isVentasLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        items(uiState.ventas, key = { it.id }) { venta ->
            val (bg, fg) = when (venta.estado.uppercase()) {
                "PAGADO" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                "ANULADO", "ANULADA" -> Color(0xFFEEEEEE) to Color(0xFF616161)
                else -> Color(0xFFFFF8E1) to Color(0xFFE65100)
            }
            Card(modifier = Modifier.fillMaxWidth(), onClick = { selectedVenta = venta }) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(venta.cuadrillaNombre.ifBlank { venta.id }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val total = "S/ %.2f".format(venta.totalCents / 100.0)
                        val saldo = if (venta.saldoPendienteCents > 0) " · Saldo: S/ %.2f".format(venta.saldoPendienteCents / 100.0) else ""
                        Text("$total$saldo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.small) {
                        Text(venta.estado, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (uiState.ventas.isEmpty()) item { Text("Sin ventas registradas.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Spacer(Modifier.height(16.dp)) }
    }

    selectedVenta?.let { v ->
        ModalBottomSheet(onDismissRequest = { selectedVenta = null }) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Detalle de venta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val (bg, fg) = when (v.estado.uppercase()) {
                        "PAGADO" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                        "ANULADO", "ANULADA" -> Color(0xFFEEEEEE) to Color(0xFF616161)
                        else -> Color(0xFFFFF8E1) to Color(0xFFE65100)
                    }
                    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.small) {
                        Text(v.estado, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                HorizontalDivider()
                VentaDetalleRow("Cuadrilla", v.cuadrillaNombre.ifBlank { v.cuadrillaId })
                if (v.area.isNotBlank()) VentaDetalleRow("Área", v.area)
                if (!v.creadoAtStr.isNullOrBlank()) VentaDetalleRow("Fecha", v.creadoAtStr.take(10))
                VentaDetalleRow("Total", "S/ %.2f".format(v.totalCents / 100.0))
                if (v.saldoPendienteCents > 0) VentaDetalleRow("Saldo pendiente", "S/ %.2f".format(v.saldoPendienteCents / 100.0), valueColor = Color(0xFFE65100))
                if (v.cuotasTotal > 0) VentaDetalleRow("Cuotas", "${v.cuotasPagadas} / ${v.cuotasTotal} pagadas")
            }
        }
    }
}

@Composable
private fun VentaDetalleRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
private fun CoordPlantillasContent(uiState: CoordinadorUiState) {
    val clipboard = LocalClipboardManager.current
    if (uiState.isPlantillasLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    val hasPendientes = uiState.plantillas.any { it.total > 0 }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Text(
                "Plantillas pendientes · ${uiState.selectedYm}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (!hasPendientes) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8F5E9), contentColor = Color(0xFF1B5E20), shape = MaterialTheme.shapes.medium) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CheckCircle, null)
                        Text("Sin plantillas pendientes para este mes.", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            items(uiState.plantillas, key = { it.cuadrillaId }) { cuad ->
                if (cuad.total == 0) return@items
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(cuad.cuadrillaNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Surface(color = Color(0xFFFFF8E1), contentColor = Color(0xFFE65100), shape = MaterialTheme.shapes.small) {
                                Text("${cuad.total} pendientes", modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                            IconButton(
                                onClick = {
                                    val texto = buildString {
                                        appendLine(cuad.cuadrillaNombre)
                                        cuad.pedidos.forEach { p ->
                                            appendLine("${p.pedido} - ${p.cliente.ifBlank { "-" }} ${p.ymd.takeLast(5)}")
                                        }
                                    }.trimEnd()
                                    clipboard.setText(AnnotatedString(texto))
                                },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar todo", modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        cuad.pedidos.forEach { p ->
                            Text(
                                "${p.pedido} - ${p.cliente.ifBlank { "-" }} ${p.ymd.takeLast(5)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/* ================================================================
   HELPERS
   ================================================================ */
private fun CoordinadorTab.label() = when (this) {
    CoordinadorTab.INICIO -> "Inicio"
    CoordinadorTab.CUADRILLAS -> "Cuadrillas"
    CoordinadorTab.ALMACEN -> "Almacén"
    CoordinadorTab.GESTION -> "Gestión"
}

private fun CoordinadorTab.icon() = when (this) {
    CoordinadorTab.INICIO -> Icons.Outlined.Home
    CoordinadorTab.CUADRILLAS -> Icons.Outlined.Groups
    CoordinadorTab.ALMACEN -> Icons.Outlined.Inventory2
    CoordinadorTab.GESTION -> Icons.Outlined.AccountBalance
}

private fun AlmacenSubTab.label() = when (this) {
    AlmacenSubTab.STOCK -> "Stock"
    AlmacenSubTab.AUDITORIA -> "Auditoría"
    AlmacenSubTab.PREDESPACHO -> "Predespacho"
}

private fun GestionSubTab.label() = when (this) {
    GestionSubTab.VENTAS -> "Ventas"
    GestionSubTab.PLANTILLAS -> "Plantillas"
}
