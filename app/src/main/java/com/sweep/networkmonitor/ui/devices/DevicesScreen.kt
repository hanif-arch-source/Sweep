package com.sweep.networkmonitor.ui.devices

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sweep.networkmonitor.R
import com.sweep.networkmonitor.data.model.Device
import com.sweep.networkmonitor.ui.components.StatusDot
import com.sweep.networkmonitor.ui.components.SweepCard
import com.sweep.networkmonitor.ui.theme.SweepCharcoal
import com.sweep.networkmonitor.ui.theme.SweepCharcoalMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The Devices screen lists discovered devices and provides access to
 * individual device details (Section 5.2 of the spec).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    devices: List<Device>,
    onDeviceClick: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.devices_title)) }) }
    ) { padding ->
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.devices_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SweepCharcoalMuted
                )
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(devices, key = { it.ipAddress }) { device ->
                DeviceRow(device = device, onClick = { onDeviceClick(device) })
            }
        }
    }
}

@Composable
private fun DeviceRow(device: Device, onClick: () -> Unit) {
    SweepCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(online = device.isOnline)
            Spacer(Modifier.height(0.dp))
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = SweepCharcoal,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = device.ipAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SweepCharcoalMuted
                )
                Text(
                    text = stringResource(
                        R.string.device_last_seen,
                        formatTime(device.lastSeenMillis)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = SweepCharcoalMuted
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
