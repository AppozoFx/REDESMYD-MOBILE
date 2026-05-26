package com.redes.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.redes.app.data.tecnico.TecnicoMapItem
import com.redes.app.data.tecnico.TecnicoOrderSummary
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.tecnico.TecnicoTab
import com.redes.app.ui.tecnico.TecnicoUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TecnicoShellScreen(
    uiState: TecnicoUiState,
    onTabSelected: (TecnicoTab) -> Unit,
    onRefreshClick: () -> Unit,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
    onDateSelected: (String) -> Unit,
    onOrderClick: (String) -> Unit,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseRouteClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Tecnico",
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            NavigationBar {
                TecnicoTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon(),
                                contentDescription = tab.label(),
                            )
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val contentModifier = Modifier.weight(1f, fill = true)
            if (uiState.errorMessage != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            if (uiState.isLoading && uiState.home == null) {
                CircularProgressIndicator()
            }

            when (uiState.selectedTab) {
                TecnicoTab.INICIO -> TecnicoHomeTab(
                    modifier = contentModifier,
                    uiState = uiState,
                    onCloseRouteClick = onCloseRouteClick,
                    onRefreshClick = onRefreshClick,
                    onSettingsClick = onSettingsClick,
                    onLogoutClick = onLogoutClick,
                )
                TecnicoTab.ORDENES -> TecnicoOrdersTab(
                    modifier = contentModifier,
                    uiState = uiState,
                    onDateSelected = onDateSelected,
                    onOrderClick = onOrderClick,
                )
                TecnicoTab.STOCK -> TecnicoStockTab(contentModifier, uiState)
                TecnicoTab.MAPA -> TecnicoMapTab(
                    modifier = contentModifier,
                    uiState = uiState,
                    onPreviousDayClick = onPreviousDayClick,
                    onNextDayClick = onNextDayClick,
                )
            }
        }
    }
}

