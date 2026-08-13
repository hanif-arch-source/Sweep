package com.sweep.networkmonitor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sweep.networkmonitor.R
import com.sweep.networkmonitor.ui.alerts.AlertsScreen
import com.sweep.networkmonitor.ui.dashboard.DashboardScreen
import com.sweep.networkmonitor.ui.devices.DeviceDetailsScreen
import com.sweep.networkmonitor.ui.devices.DevicesScreen
import com.sweep.networkmonitor.ui.settings.SettingsScreen
import com.sweep.networkmonitor.ui.theme.SweepRed
import com.sweep.networkmonitor.viewmodel.NetworkViewModel

private sealed class SweepDestination(val route: String) {
    data object Dashboard : SweepDestination("dashboard")
    data object Devices : SweepDestination("devices")
    data object DeviceDetails : SweepDestination("devices/{ip}") {
        fun route(ip: String) = "devices/$ip"
    }
    data object Alerts : SweepDestination("alerts")
    data object Settings : SweepDestination("settings")
}

private data class BottomTab(
    val destination: SweepDestination,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomTabs = listOf(
    BottomTab(SweepDestination.Dashboard, R.string.nav_dashboard, Icons.Filled.Wifi),
    BottomTab(SweepDestination.Devices, R.string.nav_devices, Icons.Filled.Router),
    BottomTab(SweepDestination.Alerts, R.string.nav_alerts, Icons.Filled.Notifications),
    BottomTab(SweepDestination.Settings, R.string.nav_settings, Icons.Filled.Settings)
)

/**
 * Top-level scaffold: bottom navigation across Dashboard / Devices / Alerts
 * / Settings, plus the Device Details screen reached from Devices.
 *
 * [viewModel] is supplied by the caller (see MainActivity), which wires it
 * up via [com.sweep.networkmonitor.viewmodel.NetworkViewModelFactory] so it
 * has a real NetworkRepository/NotificationHelper behind it.
 */
@Composable
fun SweepNavHost(viewModel: NetworkViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                bottomTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SweepRed,
                            selectedTextColor = SweepRed,
                            indicatorColor = SweepRed.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = SweepDestination.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(SweepDestination.Dashboard.route) {
                DashboardScreen(
                    state = uiState,
                    onRefresh = viewModel::refresh,
                    onScan = viewModel::scanNetwork,
                    onOpenSettings = { navController.navigate(SweepDestination.Settings.route) }
                )
            }
            composable(SweepDestination.Devices.route) {
                DevicesScreen(
                    devices = uiState.devices,
                    onDeviceClick = { device ->
                        navController.navigate(SweepDestination.DeviceDetails.route(device.ipAddress))
                    }
                )
            }
            composable(SweepDestination.DeviceDetails.route) { backStackEntry ->
                val ip = backStackEntry.arguments?.getString("ip")
                val device = ip?.let { viewModel.deviceById(it) }
                DeviceDetailsScreen(
                    device = device,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(SweepDestination.Alerts.route) {
                AlertsScreen(
                    alerts = uiState.alerts,
                    onAlertClick = { alert -> viewModel.markAlertRead(alert.id) }
                )
            }
            composable(SweepDestination.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    onSettingsChange = viewModel::updateSettings
                )
            }
        }
    }
}
