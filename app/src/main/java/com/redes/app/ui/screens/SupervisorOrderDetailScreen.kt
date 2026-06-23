package com.redes.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.redes.app.ui.supervisor.SupervisorUiState

private val BrandBlue = Color(0xFF30518C)
private val GarantiaGold = Color(0xFFFFD54F)
private val GarantiaRed = Color(0xFF92400E)
private val SupervisadaGreen = Color(0xFF10B981)

private val RESPONSABLE_OPTIONS = listOf("Cuadrilla", "Cliente", "Externo")
private val CASO_OPTIONS = listOf(
    "Cambio de ONT", "Cambio de MESH", "Cambio de FONO", "Cambio de BOX",
    "Cambio de Conector", "Cambio de Roseta", "Recableado", "Reubicacion",
)
private val IMPUTADO_OPTIONS = listOf("REDES M&D", "WIN")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorOrderDetailScreen(
    uiState: SupervisorUiState,
    onBackClick: () -> Unit,
    onSaveSupervision: (orderId: String, notas: String, observaciones: String) -> Unit,
    onUpdateGarantia: (ordenId: String, motivo: String, diagnostico: String, solucion: String, responsable: String, caso: String, imputado: String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    val detail = uiState.selectedOrderDetail

    var notasText by remember(detail?.id) { mutableStateOf(detail?.supervision?.notas ?: "") }
    var obsText by remember(detail?.id) { mutableStateOf(detail?.supervision?.observaciones ?: "") }

    // Garantia edit state — reset when detail changes
    var diagnosticoText by remember(detail?.id) { mutableStateOf(detail?.diagnosticoGarantia ?: "") }
    var solucionText by remember(detail?.id) { mutableStateOf(detail?.solucionGarantia ?: "") }
    var responsableSelected by remember(detail?.id) { mutableStateOf(detail?.responsableGarantia ?: "") }
    var casoSelected by remember(detail?.id) { mutableStateOf(detail?.casoGarantia ?: "") }
    var imputadoSelected by remember(detail?.id) { mutableStateOf(detail?.imputadoGarantia ?: "") }

    LaunchedEffect(uiState.garantiaSaved) {
        if (uiState.garantiaSaved) {
            // detail reloads automatically via loadOrderDetail in ViewModel
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(detail?.ordenId ?: "Detalle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (detail?.isGarantia == true) {
                            Text("GARANTÍA", fontSize = 11.sp, color = GarantiaRed, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, contentDescription = "Configuracion") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        if (uiState.isDetailLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandBlue)
            }
            return@Scaffold
        }

        if (detail == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Orden no disponible", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Estado + garantia badge
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val statusColor = when {
                    detail.estado.uppercase().contains("FINALIZ") -> Color(0xFF1D4ED8)
                    detail.estado.uppercase().contains("CANCEL") -> Color(0xFFDC2626)
                    detail.estado.uppercase().contains("CAMINO") -> Color(0xFF7C3AED)
                    detail.estado.uppercase().contains("INICI") -> SupervisadaGreen
                    else -> Color(0xFF475569)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(detail.estado.ifBlank { "Sin estado" }, fontSize = 13.sp, color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                if (detail.isGarantia) {
                    Surface(shape = RoundedCornerShape(20.dp), color = GarantiaGold.copy(alpha = 0.2f), border = BorderStroke(1.dp, GarantiaGold)) {
                        Text("GARANTÍA", fontSize = 11.sp, color = Color(0xFF78350F), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                    }
                }
            }

            // Cliente info
            DetailCard(title = "Cliente") {
                DetailRow("Nombre", detail.cliente)
                if (detail.codigoCliente.isNotBlank()) DetailRow("Código", detail.codigoCliente)
                if (detail.documento.isNotBlank()) DetailRow("Documento", detail.documento)
                if (detail.telefono.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Teléfono", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(detail.telefono, fontSize = 14.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${detail.telefono}"))) },
                                modifier = Modifier.size(36.dp)
                            ) { Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = BrandBlue, modifier = Modifier.size(20.dp)) }
                            IconButton(
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${detail.telefono.replace(Regex("[^0-9]"), "").let { d -> if (d.startsWith("51") && d.length >= 11) d else "51$d" }}"))) },
                                modifier = Modifier.size(36.dp)
                            ) { Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }

            // Orden info
            DetailCard(title = "Orden") {
                DetailRow("Orden ID", detail.ordenId)
                DetailRow("Fecha", "${detail.fechaProgramadaYmd} ${detail.fechaProgramadaHm}".trim())
                if (detail.tipoTrabajo.isNotBlank()) DetailRow("Tipo", detail.tipoTrabajo)
                if (detail.plan.isNotBlank()) DetailRow("Plan", detail.plan)
                DetailRow("Región", detail.region.ifBlank { "Sin región" })
                DetailRow("Cuadrilla", detail.cuadrillaNombre.ifBlank { "Sin cuadrilla" })
            }

            // Direccion + mapa
            if (detail.direccion.isNotBlank()) {
                DetailCard(title = "Dirección") {
                    Text(detail.direccion, fontSize = 14.sp)
                    if (detail.lat != null && detail.lng != null) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${detail.lat},${detail.lng}")))
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Abrir en Google Maps", fontSize = 13.sp)
                        }
                    }
                }
            }

            // ─── Sección de edición de garantía ───────────────────────────────
            if (detail.isGarantia) {
                DetailCard(
                    title = "Datos de Garantía",
                    titleAction = {
                        Surface(shape = RoundedCornerShape(6.dp), color = GarantiaGold.copy(alpha = 0.15f)) {
                            Text("Editar", fontSize = 11.sp, color = GarantiaRed, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    },
                ) {
                    if (detail.motivoGarantia.isNotBlank()) {
                        DetailRow("Motivo", detail.motivoGarantia)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), thickness = 0.5.dp)
                    }
                    OutlinedTextField(
                        value = diagnosticoText,
                        onValueChange = { diagnosticoText = it },
                        label = { Text("Diagnóstico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                    )
                    OutlinedTextField(
                        value = solucionText,
                        onValueChange = { solucionText = it },
                        label = { Text("Solución") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2,
                    )
                    GarantiaDropdown(
                        label = "Responsable",
                        options = RESPONSABLE_OPTIONS,
                        selected = responsableSelected,
                        onSelect = { responsableSelected = it },
                    )
                    GarantiaDropdown(
                        label = "Caso",
                        options = CASO_OPTIONS,
                        selected = casoSelected,
                        onSelect = { casoSelected = it },
                    )
                    GarantiaDropdown(
                        label = "Imputado",
                        options = IMPUTADO_OPTIONS,
                        selected = imputadoSelected,
                        onSelect = { imputadoSelected = it },
                    )

                    if (uiState.garantiaSaved) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0FDF4), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SupervisadaGreen, modifier = Modifier.size(16.dp))
                                Text("Garantía actualizada correctamente", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onUpdateGarantia(
                                detail.id,
                                detail.motivoGarantia,  // preservar motivo original, no editable
                                diagnosticoText,
                                solucionText,
                                responsableSelected,
                                casoSelected,
                                imputadoSelected,
                            )
                        },
                        enabled = !uiState.isSavingGarantia,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF92400E)),
                    ) {
                        if (uiState.isSavingGarantia) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Guardar garantía", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Supervision section
            DetailCard(
                title = "Supervisión",
                titleAction = if (detail.supervision != null) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(Modifier.size(8.dp).background(SupervisadaGreen, RoundedCornerShape(4.dp)))
                            Text("Supervisada", fontSize = 11.sp, color = SupervisadaGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else null,
            ) {
                if (detail.isGarantia) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GarantiaGold.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = GarantiaRed, modifier = Modifier.size(16.dp))
                            Text("Las garantías deben supervisarse en campo.", fontSize = 12.sp, color = GarantiaRed, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                OutlinedTextField(
                    value = notasText,
                    onValueChange = { notasText = it },
                    label = { Text("Notas de supervisión") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text("Observaciones generales de la visita...", fontSize = 13.sp) },
                )
                OutlinedTextField(
                    value = obsText,
                    onValueChange = { obsText = it },
                    label = { Text("Observaciones técnicas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text("Detalles técnicos o incidencias...", fontSize = 13.sp) },
                )

                Button(
                    onClick = { onSaveSupervision(detail.id, notasText, obsText) },
                    enabled = !uiState.isSavingSupervision,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (detail.isGarantia) GarantiaRed else BrandBlue),
                ) {
                    if (uiState.isSavingSupervision) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (detail.supervision != null) "Actualizar supervisión" else "Guardar supervisión", fontWeight = FontWeight.SemiBold)
                }

                if (detail.supervision != null) {
                    Text("Última supervisión: ${detail.supervision.supervisadoEn.take(19).replace("T", " ")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }

            if (uiState.errorMessage != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEF2F2), modifier = Modifier.fillMaxWidth()) {
                    Text(uiState.errorMessage, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Color(0xFFDC2626))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GarantiaDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.ifBlank { "Seleccionar..." },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = if (selected.isBlank()) OutlinedTextFieldDefaults.colors(
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            ) else OutlinedTextFieldDefaults.colors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    titleAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), letterSpacing = 0.8.sp)
                titleAction?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.weight(0.4f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f), textAlign = androidx.compose.ui.text.style.TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
