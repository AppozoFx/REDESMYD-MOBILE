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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.redes.app.R
import com.redes.app.data.session.MobileComunicado
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.home.blockingComunicados

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComunicadosScreen(
    uiState: HomeUiState,
    onMarkSeenClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = if (uiState.blockingComunicados.isNotEmpty()) {
        stringResource(R.string.comunicados_blocking_subtitle)
    } else {
        stringResource(R.string.comunicados_optional_subtitle)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.comunicados_title)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
            )

            if (uiState.comunicados.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.comunicados_empty),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.comunicados, key = { it.id }) { comunicado ->
                        ComunicadoCard(
                            comunicado = comunicado,
                            onMarkSeenClick = { onMarkSeenClick(comunicado.id) },
                        )
                    }
                }
            }

            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.logout_button))
            }
        }
    }
}

@Composable
private fun ComunicadoCard(
    comunicado: MobileComunicado,
    onMarkSeenClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = comunicado.titulo.ifBlank { "Comunicado" },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = buildString {
                    append(if (comunicado.obligatorio) "Obligatorio" else "Opcional")
                    append(" | ")
                    append(comunicado.persistencia)
                },
                style = MaterialTheme.typography.labelMedium,
            )
            if (comunicado.cuerpo.isNotBlank()) {
                Text(
                    text = comunicado.cuerpo,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onMarkSeenClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.comunicados_mark_seen))
            }
        }
    }
}
