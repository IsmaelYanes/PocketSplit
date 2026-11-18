package com.develop.pocketsplit.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.develop.pocketsplit.R
import com.develop.pocketsplit.ui.navigation.Routes

private data class NavigationDestination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(currentRoute: String, onNavigate: (String) -> Unit) {
    val destinations = listOf(
        NavigationDestination(Routes.HOME, R.string.navigation_home, Icons.Default.Home),
        NavigationDestination(Routes.STATISTICS, R.string.statistics_title, Icons.Default.BarChart),
        NavigationDestination(Routes.SETTINGS, R.string.settings_title, Icons.Default.Settings)
    )

    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route ||
                (currentRoute == Routes.FINANCE && destination.route == Routes.HOME)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
                // Inicio debe seguir siendo accionable en Finanzas aunque esté seleccionado.
                modifier = Modifier.semantics { role = Role.Button },
                icon = { Icon(imageVector = destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.label)) }
            )
        }
    }
}
