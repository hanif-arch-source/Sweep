package com.sweep.networkmonitor.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sweep.networkmonitor.data.model.Device
import com.sweep.networkmonitor.ui.components.MetricRow
import com.sweep.networkmonitor.ui.components.StatusPill
import com.sweep.networkmonitor.ui.components.SweepCard
import com.sweep.networkmonitor.ui.theme.SweepCharcoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Detail screen for an individual discovered device (Section 4, FR-07). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    device: Device?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(device?.displayName ?: "Device") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (device == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("This device is no longer in the current scan results.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SweepCard {
                Row {
                    StatusPill(
                        isPositive = device.isOnline,
                        positiveText = "ONLINE",
                        negativeText = "OFFLINE"
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = SweepCharcoal,
                    fontWeight = FontWeight.Bold
                )
            }

            SweepCard {
                MetricRow("IP Address", device.ipAddress)
                Spacer(Modifier.height(10.dp))
                MetricRow("MAC Address", device.macAddress ?: "Unavailable")
                Spacer(Modifier.height(10.dp))
                MetricRow("Hostname", device.hostname ?: "Unavailable")
                Spacer(Modifier.height(10.dp))
                MetricRow("Manufacturer", device.manufacturer ?: "Unknown")
                Spacer(Modifier.height(10.dp))
                MetricRow("Last seen", formatTimestamp(device.lastSeenMillis))
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(millis))
