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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.redes.app.R
import com.redes.app.ui.home.HomeUiState

@Composable
fun NotificationsScreen(
    uiState: HomeUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.notifications_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(stringResource(R.string.notifications_subtitle))
                }
            }

            if (uiState.comunicados.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.notifications_empty),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.comunicados, key = { it.id }) { comunicado ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = comunicado.titulo.ifBlank { "Comunicado" },
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = buildString {
                                        append(if (comunicado.obligatorio) "Obligatorio" else "Informativo")
                                        append(" | ")
                                        append(comunicado.persistencia)
                                    },
                                )
                                if (comunicado.cuerpo.isNotBlank()) {
                                    Text(comunicado.cuerpo)
                                }
                            }
                        }
                    }
                }
            }

            Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_back_action))
            }
        }
    }
}
