package com.redes.app.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.redes.app.data.almacen.*
import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.ui.almacen.AlmacenTab
import com.redes.app.ui.almacen.AlmacenUiState
import com.redes.app.ui.almacen.LiquidacionForm
import com.redes.app.ui.components.AppTopBar

/* ================================================================
   HELPERS
   ================================================================ */
private fun AlmacenTab.icon(): ImageVector = when (this) {
    AlmacenTab.STOCK -> Icons.Outlined.Inventory2
    AlmacenTab.LIQUIDACION -> Icons.Outlined.Receipt
    AlmacenTab.INSTALACIONES -> Icons.Outlined.Cable
    AlmacenTab.MAPA -> Icons.Outlined.Map
}

private fun AlmacenTab.label(): String = when (this) {
    AlmacenTab.STOCK -> "Stock"
    AlmacenTab.LIQUIDACION -> "Liquidación"
    AlmacenTab.INSTALACIONES -> "Instalaciones"
    AlmacenTab.MAPA -> "Mapa"
}

/* ================================================================
   SHELL PRINCIPAL
   ================================================================ */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlmacenShellScreen(
    uiState: AlmacenUiState,
    onTabSelected: (AlmacenTab) -> Unit,
    onRefreshClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleStockExpanded: (String) -> Unit,
    onToggleInstalacionExpanded: (String) -> Unit,
    onToggleLiquidacionExpanded: (String) -> Unit,
    onUpdateLiquidacionForm: (String, LiquidacionForm.() -> LiquidacionForm) -> Unit,
    onLiquidarOrden: (String) -> Unit,
    onDismissError: () -> Unit,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Almacén",
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            NavigationBar {
                AlmacenTab.entries.forEach { tab ->
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
                    AlmacenTab.STOCK -> AlmacenStockTab(
                        uiState = uiState,
                        onToggleExpanded = onToggleStockExpanded,
                        modifier = Modifier.weight(1f),
                    )
                    AlmacenTab.LIQUIDACION -> AlmacenLiquidacionTab(
                        uiState = uiState,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onToggleExpanded = onToggleLiquidacionExpanded,
                        onUpdateForm = onUpdateLiquidacionForm,
                        onLiquidar = onLiquidarOrden,
                        modifier = Modifier.weight(1f),
                    )
                    AlmacenTab.INSTALACIONES -> AlmacenInstalacionesTab(
                        uiState = uiState,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onToggleExpanded = onToggleInstalacionExpanded,
                        modifier = Modifier.weight(1f),
                    )
                    AlmacenTab.MAPA -> AlmacenMapaTab(
                        uiState = uiState,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/* ================================================================
   TAB 1: STOCK
   ================================================================ */
@Composable
private fun AlmacenStockTab(
    uiState: AlmacenUiState,
    onToggleExpanded: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isStockLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val total = uiState.stock.sumOf { it.total }
                val ont = uiState.stock.sumOf { it.ont }
                val mesh = uiState.stock.sumOf { it.mesh }
                AlmacenKpiCard("Total", total.toString(), Icons.Outlined.Inventory2, Color(0xFF1565C0), Modifier.weight(1f))
                AlmacenKpiCard("ONT", ont.toString(), Icons.Outlined.Router, Color(0xFF1565C0), Modifier.weight(1f))
                AlmacenKpiCard("MESH", mesh.toString(), Icons.Outlined.Wifi, Color(0xFFAD1457), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val fono = uiState.stock.sumOf { it.fono }
                val box = uiState.stock.sumOf { it.box }
                AlmacenKpiCard("FONO", fono.toString(), Icons.Outlined.PhoneAndroid, Color(0xFF00695C), Modifier.weight(1f))
                AlmacenKpiCard("BOX", box.toString(), Icons.Outlined.DevicesOther, Color(0xFFE65100), Modifier.weight(1f))
                AlmacenKpiCard("Cuadrillas", uiState.stock.size.toString(), Icons.Outlined.Groups, Color(0xFF6A1B9A), Modifier.weight(1f))
            }
        }
        if (uiState.stock.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Sin stock registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(uiState.stock, key = { it.cuadrillaId }) { cuad ->
                val isExpanded = uiState.expandedStockCuadrillaId == cuad.cuadrillaId
                StockCuadrillaCard(cuad = cuad, isExpanded = isExpanded, onToggle = { onToggleExpanded(cuad.cuadrillaId) })
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun StockCuadrillaCard(cuad: AlmacenStockCuadrilla, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(cuad.cuadrillaNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AlmacenEquipoChip("ONT", cuad.ont, Color(0xFF1565C0))
                        AlmacenEquipoChip("MESH", cuad.mesh, Color(0xFFAD1457))
                        AlmacenEquipoChip("FONO", cuad.fono, Color(0xFF00695C))
                        AlmacenEquipoChip("BOX", cuad.box, Color(0xFFE65100))
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
                        Surface(
                            color = (tipoColors[tipo] ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f),
                            contentColor = tipoColors[tipo] ?: MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text("$tipo · ${items.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        items.forEach { eq ->
                            Text(eq.sn, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp, top = 1.dp))
                        }
                    }
                }
            }
        }
    }
}

/* ================================================================
   TAB 2: LIQUIDACIÓN
   ================================================================ */
@Composable
private fun AlmacenLiquidacionTab(
    uiState: AlmacenUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleExpanded: (String) -> Unit,
    onUpdateForm: (String, LiquidacionForm.() -> LiquidacionForm) -> Unit,
    onLiquidar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onPreviousMonth) { Icon(Icons.Outlined.ChevronLeft, "Mes anterior") }
            Text(uiState.selectedYm, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNextMonth) { Icon(Icons.Outlined.ChevronRight, "Mes siguiente") }
        }
        if (uiState.isLiquidacionLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        val liq = uiState.liquidacion
        if (liq == null) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Sin datos de liquidación.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            return@Column
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlmacenKpiCard("Finalizadas", liq.kpi.finalizadas.toString(), Icons.Outlined.CheckCircle, Color(0xFF1565C0), Modifier.weight(1f))
            AlmacenKpiCard("Liquidadas", liq.kpi.liquidadas.toString(), Icons.Outlined.TaskAlt, Color(0xFF2E7D32), Modifier.weight(1f))
            AlmacenKpiCard("Pendientes", liq.kpi.pendientes.toString(), Icons.Outlined.HourglassEmpty, Color(0xFFE65100), Modifier.weight(1f))
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Orden, cliente, código, cuadrilla", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (searchQuery.isNotBlank()) {
                { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Outlined.Close, "Limpiar", modifier = Modifier.size(18.dp)) } }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        val visibleItems = if (searchQuery.isBlank()) {
            liq.items
        } else {
            val q = searchQuery.trim().lowercase()
            liq.items.filter { item ->
                item.ordenId.lowercase().contains(q) ||
                item.codigoCliente.lowercase().contains(q) ||
                item.cliente.lowercase().contains(q) ||
                item.cuadrillaNombre.lowercase().contains(q)
            }
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
            if (visibleItems.isNotEmpty()) {
                item {
                    Text(
                        "Pendientes (${visibleItems.size}${if (searchQuery.isNotBlank()) " de ${liq.items.size}" else ""})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                items(visibleItems, key = { it.id }) { item ->
                    val isExpanded = uiState.expandedLiquidacionId == item.id
                    val form = uiState.liquidacionForms[item.id] ?: LiquidacionForm()
                    val isPreliqLoading = uiState.isPreliqLoadingId == item.id
                    LiquidacionItemCard(
                        item = item,
                        isExpanded = isExpanded,
                        form = form,
                        isPreliqLoading = isPreliqLoading,
                        onToggle = { onToggleExpanded(item.id) },
                        onUpdateForm = { update -> onUpdateForm(item.id, update) },
                        onLiquidar = { onLiquidar(item.id) },
                    )
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            if (searchQuery.isNotBlank()) "Sin resultados para \"$searchQuery\"."
                            else "Sin órdenes pendientes de liquidación este mes.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LiquidacionItemCard(
    item: AlmacenLiquidacionItem,
    isExpanded: Boolean,
    form: LiquidacionForm,
    isPreliqLoading: Boolean,
    onToggle: () -> Unit,
    onUpdateForm: (LiquidacionForm.() -> LiquidacionForm) -> Unit,
    onLiquidar: () -> Unit,
) {
    val (bgColor, fgColor) = when {
        item.correccionPendiente -> Color(0xFFFCE4EC) to Color(0xFFC62828)
        item.liquidado -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> Color(0xFFFFF8E1) to Color(0xFFE65100)
    }
    val statusText = when {
        item.correccionPendiente -> "Corrección"
        item.liquidado -> "Liquidada"
        else -> "Pendiente"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().let { if (!item.liquidado) it.clickable(onClick = onToggle) else it },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.cliente.ifBlank { item.ordenId }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (item.codigoCliente.isNotBlank()) {
                        Text(item.codigoCliente, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = bgColor, contentColor = fgColor, shape = MaterialTheme.shapes.extraSmall) {
                        Text(statusText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    if (!item.liquidado) {
                        Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.cuadrillaNombre.isNotBlank()) Text(item.cuadrillaNombre, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (item.fechaYmd.isNotBlank()) Text(item.fechaYmd, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (item.tipo.isNotBlank()) Text(item.tipo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.plan.isNotBlank()) {
                Text(item.plan, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val equipos = buildString {
                if (item.cantMesh != "0" && item.cantMesh.isNotBlank()) append("MESH ${item.cantMesh}  ")
                if (item.cantFono != "0" && item.cantFono.isNotBlank()) append("FONO ${item.cantFono}  ")
                if (item.cantBox != "0" && item.cantBox.isNotBlank()) append("BOX ${item.cantBox}")
            }.trim()
            if (equipos.isNotBlank()) {
                Text(equipos, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isExpanded && !item.liquidado) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                LiquidacionForm(
                    form = form,
                    item = item,
                    isPreliqLoading = isPreliqLoading,
                    onUpdateForm = onUpdateForm,
                    onLiquidar = onLiquidar,
                )
            }
        }
    }
}

@Composable
private fun LiquidacionForm(
    form: LiquidacionForm,
    item: AlmacenLiquidacionItem,
    isPreliqLoading: Boolean,
    onUpdateForm: (LiquidacionForm.() -> LiquidacionForm) -> Unit,
    onLiquidar: () -> Unit,
) {
    val cantMesh = item.cantMesh.trim().toIntOrNull() ?: 0
    val cantFono = item.cantFono.trim().toIntOrNull() ?: 0
    val cantBox  = item.cantBox.trim().toIntOrNull()  ?: 0

    val meshBaseSlots   = minOf(4, maxOf(0, cantMesh))
    val boxBaseSlots    = minOf(4, maxOf(0, cantBox))
    val canAddMeshExtra = meshBaseSlots < 4
    val canAddBoxExtra  = boxBaseSlots  < 4
    val meshTotalSlots  = if (form.meshExtraEnabled && canAddMeshExtra) 4 else meshBaseSlots
    val boxTotalSlots   = if (form.boxExtraEnabled  && canAddBoxExtra)  4 else boxBaseSlots

    val snMeshUi = (form.snMeshes + List(maxOf(0, meshTotalSlots - form.snMeshes.size)) { "" }).take(meshTotalSlots)
    val snBoxUi  = (form.snBoxes  + List(maxOf(0, boxTotalSlots  - form.snBoxes.size))  { "" }).take(boxTotalSlots)

    val allSns = buildList {
        add(form.snOnt.trim().uppercase())
        snMeshUi.forEach { add(it.trim().uppercase()) }
        snBoxUi.forEach  { add(it.trim().uppercase()) }
        if (cantFono > 0 || form.fonoExtraEnabled) add(form.snFono.trim().uppercase())
    }.filter { it.isNotBlank() }
    val duplicates = allSns.groupBy { it }.filter { it.value.size > 1 }.keys.sorted()

    val meshEnteredCount = snMeshUi.count { it.isNotBlank() }
    val boxEnteredCount  = snBoxUi.count  { it.isNotBlank() }
    val fonoEntered = form.snFono.isNotBlank()
    val exceptionalUsed = meshEnteredCount > meshBaseSlots || boxEnteredCount > boxBaseSlots || (cantFono <= 0 && fonoEntered)
    val observacionRequired = exceptionalUsed
    val observacionValid    = !observacionRequired || form.observacion.isNotBlank()

    val meshBaseComplete = meshBaseSlots <= 0 || snMeshUi.take(meshBaseSlots).all { it.isNotBlank() }
    val boxBaseComplete  = boxBaseSlots  <= 0 || snBoxUi.take(boxBaseSlots).all  { it.isNotBlank() }
    val fonoComplete     = cantFono <= 0 || form.snFono.isNotBlank()
    val firstMeshExtraFilled = !form.meshExtraEnabled || !canAddMeshExtra || snMeshUi.getOrElse(meshBaseSlots) { "" }.isNotBlank()
    val firstBoxExtraFilled  = !form.boxExtraEnabled  || !canAddBoxExtra  || snBoxUi.getOrElse(boxBaseSlots)   { "" }.isNotBlank()

    val canLiquidar = !form.isSubmitting &&
        form.snOnt.isNotBlank() &&
        form.rotuloNapCto.isNotBlank() &&
        duplicates.isEmpty() &&
        meshBaseComplete && boxBaseComplete && fonoComplete &&
        firstMeshExtraFilled && firstBoxExtraFilled &&
        observacionValid

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isPreliqLoading) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Text("Buscando preliquidación…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (duplicates.isNotEmpty()) {
            Surface(color = Color(0xFFFFEBEE), shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Cancel, null, modifier = Modifier.size(14.dp).padding(top = 1.dp), tint = Color(0xFFC62828))
                    Text("SN duplicados: ${duplicates.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC62828))
                }
            }
        }
        if (observacionRequired) {
            Surface(color = Color(0xFFFFF8E1), shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.Warning, null, modifier = Modifier.size(14.dp).padding(top = 1.dp), tint = Color(0xFFE65100))
                    Text("Equipos fuera del plan. La observación es obligatoria para liquidar.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100))
                }
            }
        }

        Text("Series (SN)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        val ontDup = form.snOnt.trim().uppercase().let { it.isNotBlank() && duplicates.contains(it) }
        OutlinedTextField(
            value = form.snOnt,
            onValueChange = { v -> onUpdateForm { copy(snOnt = v.uppercase()) } },
            label = { Text("ONT *") },
            singleLine = true,
            isError = form.snOnt.isBlank() || ontDup,
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth(),
        )

        // ── MESH ─────────────────────────────────────────────────
        if (meshTotalSlots > 0) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "MESH ($meshEnteredCount/$meshTotalSlots)${if (meshEnteredCount > meshBaseSlots) " — excede plan" else ""}",
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = if (meshEnteredCount > meshBaseSlots) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (canAddMeshExtra) {
                    TextButton(
                        onClick = { onUpdateForm { val e = !meshExtraEnabled; copy(meshExtraEnabled = e, snMeshes = if (!e) snMeshes.take(meshBaseSlots) else snMeshes) } },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) { Text(if (form.meshExtraEnabled) "— Quitar extras" else "+ MESH extra", style = MaterialTheme.typography.labelSmall) }
                }
            }
            snMeshUi.forEachIndexed { idx, v ->
                val isDup = v.trim().uppercase().let { it.isNotBlank() && duplicates.contains(it) }
                val prevFilled = idx == 0 || snMeshUi.getOrElse(idx - 1) { "" }.isNotBlank()
                OutlinedTextField(
                    value = v,
                    onValueChange = { new ->
                        val slots = meshTotalSlots
                        onUpdateForm {
                            val list = (snMeshes + List(maxOf(0, slots - snMeshes.size)) { "" }).take(slots).toMutableList()
                            if (idx < list.size) list[idx] = new.uppercase()
                            copy(snMeshes = list)
                        }
                    },
                    label = { Text("MESH ${idx + 1}${if (idx < meshBaseSlots) " *" else " (exc)"}") },
                    singleLine = true, enabled = prevFilled, isError = isDup,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (!firstMeshExtraFilled) Text("Completa el MESH extra para continuar.", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("MESH no requerido por esta orden.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = { onUpdateForm { copy(meshExtraEnabled = !meshExtraEnabled) } },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) { Text(if (form.meshExtraEnabled) "— Quitar extras" else "+ MESH extra", style = MaterialTheme.typography.labelSmall) }
            }
        }

        // ── BOX ──────────────────────────────────────────────────
        if (boxTotalSlots > 0) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "BOX ($boxEnteredCount/$boxTotalSlots)${if (boxEnteredCount > boxBaseSlots) " — excede plan" else ""}",
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = if (boxEnteredCount > boxBaseSlots) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (canAddBoxExtra) {
                    TextButton(
                        onClick = { onUpdateForm { val e = !boxExtraEnabled; copy(boxExtraEnabled = e, snBoxes = if (!e) snBoxes.take(boxBaseSlots) else snBoxes) } },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) { Text(if (form.boxExtraEnabled) "— Quitar extras" else "+ BOX extra", style = MaterialTheme.typography.labelSmall) }
                }
            }
            snBoxUi.forEachIndexed { idx, v ->
                val isDup = v.trim().uppercase().let { it.isNotBlank() && duplicates.contains(it) }
                val prevFilled = idx == 0 || snBoxUi.getOrElse(idx - 1) { "" }.isNotBlank()
                OutlinedTextField(
                    value = v,
                    onValueChange = { new ->
                        val slots = boxTotalSlots
                        onUpdateForm {
                            val list = (snBoxes + List(maxOf(0, slots - snBoxes.size)) { "" }).take(slots).toMutableList()
                            if (idx < list.size) list[idx] = new.uppercase()
                            copy(snBoxes = list)
                        }
                    },
                    label = { Text("BOX ${idx + 1}${if (idx < boxBaseSlots) " *" else " (exc)"}") },
                    singleLine = true, enabled = prevFilled, isError = isDup,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (!firstBoxExtraFilled) Text("Completa el BOX extra para continuar.", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("BOX no requerido por esta orden.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = { onUpdateForm { copy(boxExtraEnabled = !boxExtraEnabled) } },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) { Text(if (form.boxExtraEnabled) "— Quitar extras" else "+ BOX extra", style = MaterialTheme.typography.labelSmall) }
            }
        }

        // ── FONO ─────────────────────────────────────────────────
        if (cantFono > 0 || form.fonoExtraEnabled) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (cantFono > 0) "FONO *" else "FONO (exc)",
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                    color = if (cantFono <= 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (cantFono <= 0) {
                    TextButton(
                        onClick = { onUpdateForm { copy(fonoExtraEnabled = false, snFono = "") } },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) { Text("— Quitar FONO", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100)) }
                }
            }
            val fonoDup = form.snFono.trim().uppercase().let { it.isNotBlank() && duplicates.contains(it) }
            OutlinedTextField(
                value = form.snFono,
                onValueChange = { v -> onUpdateForm { copy(snFono = v.uppercase()) } },
                label = { Text(if (cantFono > 0) "SN FONO *" else "SN FONO (excepcional)") },
                singleLine = true,
                isError = fonoDup || (cantFono > 0 && form.snFono.isBlank()),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("FONO no requerido por esta orden.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = { onUpdateForm { copy(fonoExtraEnabled = true) } },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) { Text("+ FONO extra", style = MaterialTheme.typography.labelSmall) }
            }
        }

        OutlinedTextField(
            value = form.rotuloNapCto,
            onValueChange = { v -> onUpdateForm { copy(rotuloNapCto = v) } },
            label = { Text("Rótulo NAP/CTO *") },
            singleLine = true,
            isError = form.rotuloNapCto.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.observacion,
            onValueChange = { v -> onUpdateForm { copy(observacion = v) } },
            label = { Text(if (observacionRequired) "Observación * (obligatoria por excepción)" else "Observación (opcional)") },
            singleLine = true,
            isError = observacionRequired && form.observacion.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Tipificaciones", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = form.planGamer, onClick = { onUpdateForm { copy(planGamer = !planGamer) } }, label = { Text("Gamer", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
            FilterChip(selected = form.kitWifiPro, onClick = { onUpdateForm { copy(kitWifiPro = !kitWifiPro) } }, label = { Text("WiFi Pro", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
            FilterChip(selected = form.servicioCableadoMesh, onClick = { onUpdateForm { copy(servicioCableadoMesh = !servicioCableadoMesh) } }, label = { Text("Cab. MESH", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = if (form.cat5e == 0) "" else form.cat5e.toString(),
                onValueChange = { v -> onUpdateForm { copy(cat5e = v.toIntOrNull() ?: 0) } },
                label = { Text("CAT5e (pts)") },
                singleLine = true,
                enabled = form.servicioCableadoMesh,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = if (form.cat6 == 0) "" else form.cat6.toString(),
                onValueChange = {},
                label = { Text("CAT6 (pts)") },
                readOnly = true, singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        if (form.submitError != null) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = friendlyLiqError(form.submitError),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Button(
            onClick = onLiquidar,
            enabled = canLiquidar,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
        ) {
            if (form.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (form.isSubmitting) "Liquidando…" else "Liquidar orden", fontWeight = FontWeight.Bold)
        }
    }
}

private fun friendlyLiqError(code: String): String = when {
    code.startsWith("EQUIPO_NOT_FOUND") -> "SN no encontrado: ${code.removePrefix("EQUIPO_NOT_FOUND").trim()}"
    code.startsWith("SN_NO_EN_CUADRILLA") -> "SN no está en la cuadrilla: ${code.removePrefix("SN_NO_EN_CUADRILLA").trim()}"
    code.startsWith("SN_UBICACION_INVALIDA") -> "SN en ubicación incorrecta: ${code.removePrefix("SN_UBICACION_INVALIDA").trim()}"
    code == "ONT_INVALID_COUNT" -> "Debe ingresar exactamente 1 SN ONT."
    code == "MESH_INSUFICIENTE" -> "Cantidad de MESH insuficiente."
    code == "BOX_INSUFICIENTE" -> "Cantidad de BOX insuficiente."
    code == "FONO_INSUFICIENTE" -> "Falta el SN de FONO requerido."
    code == "MESH_MAX_4" -> "Máximo 4 MESH por instalación."
    code == "BOX_MAX_4" -> "Máximo 4 BOX por instalación."
    code == "FONO_MAX_1" -> "Solo se permite 1 FONO."
    code == "ORDEN_YA_LIQUIDADA" -> "Esta orden ya fue liquidada."
    code == "ROTULO_REQUIRED" -> "El rótulo NAP/CTO es obligatorio."
    code == "SN_REQUERIDO" -> "Debe ingresar al menos un SN ONT."
    code.startsWith("STOCK_INSUFICIENTE") -> "Stock de materiales insuficiente en la cuadrilla."
    else -> code
}

/* ================================================================
   TAB 3: INSTALACIONES
   ================================================================ */
@Composable
private fun AlmacenInstalacionesTab(
    uiState: AlmacenUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleExpanded: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onPreviousMonth) { Icon(Icons.Outlined.ChevronLeft, "Mes anterior") }
            Text(uiState.selectedYm, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNextMonth) { Icon(Icons.Outlined.ChevronRight, "Mes siguiente") }
        }
        if (uiState.isInstalacionesLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        if (uiState.instalaciones.isNotEmpty()) {
            val ok = uiState.instalaciones.count { it.estadoMateriales == "ok" }
            val pendientes = uiState.instalaciones.size - ok
            val gamer = uiState.instalaciones.count { it.planGamer }
            val wifiPro = uiState.instalaciones.count { it.kitWifiPro }
            val cMesh = uiState.instalaciones.count { it.servicioCableadoMesh }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlmacenKpiCard("Total", uiState.instalaciones.size.toString(), Icons.Outlined.Cable, Color(0xFF1565C0), Modifier.weight(1f))
                AlmacenKpiCard("Completas", ok.toString(), Icons.Outlined.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f))
                AlmacenKpiCard("Pendientes", pendientes.toString(), Icons.Outlined.HourglassEmpty, Color(0xFFE65100), Modifier.weight(1f))
            }
            if (gamer > 0 || wifiPro > 0 || cMesh > 0) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (gamer > 0) AlmacenKpiCard("Gamer", gamer.toString(), Icons.Outlined.SportsEsports, Color(0xFF6A1B9A), Modifier.weight(1f))
                    if (wifiPro > 0) AlmacenKpiCard("WiFi Pro", wifiPro.toString(), Icons.Outlined.Wifi, Color(0xFF00695C), Modifier.weight(1f))
                    if (cMesh > 0) AlmacenKpiCard("Cab. MESH", cMesh.toString(), Icons.Outlined.Cable, Color(0xFFAD1457), Modifier.weight(1f))
                }
            }
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
            if (uiState.instalaciones.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Sin instalaciones este mes.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(uiState.instalaciones, key = { it.id }) { inst ->
                    val isExpanded = uiState.expandedInstalacionId == inst.id
                    InstalacionCard(inst = inst, isExpanded = isExpanded, onToggle = { onToggleExpanded(inst.id) })
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun InstalacionCard(inst: AlmacenInstalacion, isExpanded: Boolean, onToggle: () -> Unit) {
    val okMateriales = inst.estadoMateriales == "ok"
    val (statusBg, statusFg) = if (okMateriales) Color(0xFFE8F5E9) to Color(0xFF2E7D32) else Color(0xFFFFF8E1) to Color(0xFFE65100)
    Card(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(inst.cliente.ifBlank { inst.codigoCliente }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (inst.cuadrillaNombre.isNotBlank()) Text(inst.cuadrillaNombre, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                        if (inst.fechaYmd.isNotBlank()) Text(inst.fechaYmd, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (inst.plan.isNotBlank()) Text(inst.plan, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(color = statusBg, contentColor = statusFg, shape = MaterialTheme.shapes.extraSmall) {
                    Text(if (okMateriales) "OK" else "Pendiente", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(4.dp))
                Icon(if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(18.dp))
            }

            // Chips de tipificación siempre visibles
            val chips = buildList {
                if (inst.planGamer) add("Gamer" to Color(0xFF6A1B9A))
                if (inst.kitWifiPro) add("WiFi Pro" to Color(0xFF00695C))
                if (inst.servicioCableadoMesh) add("Cab. MESH" to Color(0xFFAD1457))
            }
            if (chips.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    chips.forEach { (label, color) ->
                        Surface(color = color.copy(alpha = 0.1f), contentColor = color, shape = MaterialTheme.shapes.small) {
                            Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider()
                if (inst.codigoCliente.isNotBlank()) DetailRow(label = "Pedido", value = inst.codigoCliente)
                if (inst.tipoOrden.isNotBlank()) DetailRow(label = "Tipo", value = inst.tipoOrden)
                if (inst.tipoCuadrilla.isNotBlank()) DetailRow(label = "Segmento", value = inst.tipoCuadrilla)
                if (inst.coordinadorNombre.isNotBlank()) DetailRow(label = "Coordinador", value = inst.coordinadorNombre)
                if (inst.acta.isNotBlank()) DetailRow(label = "Acta", value = inst.acta)
                if (inst.precon.isNotBlank()) DetailRow(label = "PRECON", value = inst.precon)
                if (inst.snOnt.isNotBlank()) DetailRow(label = "ONT", value = inst.snOnt, monospace = true)
                if (inst.snMesh.isNotEmpty()) DetailRow(label = "MESH", value = inst.snMesh.joinToString(", "), monospace = true)
                if (inst.snBox.isNotEmpty()) DetailRow(label = "BOX", value = inst.snBox.joinToString(", "), monospace = true)
                if (inst.snFono.isNotBlank()) DetailRow(label = "FONO", value = inst.snFono, monospace = true)
                if (inst.bobinaMetros > 0) DetailRow(label = "Bobina", value = "${inst.bobinaMetros} m")
                val utp = buildString {
                    if (inst.cat5e > 0) append("CAT5e: ${inst.cat5e} pts")
                    if (inst.cat6 > 0) { if (isNotEmpty()) append("  ·  "); append("CAT6: ${inst.cat6} pts") }
                    if (inst.puntosUTP > 0) { if (isNotEmpty()) append("  ·  "); append("Total: ${inst.puntosUTP} pts") }
                }
                if (utp.isNotBlank()) DetailRow(label = "UTP", value = utp)
                if (inst.observacion.isNotBlank()) DetailRow(label = "Obs.", value = inst.observacion)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, monospace: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(72.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = if (monospace) FontFamily.Monospace else null,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/* ================================================================
   TAB 4: MAPA
   ================================================================ */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlmacenMapaTab(uiState: AlmacenUiState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locationGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.046374, -77.042793), 11f)
    }
    val cuadrillasMapa = uiState.cuadrillasMapa

    LaunchedEffect(cuadrillasMapa) {
        if (cuadrillasMapa.size >= 2) {
            val bounds = LatLngBounds.builder()
            cuadrillasMapa.forEach { bounds.include(LatLng(it.lat, it.lng)) }
            runCatching { cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 80), durationMs = 700) }
        }
    }

    var selectedCuad by remember { mutableStateOf<CuadrillaMapa?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = locationGranted, zoomControlsEnabled = false),
            onMapClick = { selectedCuad = null },
        ) {
            cuadrillasMapa.forEach { cuad ->
                key("cuad-${cuad.id}") {
                    MarkerComposable(
                        keys = arrayOf(cuad.estadoActual),
                        state = rememberMarkerState(position = LatLng(cuad.lat, cuad.lng)),
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        onClick = { selectedCuad = cuad; true },
                    ) {
                        CuadrillaMarkerIcon(cuad.estadoActual)
                    }
                }
            }
        }

        selectedCuad?.let { cuad ->
            MapCuadrillaPopup(cuadrilla = cuad, onDismiss = { selectedCuad = null }, modifier = Modifier.align(Alignment.BottomCenter))
        }

        if (!uiState.isCuadrillasMapaLoading && cuadrillasMapa.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Outlined.LocationOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sin cuadrillas con ubicación activa.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }

        if (uiState.isCuadrillasMapaLoading) {
            CenteredLoadingOverlay(modifier = Modifier.fillMaxSize(), message = "Cargando mapa...")
        }
    }
}

/* ================================================================
   COMPONENTES COMPARTIDOS
   ================================================================ */
@Composable
private fun AlmacenKpiCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AlmacenEquipoChip(tipo: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Text("$tipo: $count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}
