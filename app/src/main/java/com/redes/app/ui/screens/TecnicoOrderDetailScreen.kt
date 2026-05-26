package com.redes.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                uiState.isDetailLoading -> CircularProgressIndicator()
                uiState.selectedOrderDetail == null -> Text("No hay detalle disponible.")
                else -> {
                    val detail = uiState.selectedOrderDetail
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(detail.cliente.ifBlank { detail.ordenId }, style = MaterialTheme.typography.titleLarge)
                                    Text("Orden: ${detail.ordenId}")
                                    Text("Codigo cliente: ${detail.codigoCliente.ifBlank { "sin datos" }}")
                                    Text("Direccion: ${detail.direccion.ifBlank { "sin direccion" }}")
                                    Text("Tipo: ${detail.tipoTrabajo.ifBlank { "sin datos" }}")
                                    Text("Servicio: ${detail.tipoServicio.ifBlank { "sin datos" }}")
                                    Text("Plan: ${detail.plan.ifBlank { "sin datos" }}")
                                    Text("Estado: ${detail.estado.ifBlank { "sin datos" }}")
                                    Text("Liquidacion: ${detail.liquidacionEstado.ifBlank { if (detail.isLiquidated) "LIQUIDADO" else "PENDIENTE" }}")
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Materiales usados", style = MaterialTheme.typography.titleMedium)
                                    if (detail.materiales.isEmpty()) {
                                        Text("Sin materiales registrados.")
                                    } else {
                                        detail.materiales.forEach { material ->
                                            Text("${material.nombre} | und ${material.cantidad} | m ${material.metros}")
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Equipos instalados", style = MaterialTheme.typography.titleMedium)
                                    if (detail.equipos.isEmpty()) {
                                        Text("Sin equipos registrados.")
                                    } else {
                                        detail.equipos.forEach { equipo ->
                                            Text("${equipo.tipo.ifBlank { "Equipo" }} | ${equipo.sn} ${equipo.proid.takeIf { it.isNotBlank() }?.let { " | $it" } ?: ""}")
                                        }
                                    }
                                }
                            }
                        }
                        if (detail.observacion.isNotBlank()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Observacion", style = MaterialTheme.typography.titleMedium)
                                        Text(detail.observacion)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
