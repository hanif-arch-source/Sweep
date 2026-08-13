package com.sweep.networkmonitor.ui.alerts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sweep.networkmonitor.R
import com.sweep.networkmonitor.data.model.Alert
import com.sweep.networkmonitor.data.model.AlertType
import com.sweep.networkmonitor.ui.components.SweepCard
import com.sweep.networkmonitor.ui.theme.SweepCharcoal
import com.sweep.networkmonitor.ui.theme.SweepCharcoalMuted
import com.sweep.networkmonitor.ui.theme.SweepRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lists alerts generated for the events in Section 12: new devices, weak
 * signal, disconnects, high traffic, and devices going offline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    alerts: List<Alert>,
    onAlertClick: (Alert) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.alerts_title)) }) }
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.alerts_empty),
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
            items(alerts, key = { it.id }) { alert ->
                AlertRow(alert = alert, onClick = { onAlertClick(alert) })
            }
        }
    }
}

@Composable
private fun AlertRow(alert: Alert, onClick: () -> Unit) {
    SweepCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconFor(alert.type),
                    contentDescription = null,
                    tint = SweepRed
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = SweepCharcoal,
                    fontWeight = if (alert.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SweepCharcoalMuted
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(alert.timestampMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = SweepCharcoalMuted
                )
            }
        }
    }
}

private fun iconFor(type: AlertType) = when (type) {
    AlertType.NEW_DEVICE -> Icons.Filled.Wifi
    AlertType.DEVICE_OFFLINE -> Icons.Filled.WifiOff
    AlertType.WEAK_SIGNAL -> Icons.Filled.SignalWifiOff
    AlertType.HIGH_USAGE -> Icons.Filled.Speed
    AlertType.NETWORK_DISCONNECTED -> Icons.Filled.NotificationsOff
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
