package com.redes.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.redes.app.data.tecnico.TecnicoConsumedMaterial
import com.redes.app.data.tecnico.TecnicoInstalledEquipment
import com.redes.app.ui.components.AppTopBar
import com.redes.app.ui.tecnico.TecnicoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoOrderDetailScreen(
    uiState: TecnicoUiState,
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var showPhoneOptions by remember { mutableStateOf(false) }
    var showAddressOptions by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = "Detalle de orden",
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isDetailLoading -> CenteredLoadingOverlay(
                    modifier = Modifier.fillMaxSize(),
                    message = "Cargando detalle...",
                )
                uiState.selectedOrderDetail == null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "No hay detalle disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    val detail = uiState.selectedOrderDetail
                    val isOperativeFinalized = detail.isFinalizada
                    val whatsappMessage = buildWhatsappMessage(detail.cliente)

                    if (showPhoneOptions) {
                        ActionOptionsDialog(
                            title = "Teléfono",
                            onDismiss = { showPhoneOptions = false },
                            actions = listOf(
                                "WhatsApp" to {
                                    showPhoneOptions = false
                                    openExternal(
                                        context = context,
                                        uri = Uri.parse(buildWhatsappUrl(detail.telefono, whatsappMessage)),
                                        errorMessage = "No se pudo abrir WhatsApp.",
                                    )
                                },
                                "Llamar" to {
                                    showPhoneOptions = false
                                    openExternal(
                                        context = context,
                                        uri = Uri.parse("tel:${detail.telefono.filter { it.isDigit() || it == '+' }}"),
                                        action = Intent.ACTION_DIAL,
                                        errorMessage = "No se pudo abrir el marcador.",
                                    )
                                },
                            ),
                        )
                    }

                    if (showAddressOptions) {
                        ActionOptionsDialog(
                            title = "Dirección",
                            onDismiss = { showAddressOptions = false },
                            actions = listOf(
                                "Google Maps" to {
                                    showAddressOptions = false
                                    val uri = detail.lat?.let { lat ->
                                        detail.lng?.let { lng ->
                                            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                                        }
                                    } ?: Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(detail.direccion)}")
                                    openExternal(context = context, uri = uri, errorMessage = "No se pudo abrir Google Maps.")
                                },
                                "Waze" to {
                                    showAddressOptions = false
                                    val uri = detail.lat?.let { lat ->
                                        detail.lng?.let { lng ->
                                            Uri.parse("https://waze.com/ul?ll=$lat,$lng&navigate=yes")
                                        }
                                    } ?: Uri.parse("https://waze.com/ul?q=${Uri.encode(detail.direccion)}")
                                    openExternal(context = context, uri = uri, errorMessage = "No se pudo abrir Waze.")
                                },
                            ),
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, bottom = 72.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item { Spacer(Modifier.height(8.dp)) }

                        // ── Card 1: OrdenId + Cliente + Estado ──
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // OrdenId
                                    if (detail.ordenId.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                            ) {
                                                Text(
                                                    "ORDEN ID",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    detail.ordenId,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    fontFamily = FontFamily.Monospace,
                                                )
                                            }
                                            IconButton(
                                                onClick = { copyToClipboard(clipboard, detail.ordenId, context) },
                                                modifier = Modifier.size(34.dp),
                                            ) {
                                                Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    }
                                    // Cliente
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Text(
                                                "CLIENTE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                detail.cliente.ifBlank { "Sin datos" },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                        if (detail.cliente.isNotBlank()) {
                                            IconButton(
                                                onClick = { copyToClipboard(clipboard, detail.cliente, context) },
                                                modifier = Modifier.size(34.dp),
                                            ) {
                                                Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    // Chips de estado
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (detail.estado.isNotBlank()) EstadoChipDetalle(detail.estado)
                                        val plantillaOk = detail.plantillaStatus.trim().uppercase() == "OK"
                                        StatusBadgeChip(
                                            text = if (plantillaOk) "Plantilla OK" else "Plantilla pendiente",
                                            isOk = plantillaOk,
                                        )
                                        if (isOperativeFinalized) {
                                            val liquidOk = detail.isLiquidated
                                            StatusBadgeChip(
                                                text = detail.resolveLiquidacionLabel(),
                                                isOk = liquidOk,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Card 2: Datos de contacto ──
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    DetailInfoRow(
                                        label = "Código de cliente",
                                        value = detail.codigoCliente.ifBlank { "Sin datos" },
                                        onCopy = detail.codigoCliente.takeIf { it.isNotBlank() }?.let {
                                            { copyToClipboard(clipboard, it, context) }
                                        },
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    DetailInfoRow(
                                        label = "Documento",
                                        value = detail.documento.ifBlank { "Sin datos" },
                                        onCopy = detail.documento.takeIf { it.isNotBlank() }?.let {
                                            { copyToClipboard(clipboard, it, context) }
                                        },
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    DetailInfoRow(
                                        label = "Teléfono",
                                        value = detail.telefono.ifBlank { "Sin datos" },
                                        onCopy = detail.telefono.takeIf { it.isNotBlank() }?.let {
                                            { copyToClipboard(clipboard, it, context) }
                                        },
                                        actionIcon = Icons.Outlined.Call.takeIf { detail.telefono.isNotBlank() },
                                        onAction = detail.telefono.takeIf { it.isNotBlank() }?.let { { showPhoneOptions = true } },
                                        actionFirst = true,
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    DetailInfoRow(
                                        label = "Dirección",
                                        value = detail.direccion.ifBlank { "Sin dirección" },
                                        actionIcon = Icons.Outlined.LocationOn.takeIf { detail.direccion.isNotBlank() },
                                        onAction = detail.direccion.takeIf { it.isNotBlank() }?.let { { showAddressOptions = true } },
                                    )
                                }
                            }
                        }

                        // ── Card 3: Servicio y equipos solicitados ──
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    val planLabel = detail.plan.ifBlank { detail.tipoServicio.ifBlank { "Sin datos" } }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            "PLAN / SERVICIO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            planLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        EquipoCountChip("MESH", detail.cantMesh, Color(0xFFAD1457))
                                        EquipoCountChip("FONO", detail.cantFono, Color(0xFF00695C))
                                        EquipoCountChip("BOX", detail.cantBox, Color(0xFFE65100))
                                    }
                                }
                            }
                        }

                        // ── Liquidación ──
                        if (isOperativeFinalized && !detail.isLiquidated) {
                            item {
                                Button(onClick = {
                                    Toast.makeText(context, "Liquidacion movil pendiente de implementacion.", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Liquidar")
                                }
                            }
                        } else if (isOperativeFinalized && detail.isLiquidated) {
                            // Acta + Servicios
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            "ACTA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            detail.acta.ifBlank { "Pendiente" },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        if (detail.servicios.isNotEmpty()) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                            Text(
                                                "SERVICIOS",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                detail.servicios.forEach { servicio ->
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        shape = MaterialTheme.shapes.extraSmall,
                                                    ) {
                                                        Text(
                                                            servicio,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Equipos instalados
                            if (detail.equipos.isNotEmpty()) {
                                item {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                "Equipos instalados",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            HorizontalDivider()
                                            detail.equipos.forEach { EquipoInstalledRow(it) }
                                        }
                                    }
                                }
                            }

                            // Materiales instalados
                            if (detail.materiales.isNotEmpty()) {
                                item {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                "Materiales instalados",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            HorizontalDivider()
                                            detail.materiales.forEach { MaterialConsumedRow(it) }
                                        }
                                    }
                                }
                            }
                        }

                        // Observación
                        if (isOperativeFinalized && detail.observacion.isNotBlank()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            "Observación",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            detail.observacion,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }

                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

// ── Componentes visuales ──────────────────────────────────────────

@Composable
private fun EstadoChipDetalle(estado: String) {
    val (bg, fg) = estadoColoresDetail(estado)
    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.extraSmall) {
        Text(
            estado,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusBadgeChip(text: String, isOk: Boolean) {
    val (bg, fg) = if (isOk) Color(0xFFE8F5E9) to Color(0xFF1B5E20) else Color(0xFFFFF8E1) to Color(0xFFE65100)
    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.extraSmall) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EquipoCountChip(tipo: String, count: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Text(
            "$tipo: $count",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    onCopy: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    actionFirst: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onAction != null) Modifier.clickable(onClick = onAction) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (actionFirst) {
            if (actionIcon != null && onAction != null) {
                IconButton(onClick = onAction, modifier = Modifier.size(34.dp)) {
                    Icon(actionIcon, null, modifier = Modifier.size(18.dp))
                }
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                }
            }
        } else {
            if (onCopy != null) {
                IconButton(onClick = onCopy, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                }
            }
            if (actionIcon != null && onAction != null) {
                IconButton(onClick = onAction, modifier = Modifier.size(34.dp)) {
                    Icon(actionIcon, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EquipoInstalledRow(equipo: TecnicoInstalledEquipment) {
    val tipoColor = when (equipo.tipo.trim().uppercase()) {
        "ONT"  -> Color(0xFF1565C0)
        "MESH" -> Color(0xFFAD1457)
        "FONO" -> Color(0xFF00695C)
        "BOX"  -> Color(0xFFE65100)
        else   -> Color(0xFF616161)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = tipoColor.copy(alpha = 0.1f),
            contentColor = tipoColor,
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(
                equipo.tipo.ifBlank { "?" },
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            equipo.sn.ifBlank { "-" },
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (equipo.proid.isNotBlank()) {
            Text(
                equipo.proid,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MaterialConsumedRow(material: TecnicoConsumedMaterial) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Text(
            material.nombre.ifBlank { material.materialId.ifBlank { "Material" } },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (material.cantidad > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    "${material.cantidad.cleanAmount()} und",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (material.metros > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    "${material.metros.cleanAmount()} m",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ActionOptionsDialog(
    title: String,
    onDismiss: () -> Unit,
    actions: List<Pair<String, () -> Unit>>,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { (label, action) ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = action),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = when (label) {
                                    "WhatsApp" -> Icons.AutoMirrored.Outlined.Message
                                    "Llamar"   -> Icons.Outlined.Call
                                    else       -> Icons.AutoMirrored.Outlined.OpenInNew
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
    )
}

// ── Helpers ──────────────────────────────────────────────────────

private fun estadoColoresDetail(estado: String): Pair<Color, Color> = when (estado.trim().uppercase()) {
    "FINALIZADA"             -> Color(0xFFE0F2FE) to Color(0xFF0C4A6E)
    "INICIADA"               -> Color(0xFFDCFCE7) to Color(0xFF14532D)
    "CANCELADA", "ANULADA"   -> Color(0xFFFEE2E2) to Color(0xFF7F1D1D)
    "REGESTION", "REGESTIÓN" -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
    "EN CAMINO"              -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
    "AGENDADA"               -> Color(0xFFF5F5F5) to Color(0xFF424242)
    else                     -> Color(0xFFF5F5F5) to Color(0xFF616161)
}

private fun copyToClipboard(
    clipboard: androidx.compose.ui.platform.ClipboardManager,
    value: String,
    context: android.content.Context,
) {
    clipboard.setText(AnnotatedString(value))
    Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
}

private fun buildWhatsappMessage(cliente: String): String {
    return "Buenos dias, estimado ${cliente.ifBlank { "cliente" }}. 👋 Le saluda el tecnico de WIN. Me comunico con usted para coordinar la instalacion de su servicio de Internet WIN. Quedo atento a su disponibilidad. Muchas gracias."
}

private fun buildWhatsappUrl(phone: String, message: String): String {
    val digits = phone.filter { it.isDigit() }
    val withCountry = if (digits.startsWith("51") && digits.length >= 11) digits else "51$digits"
    return "https://wa.me/$withCountry?text=${Uri.encode(message)}"
}

private fun openExternal(
    context: android.content.Context,
    uri: Uri,
    action: String = Intent.ACTION_VIEW,
    errorMessage: String,
) {
    val intent = Intent(action, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
        .onFailure { Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show() }
}

private fun Double.cleanAmount(): String =
    if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()

private fun com.redes.app.data.tecnico.TecnicoOrderDetail.resolveLiquidacionLabel(): String = when {
    isLiquidated -> "Liquidado"
    liquidacionEstado.isNotBlank() -> liquidacionEstado
    else -> "Pendiente"
}