@Composable
private fun TecnicoHomeTab(
    modifier: Modifier,
    uiState: TecnicoUiState,
    onCloseRouteClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val home = uiState.home ?: return
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(home.cuadrilla.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("Coordinador: ${home.cuadrilla.coordinadorNombre.ifBlank { "sin datos" }}")
                            Text("Gestor: ${home.cuadrilla.gestorNombre.ifBlank { "sin datos" }}")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedButton(onClick = onCloseRouteClick) {
                            Text("Cerrar ruta")
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("KPIs del mes actual", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        maxItemsInEachRow = 2,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        KpiMiniCard("Instalaciones", home.kpis.instalacionesMes.toString())
                        KpiMiniCard("Canceladas", home.kpis.canceladasMes.toString())
                        KpiMiniCard("Anuladas", home.kpis.anuladasMes.toString())
                        KpiMiniCard("Regestion", home.kpis.regestionMes.toString())
                        KpiMiniCard("Garantias", home.kpis.garantiasMes.toString())
                        KpiMiniCard("% Garantias", "${home.kpis.porcentajeGarantias}%")
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resumen de equipos", style = MaterialTheme.typography.titleMedium)
                    if (home.equipmentSummary.isEmpty()) {
                        Text("Sin equipos registrados.")
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            home.equipmentSummary.forEach { equipment ->
                                val isLow = equipment.isLowStock()
                                Surface(
                                    color = if (isLow) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    contentColor = if (isLow) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    shape = MaterialTheme.shapes.large,
                                ) {
                                    Text(
                                        text = "${equipment.tipo}-${equipment.cantidad}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMiniCard(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.48f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun TecnicoOrdersTab(
    modifier: Modifier,
    uiState: TecnicoUiState,
    onDateSelected: (String) -> Unit,
    onOrderClick: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectableDateHeader(
            ymd = uiState.selectedYmd,
            onDateSelected = onDateSelected,
        )
        OrdersUpdateBanner(uiState)
        LazyColumn(modifier = Modifier.weight(1f, fill = true), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.orders, key = { it.id }) { order ->
                OrderCard(order, onOrderClick)
            }
        }
    }
}

@Composable
private fun OrderCard(order: TecnicoOrderSummary, onOrderClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (order.isGarantia) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFFFFD54F),
                        shape = MaterialTheme.shapes.medium,
                    )
                } else {
                    Modifier
                }
            )
            .clickable { onOrderClick(order.id) },
        colors = order.statusCardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "${order.displayHour()} - ${order.cliente.ifBlank { order.ordenId }}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                order.direccion.ifBlank { "Sin direccion" },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "MESH ${order.cantMesh}  FONO ${order.cantFono}  BOX ${order.cantBox}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OrdersUpdateBanner(uiState: TecnicoUiState) {
    val info = uiState.ordersData?.updateInfo ?: return
    val mainText = info.at?.let { "Ordenes actualizadas: ${formatBannerDateTime(it)}" }
        ?: "Ordenes: sin registro reciente de actualizacion"
    val detailText = listOf(
        info.sourceLabel.ifBlank { null },
        info.byNombre.ifBlank { info.byUid.ifBlank { null } },
    ).filterNotNull().joinToString(" | ")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFF4D6),
        contentColor = Color(0xFF7C5A00),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = mainText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            if (detailText.isNotBlank()) {
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun TecnicoStockTab(
    modifier: Modifier,
    uiState: TecnicoUiState,
) {
    val stock = uiState.stock ?: return
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Equipos", style = MaterialTheme.typography.titleMedium)
                    if (stock.equipos.isEmpty()) {
                        Text("Sin equipos registrados.")
                    } else {
                        stock.equipos.forEach { equipo ->
                            Text("${equipo.tipo.ifBlank { "Equipo" }} | ${equipo.sn} ${equipo.proid.takeIf { it.isNotBlank() }?.let { " | $it" } ?: ""}")
                        }
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Materiales", style = MaterialTheme.typography.titleMedium)
                    if (stock.materiales.isEmpty()) {
                        Text("Sin materiales registrados.")
                    } else {
                        stock.materiales.forEach { material ->
                            val amount = if (material.unidadTipo.equals("METROS", true)) {
                                "${material.stockCm} cm"
                            } else {
                                "${material.stockUnd} und"
                            }
                            Text("${material.nombre} | $amount")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TecnicoMapTab(
    modifier: Modifier,
    uiState: TecnicoUiState,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MapDateHeader(uiState.selectedYmd, onPreviousDayClick, onNextDayClick)
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Mapa del dia: version inicial con ordenes geolocalizadas y acceso rapido a navegacion externa.",
                modifier = Modifier.padding(16.dp),
            )
        }
        LazyColumn(modifier = Modifier.weight(1f, fill = true), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(uiState.map?.items.orEmpty(), key = { it.id }) { item ->
                MapItemCard(item) {
                    val uri = Uri.parse("geo:${item.lat},${item.lng}?q=${Uri.encode(item.direccion.ifBlank { item.cliente })}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
private fun MapDateHeader(
    ymd: String,
    onPreviousDayClick: () -> Unit,
    onNextDayClick: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onPreviousDayClick, modifier = Modifier.weight(1f)) { Text("Dia anterior") }
        Button(onClick = onNextDayClick, modifier = Modifier.weight(1f)) { Text("Dia siguiente") }
    }
    Text(formatYmdDisplay(ymd), style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun MapItemCard(item: TecnicoMapItem, onOpenMapClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.cliente.ifBlank { item.ordenId }, style = MaterialTheme.typography.titleMedium)
            Text(item.direccion.ifBlank { "Sin direccion" })
            Text("Hora: ${item.fechaProgramadaHm.ifBlank { "sin hora" }}")
            Text("Estado: ${item.estado.ifBlank { "sin datos" }}")
            Button(onClick = onOpenMapClick, modifier = Modifier.fillMaxWidth()) {
                Text("Abrir en mapa")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectableDateHeader(
    ymd: String,
    onDateSelected: (String) -> Unit,
) {
    val selectedDate = runCatching { LocalDate.parse(ymd) }.getOrElse { LocalDate.now() }
    var isPickerOpen by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        onClick = {
            isPickerOpen = true
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Fecha seleccionada",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatYmdDisplay(ymd),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Text(
                text = "Cambiar",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    if (isPickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneId.of("America/Lima"))
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { isPickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("America/Lima"))
                                .toLocalDate()
                            onDateSelected(picked.toString())
                        }
                        isPickerOpen = false
                    },
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { isPickerOpen = false }) {
                    Text("Cancelar")
                }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        text = "Selecciona una fecha",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                headline = {
                    Text(
                        text = formatYmdDisplay(
                            pickerState.selectedDateMillis?.let { millis ->
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.of("America/Lima"))
                                    .toLocalDate()
                                    .toString()
                            } ?: ymd,
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                showModeToggle = false,
            )
        }
    }
}

private fun TecnicoTab.label(): String = when (this) {
    TecnicoTab.INICIO -> "Inicio"
    TecnicoTab.ORDENES -> "Ordenes"
    TecnicoTab.STOCK -> "Stock"
    TecnicoTab.MAPA -> "Mapa"
}

private fun TecnicoTab.icon() = when (this) {
    TecnicoTab.INICIO -> Icons.Outlined.Home
    TecnicoTab.ORDENES -> Icons.Outlined.Assignment
    TecnicoTab.STOCK -> Icons.Outlined.Inventory2
    TecnicoTab.MAPA -> Icons.Outlined.Map
}

private fun com.redes.app.data.tecnico.TecnicoEquipmentSummary.isLowStock(): Boolean = when (tipo.uppercase()) {
    "ONT" -> cantidad <= 3
    "MESH" -> cantidad <= 4
    "FONO" -> cantidad == 0
    "BOX" -> cantidad <= 1
    else -> false
}

private fun formatYmdDisplay(ymd: String): String {
    val date = runCatching { LocalDate.parse(ymd) }.getOrNull() ?: return ymd
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

private fun formatBannerDateTime(iso: String): String {
    val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return iso
    val date = instant.atZone(ZoneId.of("America/Lima"))
    return date.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
}

private fun TecnicoOrderSummary.displayHour(): String {
    return fechaProgramadaHm.ifBlank { fechaFinVisiHm.ifBlank { "00:00" } }
}

@Composable
private fun TecnicoOrderSummary.statusCardColors(): androidx.compose.material3.CardColors {
    val (container, content) = when (estado.trim().uppercase()) {
        "FINALIZADA" -> Color(0xFFE0F2FE) to Color(0xFF0C4A6E)
        "INICIADA" -> Color(0xFFDCFCE7) to Color(0xFF14532D)
        "CANCELADA", "ANULADA" -> Color(0xFFFEE2E2) to Color(0xFF7F1D1D)
        "REGESTION", "REGESTIÓN" -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
        "EN CAMINO" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
    }
    return androidx.compose.material3.CardDefaults.cardColors(
        containerColor = container,
        contentColor = content,
    )
}
