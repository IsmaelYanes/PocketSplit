package com.develop.pocketsplit.ui.screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.pocketsplit.R
import com.develop.pocketsplit.ui.components.RoundedListItem
import com.develop.pocketsplit.ui.theme.PocketSplitTheme

// Mover a ui/screen/home/HomeUiState.kt cuando se cree el ViewModel.
data class HomeItem(val id: String, val title: String, val isGroup: Boolean)

data class HomeUiState(val items: List<HomeItem> = emptyList()) {
    companion object {
        /** Creamos gestiones de ejemplo */
        fun preview() = HomeUiState(
            items = listOf(
                HomeItem("personal", "Gastos personales", isGroup = false),
                HomeItem("g1", "Viaje a Tenerife", isGroup = true),
                HomeItem("g2", "Piso compartido", isGroup = true)
            )
        )
    }
}

/** PLANTILLA: Pantalla de Inicio */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeUiState = HomeUiState.preview(),
    onItemClick: (HomeItem) -> Unit = {},
    onAddClick: () -> Unit = {},
    onNavigateToFinance: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // En la cabecera se pone el nombre y logo de la app (cambiar a futuro el logo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null
                    )
                }
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.items, key = { it.id }) { item ->
                RoundedListItem(
                    text = item.title,
                    icon = if (item.isGroup) Icons.Default.Group else Icons.Default.Person,
                    onClick = onNavigateToFinance
                )
            }
            // Botón "+" debajo de la lista, como en el mockup
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledTonalIconButton(
                        onClick = onAddClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.home_add)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PocketSplitTheme { HomeScreen() }
}