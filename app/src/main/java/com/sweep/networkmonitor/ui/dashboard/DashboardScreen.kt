package com.sweep.networkmonitor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sweep.networkmonitor.R
import com.sweep.networkmonitor.data.model.Network
import com.sweep.networkmonitor.data.model.TrafficPoint
import com.sweep.networkmonitor.data.model.TrafficStats
import com.sweep.networkmonitor.ui.components.MetricRow
import com.sweep.networkmonitor.ui.components.SectionLabel
import com.sweep.networkmonitor.ui.components.SignalBars
import com.sweep.networkmonitor.ui.components.StatusPill
import com.sweep.networkmonitor.ui.components.SweepCard
import com.sweep.networkmonitor.ui.theme.SweepCharcoal
import com.sweep.networkmonitor.ui.theme.SweepCharcoalMuted
import com.sweep.networkmonitor.ui.theme.SweepRed
import com.sweep.networkmonitor.viewmodel.SweepUiState

/**
 * The Dashboard is the main screen. It should immediately answer: Is the
 * network connected? How strong is the connection? How much traffic is
 * moving? How many devices were discovered? (Section 5.1 of the spec.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: SweepUiState,
    onRefresh: () -> Unit,
    onScan: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SWEEP",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringRes(R.string.nav_settings))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { ConnectionCard(state.network) }
            item { TrafficCard(state.traffic, state.trafficHistory) }
            item { DevicesSummaryCard(deviceCount = state.devices.size) }
            item {
                ActionRow(
                    isScanning = state.isScanning,
                    isRefreshing = state.isRefreshing,
                    onScan = onScan,
                    onRefresh = onRefresh
                )
            }
        }
    }
}

@Composable
private fun ConnectionCard(network: Network) {
    SweepCard {
        StatusPill(
            isPositive = network.isConnected,
            positiveText = stringRes(R.string.dashboard_connected),
            negativeText = stringRes(R.string.dashboard_disconnected)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = network.ssid ?: "—",
            style = MaterialTheme.typography.headlineMedium,
            color = SweepCharcoal
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel(stringRes(R.string.dashboard_signal_strength))
            SignalBars(bars = network.signalBars)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (network.isConnected) "${network.signalStrength} dBm" else "—",
            style = MaterialTheme.typography.bodyMedium,
            color = SweepCharcoalMuted
        )

        Spacer(Modifier.height(12.dp))
        MetricRow(stringRes(R.string.dashboard_link_speed), if (network.isConnected) "${network.linkSpeed} Mbps" else "—")
        Spacer(Modifier.height(6.dp))
        MetricRow(stringRes(R.string.dashboard_local_ip), network.ipAddress ?: "—")
    }
}

@Composable
private fun TrafficCard(traffic: TrafficStats, history: List<TrafficPoint>) {
    SweepCard {
        SectionLabel(stringRes(R.string.dashboard_traffic_header))
        Spacer(Modifier.height(12.dp))
        TrafficGraph(points = history)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TrafficReading(
                label = "\u2193 " + stringRes(R.string.dashboard_download),
                value = "%.1f Mbps".format(traffic.downloadSpeedMbps),
                color = SweepRed
            )
            TrafficReading(
                label = "\u2191 " + stringRes(R.string.dashboard_upload),
                value = "%.1f Mbps".format(traffic.uploadSpeedMbps),
                color = SweepCharcoalMuted
            )
        }
    }
}

@Composable
private fun TrafficReading(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SweepCharcoalMuted)
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DevicesSummaryCard(deviceCount: Int) {
    SweepCard {
        SectionLabel("Devices")
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResFormatted(R.string.dashboard_devices_found, deviceCount),
            style = MaterialTheme.typography.titleLarge,
            color = SweepCharcoal,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionRow(
    isScanning: Boolean,
    isRefreshing: Boolean,
    onScan: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onScan,
            enabled = !isScanning,
            colors = ButtonDefaults.buttonColors(containerColor = SweepRed),
            modifier = Modifier.weight(1f)
        ) {
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.height(16.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                Spacer(Modifier.height(0.dp))
                Text(text = " " + stringRes(R.string.action_scanning))
            } else {
                Icon(Icons.Filled.Search, contentDescription = null)
                Text(text = " " + stringRes(R.string.action_scan_network))
            }
        }
        OutlinedButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Text(text = " " + stringRes(R.string.action_refresh))
        }
    }
}

// --- small local helpers ---

@Composable
private fun stringRes(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
private fun stringResFormatted(id: Int, vararg args: Any): String =
    androidx.compose.ui.res.stringResource(id, *args)
