package com.redes.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redes.app.data.coordinador.CoordinadorOrdenDetail
import com.redes.app.ui.coordinador.CoordinadorUiState

private val CoordBrandBlue = Color(0xFF30518C)
private val CoordGarantiaRed = Color(0xFF92400E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoordinadorOrderDetailScreen(
    uiState: CoordinadorUiState,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val detail = uiState.selectedOrdenDetail

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(detail?.ordenId ?: "Detalle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (detail?.isGarantia == true) {
                            Text("GARANTÍA", fontSize = 11.sp, color = CoordGarantiaRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CoordBrandBlue, titleContentColor = Color.White, navigationIconContentColor = Color.White),
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isOrdenDetailLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                detail == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se pudo cargar el detalle.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Estado
                        DetailEstadoChip(detail.estado)

                        // Datos del cliente
                        DetailSection(title = "Cliente") {
                            if (detail.cliente.isNotBlank()) DetailRow(Icons.Default.Person, "Nombre", detail.cliente)
                            if (detail.codigoCliente.isNotBlank()) DetailRow(Icons.Default.Tag, "Código", detail.codigoCliente)
                            if (detail.documento.isNotBlank()) DetailRow(Icons.Default.Badge, "Documento", detail.documento)
                            if (detail.telefono.isNotBlank()) {
                                val phone = detail.telefono
                                DetailRow(
                                    icon = Icons.Default.Phone,
                                    label = "Teléfono",
                                    value = phone,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }

                        // Datos de la orden
                        DetailSection(title = "Orden") {
                            if (detail.tipoTrabajo.isNotBlank()) DetailRow(Icons.Default.Build, "Tipo", detail.tipoTrabajo)
                            if (detail.tipoServicio.isNotBlank()) DetailRow(Icons.Default.Layers, "Servicio", detail.tipoServicio)
                            if (detail.fechaProgramadaHm.isNotBlank()) DetailRow(Icons.Default.Schedule, "Hora", detail.fechaProgramadaHm)
                            if (detail.fechaProgramadaYmd.isNotBlank()) DetailRow(Icons.Default.CalendarToday, "Fecha", detail.fechaProgramadaYmd)
                            if (detail.region.isNotBlank()) DetailRow(Icons.Default.LocationCity, "Región", detail.region)
                            if (detail.cuadrillaNombre.isNotBlank()) DetailRow(Icons.Default.Group, "Cuadrilla", detail.cuadrillaNombre)
                        }

                        // Equipos
                        val hasEquipos = detail.cantMesh > 0 || detail.cantFono > 0 || detail.cantBox > 0
                        if (hasEquipos) {
                            DetailSection(title = "Equipos") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (detail.cantMesh > 0) EquipoChip("MESH", detail.cantMesh)
                                    if (detail.cantFono > 0) EquipoChip("FONO", detail.cantFono)
                                    if (detail.cantBox > 0) EquipoChip("BOX", detail.cantBox)
                                }
                            }
                        }

                        // Dirección / Mapa
                        if (detail.direccion.isNotBlank()) {
                            DetailSection(title = "Dirección") {
                                val lat = detail.lat
                                val lng = detail.lng
                                DetailRow(
                                    icon = Icons.Default.Place,
                                    label = "Dirección",
                                    value = detail.direccion,
                                    onClick = if (lat != null && lng != null) ({
                                        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(Orden)")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.setPackage("com.google.android.apps.maps")
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                        } else {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        }
                                    }) else null,
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailEstadoChip(estado: String) {
    val (bg, fg) = when {
        estado.contains("FINAL") -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        estado.contains("CANCEL") || estado.contains("ANUL") -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
        estado == "EN CAMINO" -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
        estado == "INICIADA" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        estado == "AGENDADA" -> Color(0xFFF5F5F5) to Color(0xFF424242)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(6.dp)) {
        Text(
            estado,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = CoordBrandBlue)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = if (onClick != null) Modifier.fillMaxWidth().padding(vertical = 2.dp)
            .then(Modifier.clickable(onClick = onClick))
        else Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        if (onClick != null) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp).padding(top = 4.dp), tint = CoordBrandBlue)
        }
    }
}

@Composable
private fun EquipoChip(label: String, count: Int) {
    Surface(color = Color(0xFFE3F2FD), contentColor = Color(0xFF1565C0), shape = RoundedCornerShape(4.dp)) {
        Text(
            "$label $count",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
