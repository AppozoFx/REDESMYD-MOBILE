@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.redes.app.ui.screens

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cable
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.data.tecnico.MapMode
import com.redes.app.data.tecnico.TecnicoMapItem
import com.redes.app.data.tecnico.TecnicoOrderSummary
import com.redes.app.data.tecnico.TecnicoPlantillaPendiente
import com.redes.app.data.tecnico.TecnicoStockEquipment
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.tecnico.TecnicoTab
import com.redes.app.ui.tecnico.TecnicoUiState
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.net.URL
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
    onStockSustainClick: (String, String, Uri) -> Unit,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseRouteClick: () -> Unit = {},
    onToggleMapMode: () -> Unit = {},
    onRequiereAtencionClick: () -> Unit = {},
    onRefreshCuadrillasMapa: () -> Unit = {},
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Tecnico",
                notifCount = uiState.notifCount,
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
                        label = { Text(tab.label(), maxLines = 1) },
                    )
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefreshClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val contentModifier = Modifier.weight(1f)
                if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                        )
                    }
                }

                when (uiState.selectedTab) {
                    TecnicoTab.INICIO -> TecnicoHomeTab(
                        modifier = contentModifier,
                        uiState = uiState,
                        onCloseRouteClick = onCloseRouteClick,
                        onRefreshClick = onRefreshClick,
                        onSettingsClick = onSettingsClick,
                        onRequiereAtencionClick = onRequiereAtencionClick,
                        onLogoutClick = onLogoutClick,
                    )
                    TecnicoTab.ORDENES -> TecnicoOrdersTab(
                        modifier = contentModifier,
                        uiState = uiState,
                        onDateSelected = onDateSelected,
                        onOrderClick = onOrderClick,
                    )
                    TecnicoTab.STOCK -> TecnicoStockTab(
                        modifier = contentModifier,
                        uiState = uiState,
                        onStockSustainClick = onStockSustainClick,
                    )
                    TecnicoTab.MAPA -> TecnicoMapTab(
                        modifier = contentModifier,
                        uiState = uiState,
                        onToggleMapMode = onToggleMapMode,
                        onRefreshClick = onRefreshClick,
                        onRefreshCuadrillasMapa = onRefreshCuadrillasMapa,
                    )
                }
            }

            if (uiState.isInitialLoading && uiState.home == null) {
                CenteredLoadingOverlay(
                    modifier = Modifier.fillMaxSize(),
                    message = "Cargando informacion...",
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
    onRequiereAtencionClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    val context = LocalContext.current
    val home = uiState.home ?: return
    val efectivoEstadoRuta = when (uiState.alertaEstado) {
        "ACEPTADA" -> "RUTA_CERRADA"
        else -> uiState.estadoRuta
    }
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nombre + badge + botón Cerrar Ruta en la misma fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = home.cuadrilla.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        EstadoRutaBadge(estadoRuta = efectivoEstadoRuta)
                        CerrarRutaButton(uiState = uiState, onConfirm = onCloseRouteClick)
                    }
                    Text(
                        "Coordinador: ${home.cuadrilla.coordinadorNombre.ifBlank { "sin datos" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    // Gestor + dos botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Gestor: ${home.cuadrilla.gestorNombre.ifBlank { "sin datos" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        // Botón mano: pide atención al gestor
                        IconButton(
                            onClick = onRequiereAtencionClick,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationImportant,
                                contentDescription = "Pedir atención al Gestor",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        // Botón WhatsApp: abrir chat con el gestor
                        if (home.cuadrilla.gestorWhatsapp.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val phone = home.cuadrilla.gestorWhatsapp.replace("[^0-9+]".toRegex(), "")
                                    val numero = if (!phone.startsWith("+") && phone.length <= 9) "51$phone" else phone.trimStart('+')
                                    val url = "https://wa.me/$numero"
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Chat,
                                    contentDescription = "WhatsApp al Gestor",
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
}
            }
        }
        item {
            Text(
                text = "KPIs del mes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("Instalaciones", home.kpis.instalacionesMes.toString(), Icons.Outlined.CheckCircle, Color(0xFF1565C0), Modifier.weight(1f))
                KpiMiniCard("Garantías", home.kpis.garantiasMes.toString(), Icons.Outlined.Build, Color(0xFFE65100), Modifier.weight(1f))
                KpiMiniCard("% Gar.", "${home.kpis.porcentajeGarantias}%", Icons.Outlined.Percent, Color(0xFF6A1B9A), Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("Canceladas", home.kpis.canceladasMes.toString(), Icons.Outlined.Block, Color(0xFFB71C1C), Modifier.weight(1f))
                KpiMiniCard("Anuladas", home.kpis.anuladasMes.toString(), Icons.Outlined.RemoveCircle, Color(0xFF616161), Modifier.weight(1f))
                KpiMiniCard("Regestión", home.kpis.regestionMes.toString(), Icons.Outlined.Refresh, Color(0xFF9A3412), Modifier.weight(1f))
            }
        }
        item {
            Text(
                text = "Cableado del mes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard(
                    label = "pts Cat5e",
                    value = home.cableado.puntosCat5e.toString(),
                    icon = Icons.Outlined.Cable,
                    color = Color(0xFF0277BD),
                    modifier = Modifier.weight(1f),
                )
                KpiMiniCard(
                    label = "pts Cat6",
                    value = home.cableado.puntosCat6.toString(),
                    icon = Icons.Outlined.Cable,
                    color = Color(0xFF00695C),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            PlantillasPendientesCard(items = home.plantillasPendientes)
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PlantillasPendientesCard(items: List<TecnicoPlantillaPendiente>) {
    var showDialog by remember { mutableStateOf(false) }
    val count = items.size
    val (bgColor, contentColor) = if (count == 0) {
        Color(0xFFE8F5E9) to Color(0xFF1B5E20)
    } else {
        Color(0xFFFFF8E1) to Color(0xFFE65100)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (count > 0) Modifier.clickable { showDialog = true } else Modifier),
        colors = CardDefaults.cardColors(containerColor = bgColor, contentColor = contentColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.AssignmentLate,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Plantillas pendientes del mes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (count == 0) "Todo al día" else "$count sin enviar — toca para ver",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (showDialog && count > 0) {
        PlantillasPendientesDialog(items = items, onDismiss = { showDialog = false })
    }
}

@Composable
private fun PlantillasPendientesDialog(
    items: List<TecnicoPlantillaPendiente>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Plantillas pendientes (${items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider()
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 480.dp),
                ) {
                    items(items, key = { it.ordenId }) { item ->
                        PlantillaPendienteItem(
                            item = item,
                            onCopy = { text, label ->
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(text))
                                Toast.makeText(context, "$label copiado", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlantillaPendienteItem(
    item: TecnicoPlantillaPendiente,
    onCopy: (text: String, label: String) -> Unit,
) {
    val fecha = runCatching {
        java.time.LocalDate.parse(item.ymd)
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
    }.getOrElse { item.ymd }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    text = fecha,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (item.codigoCliente.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.codigoCliente,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { onCopy(item.codigoCliente, "Código") },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copiar código",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            if (item.cliente.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = item.cliente,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(
                        onClick = { onCopy(item.cliente, "Cliente") },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copiar cliente",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoRutaBadge(estadoRuta: String?) {
    if (estadoRuta == null) return
    val (bgColor, textColor, label) = when (estadoRuta.uppercase()) {
        "EN_CAMPO" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "En campo")
        "RUTA_CERRADA" -> Triple(Color(0xFFFFEBEE), Color(0xFFB71C1C), "Ruta cerrada")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), "Operativa")
    }
    Surface(color = bgColor, contentColor = textColor, shape = MaterialTheme.shapes.extraSmall) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CerrarRutaButton(uiState: TecnicoUiState, onConfirm: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val redDark = Color(0xFFB71C1C)
    val redLight = Color(0xFFFFEBEE)

    when (uiState.alertaEstado) {
        null, "RECHAZADA" -> {
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = redDark),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(if (uiState.alertaEstado == "RECHAZADA") "Reintentar cierre" else "Cerrar ruta")
            }
        }
        "PENDIENTE" -> {
            Surface(
                color = Color(0xFFFFF8E1),
                contentColor = Color(0xFFE65100),
                shape = MaterialTheme.shapes.small,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFE65100),
                    )
                    Text(
                        text = "Esperando autorización",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        "ACEPTADA" -> {
            Surface(
                color = redLight,
                contentColor = redDark,
                shape = MaterialTheme.shapes.small,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "Ruta cerrada",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cerrar ruta") },
            text = {
                Text("El tracking se detendrá ahora y se enviará una solicitud de cierre a Gestor y Jefatura para su autorización.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false; onConfirm() },
                    colors = ButtonDefaults.textButtonColors(contentColor = redDark),
                ) { Text("Confirmar cierre") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun KpiMiniCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
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
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            SelectableDateHeader(
                ymd = uiState.selectedYmd,
                onDateSelected = onDateSelected,
            )
            OrdersUpdateBanner(uiState)
            LazyColumn(modifier = Modifier.weight(1f, fill = true), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.orders, key = { it.id }) { order ->
                    OrderCard(order, onOrderClick)
                }
            }
        }
        if (uiState.isOrdersLoading) {
            CenteredLoadingOverlay(
                modifier = Modifier.fillMaxSize(),
                message = "Cargando ordenes...",
            )
        }
    }
}

@Composable
private fun OrderCard(order: TecnicoOrderSummary, onOrderClick: (String) -> Unit) {
    val (_, dotColor) = estadoColoresTecnico(order.estado)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (order.isGarantia) {
                    Modifier.border(2.dp, Color(0xFFFFD54F), MaterialTheme.shapes.medium)
                } else {
                    Modifier
                }
            )
            .clickable { onOrderClick(order.id) },
        colors = order.statusCardColors(),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            // Header: dot + hora+cliente + chip estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(8.dp).background(dotColor, CircleShape))
                Text(
                    text = "${order.displayHour().take(5)} — ${order.cliente.ifBlank { order.ordenId }}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (order.estado.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        contentColor = dotColor,
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = order.estado,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Text(
                order.direccion.ifBlank { "Sin dirección" },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "MESH ${order.cantMesh}  FONO ${order.cantFono}  BOX ${order.cantBox}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (order.isFinalizada && !order.isGarantia) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Liquidacion: ${if (order.isLiquidated) "Liquidado" else "Pendiente"} | Plantilla: ${order.plantillaStatus.toPlantillaLabel()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                    if (order.plantillaStatus.isPlantillaOk()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Plantilla OK",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
            if (order.shouldShowCancellationReason()) {
                Text(
                    text = "Motivo: ${order.motivoCancelacion.ifBlank { "Sin detalle" }}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun OrdersUpdateBanner(uiState: TecnicoUiState) {
    val info = uiState.ordersData?.updateInfo ?: return
    val mainText = info.at?.let { "Ordenes actualizadas: ${formatBannerDateTime(it)}" }
        ?: "Ordenes: sin registro reciente de actualizacion"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEAF4FF),
        contentColor = Color(0xFF0F3D68),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFBFDBFE), MaterialTheme.shapes.large)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(
                text = mainText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private enum class StockSection(val label: String) {
    EQUIPOS("Equipos"),
    MATERIALES("Materiales"),
    BOBINAS("Bobinas"),
}

@Composable
private fun TecnicoStockTab(
    modifier: Modifier,
    uiState: TecnicoUiState,
    onStockSustainClick: (String, String, Uri) -> Unit,
) {
    val stock = uiState.stock
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var sustainTarget by remember { mutableStateOf<TecnicoStockEquipment?>(null) }
    var showSustainDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var tipoFilter by remember { mutableStateOf<String?>(null) }
    var selectedSection by remember { mutableStateOf(StockSection.EQUIPOS) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val target = sustainTarget
        val uri = pendingCameraUri
        if (success && target != null && uri != null) {
            onStockSustainClick(stock?.cuadrilla?.id.orEmpty(), target.sn, uri)
        }
        sustainTarget = null
        pendingCameraUri = null
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val target = sustainTarget
        if (uri != null && target != null) {
            onStockSustainClick(stock?.cuadrilla?.id.orEmpty(), target.sn, uri)
        }
        sustainTarget = null
        showSustainDialog = false
    }

    Box(modifier = modifier) {
        when {
            stock == null && uiState.isStockLoading -> {
                CenteredLoadingOverlay(
                    modifier = Modifier.fillMaxSize(),
                    message = "Cargando stock...",
                )
            }
            stock == null -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay stock disponible.",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            else -> {
                val equipmentSummary = uiState.home?.equipmentSummary.orEmpty()
                val isResidencial = stock.cuadrilla.categoria.equals("RESIDENCIAL", ignoreCase = true)
                val sections = buildList {
                    add(StockSection.EQUIPOS)
                    add(StockSection.MATERIALES)
                    if (isResidencial) add(StockSection.BOBINAS)
                }
                // Si la cuadrilla no es residencial y quedo seleccionada Bobinas, vuelve a Equipos.
                val activeSection = if (selectedSection in sections) selectedSection else StockSection.EQUIPOS
                val filteredEquipos = stock.equipos
                    .filter { tipoFilter == null || it.tipo.equals(tipoFilter, ignoreCase = true) }
                    .sortedByDescending { dispatchAntiquityDays(it.fDespachoYmd) ?: -1L }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    if (equipmentSummary.isNotEmpty()) {
                        item {
                            StockResumenFilterCard(
                                equipmentSummary = equipmentSummary,
                                selectedTipo = tipoFilter,
                                onTipoSelected = { tipo ->
                                    tipoFilter = if (tipoFilter.equals(tipo, ignoreCase = true)) null else tipo
                                    selectedSection = StockSection.EQUIPOS
                                },
                            )
                        }
                    }
                    item {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            sections.forEachIndexed { index, section ->
                                SegmentedButton(
                                    selected = activeSection == section,
                                    onClick = { selectedSection = section },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = sections.size),
                                ) {
                                    Text(section.label)
                                }
                            }
                        }
                    }

                    when (activeSection) {
                        StockSection.EQUIPOS -> {
                            item {
                                Text(
                                    text = if (tipoFilter == null) {
                                        "${filteredEquipos.size} equipos en stock"
                                    } else {
                                        "${filteredEquipos.size} de ${stock.equipos.size} equipos (filtro: ${tipoFilter})"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            if (filteredEquipos.isEmpty()) {
                                item {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "No hay equipos para el filtro seleccionado.",
                                            modifier = Modifier.padding(16.dp),
                                        )
                                    }
                                }
                            }
                            items(filteredEquipos, key = { it.sn }) { equipo ->
                                StockEquipmentCard(
                                    equipo = equipo,
                                    onCopySn = {
                                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(equipo.sn))
                                        Toast.makeText(context, "SN copiado", Toast.LENGTH_SHORT).show()
                                    },
                                    onSustainClick = {
                                        sustainTarget = equipo
                                        showSustainDialog = true
                                    },
                                )
                            }
                        }

                        StockSection.MATERIALES -> {
                            if (stock.materiales.isEmpty()) {
                                item {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Sin materiales registrados.",
                                            modifier = Modifier.padding(16.dp),
                                        )
                                    }
                                }
                            }
                            items(stock.materiales, key = { it.id }) { material ->
                                val amount = if (material.unidadTipo.equals("METROS", true)) {
                                    "${material.stockCm} cm"
                                } else {
                                    "${material.stockUnd} und"
                                }
                                StockLineCard(title = material.nombre, value = amount)
                            }
                        }

                        StockSection.BOBINAS -> {
                            if (stock.bobinas.isEmpty()) {
                                item {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Sin bobinas activas.",
                                            modifier = Modifier.padding(16.dp),
                                        )
                                    }
                                }
                            }
                            items(stock.bobinas, key = { it.id }) { bobina ->
                                StockBobinaCard(bobina)
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isStockLoading && stock != null) {
            CenteredLoadingOverlay(
                modifier = Modifier.fillMaxSize(),
                message = "Actualizando stock...",
            )
        }
    }

    if (showSustainDialog && sustainTarget != null) {
        val target = sustainTarget!!
        AlertDialog(
            onDismissRequest = {
                showSustainDialog = false
                sustainTarget = null
            },
            title = { Text("Sustentar ${target.sn}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Elige como quieres adjuntar la evidencia.")
                    Button(
                        onClick = {
                            showSustainDialog = false
                            val uri = createStockPhotoUri(context, target.sn)
                            pendingCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tomar foto")
                    }
                    Button(
                        onClick = {
                            showSustainDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(imageVector = Icons.Outlined.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galeria")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSustainDialog = false
                    sustainTarget = null
                }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun StockResumenFilterCard(
    equipmentSummary: List<com.redes.app.data.tecnico.TecnicoEquipmentSummary>,
    selectedTipo: String?,
    onTipoSelected: (String) -> Unit,
) {
    val lowStock = equipmentSummary.filter { it.isLowStock() }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Resumen de equipos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Toca un tipo para filtrar la lista.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                equipmentSummary.forEach { equipment ->
                    val isLow = equipment.isLowStock()
                    val isSelected = selectedTipo.equals(equipment.tipo, ignoreCase = true)
                    Surface(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isLow -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isLow -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.clickable { onTipoSelected(equipment.tipo) },
                    ) {
                        Text(
                            text = "${equipment.tipo}-${equipment.cantidad}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            if (lowStock.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Cuentas con stock bajo de: " +
                            lowStock.joinToString(", ") { "${it.tipo} (${it.cantidad})" },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StockLineCard(title: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.ifBlank { "Sin nombre" },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StockBobinaCard(bobina: com.redes.app.data.tecnico.TecnicoStockBobina) {
    val antDays = dispatchAntiquityDays(bobina.fDespachoYmd)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = bobina.codigo.ifBlank { "Bobina" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
                Surface(
                    color = Color(0xFF1565C0).copy(alpha = 0.1f),
                    contentColor = Color(0xFF1565C0),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = "${formatMeters(bobina.metrosRestantes)} m",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StockInfoChip(label = "Metros iniciales", value = "${formatMeters(bobina.metrosIniciales)} m")
                StockInfoChip(label = "F. Despacho", value = formatYmdToDmy(bobina.fDespachoYmd))
                if (antDays != null) {
                    StockInfoChip(label = "Antiguedad", value = "$antDays d")
                }
            }
        }
    }
}

@Composable
private fun StockEquipmentCard(
    equipo: TecnicoStockEquipment,
    onCopySn: () -> Unit,
    onSustainClick: () -> Unit,
) {
    val antDays = dispatchAntiquityDays(equipo.fDespachoYmd)
    val audit = equipo.auditoria
    val sustentada = audit?.estado == "sustentada"
    val photoUrl = audit?.fotoURL.orEmpty()
    val hasPhoto = photoUrl.isNotBlank()
    var showPhoto by remember { mutableStateOf(false) }
    val colors = when {
        audit != null && !sustentada -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        audit != null -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        else -> CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val tipoColor = when (equipo.tipo.uppercase()) {
                            "ONT"  -> Color(0xFF1565C0)
                            "MESH" -> Color(0xFFAD1457)
                            "FONO" -> Color(0xFF00695C)
                            "BOX"  -> Color(0xFFE65100)
                            else   -> MaterialTheme.colorScheme.primary
                        }
                        Surface(
                            color = tipoColor.copy(alpha = 0.12f),
                            contentColor = tipoColor,
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                text = equipo.tipo.ifBlank { "Equipo" },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                text = antDays?.let { "$it d" } ?: "sin fecha",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Text(
                        text = equipo.sn,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    )
                }
                IconButton(onClick = onCopySn) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copiar SN",
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StockInfoChip(label = "F. Despacho", value = formatYmdToDmy(equipo.fDespachoYmd))
                StockInfoChip(label = "Guia Despacho", value = equipo.guiaDespacho.ifBlank { "sin guia" })
                if (equipo.proid.isNotBlank()) {
                    StockInfoChip(label = "PROID", value = equipo.proid)
                }
            }

            if (equipo.observacion.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Observacion",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = equipo.observacion,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (audit != null) {
                HorizontalDivider()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Auditoria",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text("Estado: ${audit.estado.ifBlank { "sin estado" }}")
                    if (hasPhoto) {
                        Text(
                            text = "Foto adjunta",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                                .clickable { showPhoto = true }
                        ) {
                            RemoteNetworkThumbnail(
                                url = photoUrl,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    } else {
                        Text("Sin foto")
                    }
                }
            }

            if (audit != null) {
                Button(
                    onClick = onSustainClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Outlined.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (sustentada) "Actualizar foto" else "Sustentar")
                }
            }
        }
    }

    if (showPhoto && hasPhoto) {
        Dialog(onDismissRequest = { showPhoto = false }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Foto de ${equipo.sn}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    RemoteNetworkThumbnail(
                        url = photoUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit,
                        maxHeight = 560.dp,
                    )
                    TextButton(onClick = { showPhoto = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun StockInfoChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(value.ifBlank { "sin datos" }, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RemoteNetworkThumbnail(
    url: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    maxHeight: androidx.compose.ui.unit.Dp? = null,
) {
    val bitmap = produceState<android.graphics.Bitmap?>(initialValue = null, key1 = url) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                URL(url).openStream().use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }.getOrNull()
        }
    }.value

    val imageModifier = if (maxHeight != null) {
        modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .padding(4.dp)
    } else {
        modifier
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = imageModifier,
            contentScale = contentScale,
        )
    } else {
        Box(
            modifier = imageModifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.width(20.dp))
        }
    }
}

private fun dispatchAntiquityDays(ymd: String): Long? {
    val date = runCatching { LocalDate.parse(ymd) }.getOrNull() ?: return null
    val today = LocalDate.now(ZoneId.of("America/Lima"))
    return java.time.temporal.ChronoUnit.DAYS.between(date, today).coerceAtLeast(0)
}

private fun formatYmdToDmy(ymd: String): String {
    val date = runCatching { LocalDate.parse(ymd) }.getOrNull() ?: return ymd
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

private fun formatMeters(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }
}

internal fun createStockPhotoUri(context: android.content.Context, sn: String): Uri {
    val imagesDir = File(context.cacheDir, "stock_auditoria").apply { mkdirs() }
    val file = File.createTempFile("auditoria_${sn}_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun TecnicoMapTab(
    modifier: Modifier,
    uiState: TecnicoUiState,
    onToggleMapMode: () -> Unit,
    onRefreshClick: () -> Unit,
    onRefreshCuadrillasMapa: () -> Unit = {},
) {
    val context = LocalContext.current
    var selectedOrder by remember { mutableStateOf<TecnicoMapItem?>(null) }
    var selectedCuadrilla by remember { mutableStateOf<CuadrillaMapa?>(null) }

    // Auto-refresh cuadrillas cada 30 s cuando el modo es CUADRILLAS
    LaunchedEffect(uiState.mapMode) {
        if (uiState.mapMode == MapMode.CUADRILLAS) {
            while (true) {
                kotlinx.coroutines.delay(30_000L)
                onRefreshCuadrillasMapa()
            }
        }
    }

    val locationGranted = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val mapItems = uiState.map?.items.orEmpty()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.046374, -77.042793), 11f)
    }

    LaunchedEffect(mapItems) {
        if (mapItems.size >= 2) {
            val bounds = LatLngBounds.builder()
            mapItems.forEach { bounds.include(LatLng(it.lat, it.lng)) }
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 80),
                    durationMs = 700,
                )
            }
        } else if (mapItems.size == 1) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(mapItems[0].lat, mapItems[0].lng), 13f),
                durationMs = 700,
            )
        }
    }

    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Banner de ultima actualizacion (igual que en Ordenes)
            val info = uiState.ordersData?.updateInfo
            if (info != null) {
                val bannerText = info.at?.let { "Ordenes actualizadas: ${formatBannerDateTime(it)}" }
                    ?: "Ordenes: sin registro reciente de actualizacion"
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = Color(0xFFEAF4FF),
                    contentColor = Color(0xFF0F3D68),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFBFDBFE), MaterialTheme.shapes.large)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Toggle modo mapa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    SegmentedButton(
                        selected = uiState.mapMode == MapMode.MIS_ORDENES,
                        onClick = { if (uiState.mapMode != MapMode.MIS_ORDENES) onToggleMapMode() },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text("Mis ordenes") }
                    SegmentedButton(
                        selected = uiState.mapMode == MapMode.CUADRILLAS,
                        onClick = { if (uiState.mapMode != MapMode.CUADRILLAS) onToggleMapMode() },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text("Cuadrillas") }
                }
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar mapa")
                }
            }

            // Mapa
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = locationGranted),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = locationGranted, zoomControlsEnabled = false),
                    onMapClick = {
                        selectedOrder = null
                        selectedCuadrilla = null
                    },
                ) {
                    // Marcadores de mis ordenes (siempre visibles en ambos modos)
                    mapItems.forEach { item ->
                        key(item.id) {
                            MarkerComposable(
                                keys = arrayOf(item.estado),
                                state = rememberMarkerState(position = LatLng(item.lat, item.lng)),
                                anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                                onClick = {
                                    selectedOrder = item
                                    selectedCuadrilla = null
                                    true
                                },
                            ) {
                                OrderMarkerIcon(item.estado)
                            }
                        }
                    }

                    // Marcadores de cuadrillas (solo en modo CUADRILLAS)
                    if (uiState.mapMode == MapMode.CUADRILLAS) {
                        uiState.cuadrillasMapa.forEach { cuad ->
                            key("cuad-${cuad.id}") {
                                MarkerComposable(
                                    keys = arrayOf(cuad.estadoActual),
                                    state = rememberMarkerState(position = LatLng(cuad.lat, cuad.lng)),
                                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                                    onClick = {
                                        selectedCuadrilla = cuad
                                        selectedOrder = null
                                        true
                                    },
                                ) {
                                    CuadrillaMarkerIcon(cuad.estadoActual)
                                }
                            }
                        }
                    }
                }

                // Popup de orden seleccionada
                selectedOrder?.let { order ->
                    MapOrderPopup(
                        item = order,
                        onDismiss = { selectedOrder = null },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                // Popup de cuadrilla seleccionada
                selectedCuadrilla?.let { cuad ->
                    MapCuadrillaPopup(
                        cuadrilla = cuad,
                        onDismiss = { selectedCuadrilla = null },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                // Estado vacio: mapa cargado pero sin ordenes con coordenadas
                val showEmptyState = !uiState.isMapLoading &&
                    uiState.map != null &&
                    mapItems.isEmpty() &&
                    uiState.mapMode == MapMode.MIS_ORDENES
                if (showEmptyState) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Sin ordenes con coordenadas para hoy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Loading overlay en modo cuadrillas
                if (uiState.isCuadrillasMapaLoading) {
                    CenteredLoadingOverlay(
                        modifier = Modifier.fillMaxSize(),
                        message = "Cargando cuadrillas...",
                    )
                }
                if (uiState.isMapLoading) {
                    CenteredLoadingOverlay(
                        modifier = Modifier.fillMaxSize(),
                        message = "Actualizando mapa...",
                    )
                }
            }
        }
    }
}

@Composable
private fun MapOrderPopup(
    item: TecnicoMapItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.cliente.ifBlank { item.ordenId },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Tramo: ${tramoLabel(item.fechaProgramadaHm)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.estado.isNotBlank()) {
                        Text(
                            text = item.estado,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                }
            }
            if (item.direccion.isNotBlank()) {
                Text(
                    text = item.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${item.lat},${item.lng}")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Abrir en Google Maps")
            }
        }
    }
}

@Composable
internal fun MapCuadrillaPopup(
    cuadrilla: CuadrillaMapa,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = cuadrilla.nombre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (cuadrilla.categoria.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text = cuadrilla.categoria,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                        if (cuadrilla.vehiculo.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                shape = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text = cuadrilla.vehiculo,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                    if (cuadrilla.estadoActual == "EN_ORDEN") {
                        // Cuando está en instalación no se mueve → no mostrar tiempo desactualizado
                        Surface(
                            color = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF1B5E20),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                )
                                Text(
                                    text = "En instalación · activo",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Última ubicación: ${timeAgoMillis(cuadrilla.lastLocationAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Outlined.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
internal fun OrderMarkerIcon(estado: String) {
    val (bgColor, icon) = when (estado.trim().uppercase()) {
        "FINALIZADA" -> Color(0xFF1565C0) to Icons.Outlined.CheckCircle
        "CANCELADA", "ANULADA" -> Color(0xFFB71C1C) to Icons.Outlined.Block
        "EN CAMINO" -> Color(0xFF6A1B9A) to Icons.Outlined.NearMe
        "INICIADA" -> Color(0xFF1B5E20) to Icons.Outlined.Build
        "AGENDADA" -> Color(0xFF424242) to Icons.Outlined.Schedule
        else -> Color(0xFF00695C) to Icons.Outlined.Place
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(bgColor, CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
internal fun CuadrillaMarkerIcon(estadoActual: String = "EN_RUTA") {
    val (bgColor, icon) = when (estadoActual.uppercase()) {
        "EN_ORDEN" -> Color(0xFF1B5E20) to Icons.Outlined.Build       // verde → en instalación
        else       -> Color(0xFF6A1B9A) to Icons.Outlined.LocalShipping // morado → en ruta
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(bgColor, CircleShape)
            .border(2.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
    }
}

internal fun timeAgoMillis(millis: Long?): String {
    if (millis == null) return "sin datos"
    val diff = System.currentTimeMillis() - millis
    val minutes = diff / 60_000
    return when {
        minutes < 1 -> "hace unos segundos"
        minutes < 60 -> "hace ${minutes} min"
        minutes < 1440 -> "hace ${minutes / 60} h"
        else -> "hace ${minutes / 1440} días"
    }
}

private fun tramoLabel(hm: String): String = when (hm.take(5)) {
    "08:00" -> "Primer Tramo"
    "12:00" -> "Segundo Tramo"
    "16:00" -> "Tercer Tramo"
    else -> hm.ifBlank { "-" }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectableDateHeader(
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
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }

    if (isPickerOpen) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDay(ZoneOffset.UTC)
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
                                .atZone(ZoneOffset.UTC)
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
                                    .atZone(ZoneOffset.UTC)
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

@Composable
internal fun CenteredLoadingOverlay(
    modifier: Modifier = Modifier,
    message: String,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
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

internal fun formatYmdDisplay(ymd: String): String {
    val date = runCatching { LocalDate.parse(ymd) }.getOrNull() ?: return ymd
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

internal fun formatBannerDateTime(iso: String): String {
    val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return iso
    val date = instant.atZone(ZoneId.of("America/Lima"))
    return date.format(DateTimeFormatter.ofPattern("HH:mm | dd-MM-yyyy"))
}

private fun estadoColoresTecnico(estado: String): Pair<Color, Color> = when (estado.trim().uppercase()) {
    "FINALIZADA"             -> Color(0xFFE0F2FE) to Color(0xFF0C4A6E)
    "INICIADA"               -> Color(0xFFDCFCE7) to Color(0xFF14532D)
    "CANCELADA", "ANULADA"   -> Color(0xFFFEE2E2) to Color(0xFF7F1D1D)
    "REGESTION", "REGESTIÓN" -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
    "EN CAMINO"              -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
    else                     -> Color(0xFFF5F5F5) to Color(0xFF616161)
}

private fun TecnicoOrderSummary.displayHour(): String {
    return fechaProgramadaHm.ifBlank { fechaFinVisiHm.ifBlank { "00:00" } }
}

private fun TecnicoOrderSummary.shouldShowCancellationReason(): Boolean {
    val normalized = estado.trim().uppercase()
    return (normalized == "CANCELADA" || normalized == "ANULADA") && motivoCancelacion.isNotBlank()
}

private fun String.isPlantillaOk(): Boolean = trim().uppercase() == "OK"

private fun String.toPlantillaLabel(): String = if (isPlantillaOk()) "OK" else "Pendiente"

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
