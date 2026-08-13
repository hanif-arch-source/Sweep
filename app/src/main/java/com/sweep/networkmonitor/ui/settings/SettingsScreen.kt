package com.sweep.networkmonitor.ui.settings

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sweep.networkmonitor.R
import com.sweep.networkmonitor.ui.components.SectionLabel
import com.sweep.networkmonitor.ui.components.SweepCard
import com.sweep.networkmonitor.ui.theme.SweepCharcoal
import com.sweep.networkmonitor.ui.theme.SweepCharcoalMuted
import com.sweep.networkmonitor.ui.theme.SweepRed
import com.sweep.networkmonitor.viewmodel.SweepSettings

/**
 * Lets users configure scan/update behavior and alert preferences
 * (Section 5.4 / FR-12 of the spec).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SweepSettings,
    onSettingsChange: ((SweepSettings) -> SweepSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SweepCard {
                    SwitchRow(
                        title = stringResource(R.string.settings_auto_update),
                        subtitle = stringResource(R.string.settings_auto_update_desc),
                        checked = settings.autoUpdateEnabled,
                        onCheckedChange = { checked -> onSettingsChange { it.copy(autoUpdateEnabled = checked) } }
                    )
                    Spacer(Modifier.height(16.dp))
                    SectionLabel(stringResource(R.string.settings_interval))
                    Text(
                        text = "${settings.monitoringIntervalSeconds}s",
                        style = MaterialTheme.typography.titleMedium,
                        color = SweepCharcoal
                    )
                    Slider(
                        value = settings.monitoringIntervalSeconds.toFloat(),
                        onValueChange = { value ->
                            onSettingsChange { it.copy(monitoringIntervalSeconds = value.toInt().coerceIn(2, 60)) }
                        },
                        valueRange = 2f..60f,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = SweepRed, activeTrackColor = SweepRed)
                    )
                    Spacer(Modifier.height(4.dp))
                    SwitchRow(
                        title = stringResource(R.string.settings_auto_scan),
                        subtitle = stringResource(R.string.settings_auto_scan_desc),
                        checked = settings.autoScanEnabled,
                        onCheckedChange = { checked -> onSettingsChange { it.copy(autoScanEnabled = checked) } }
                    )
                }
            }

            item {
                SweepCard {
                    SectionLabel(stringResource(R.string.settings_alert_thresholds))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${stringResource(R.string.settings_weak_signal_threshold)}: ${settings.weakSignalThresholdDbm} dBm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SweepCharcoalMuted
                    )
                    Slider(
                        value = settings.weakSignalThresholdDbm.toFloat(),
                        onValueChange = { value ->
                            onSettingsChange { it.copy(weakSignalThresholdDbm = value.toInt().coerceIn(-100, -40)) }
                        },
                        valueRange = -100f..-40f,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = SweepRed, activeTrackColor = SweepRed)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${stringResource(R.string.settings_high_traffic_threshold)}: ${settings.highTrafficThresholdMbps.toInt()} Mbps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SweepCharcoalMuted
                    )
                    Slider(
                        value = settings.highTrafficThresholdMbps.toFloat(),
                        onValueChange = { value ->
                            onSettingsChange { it.copy(highTrafficThresholdMbps = value.toDouble().coerceIn(5.0, 500.0)) }
                        },
                        valueRange = 5f..500f,
                        colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = SweepRed, activeTrackColor = SweepRed)
                    )
                }
            }

            item {
                SweepCard {
                    SectionLabel(stringResource(R.string.settings_notifications))
                    Spacer(Modifier.height(8.dp))
                    SwitchRow(
                        title = stringResource(R.string.settings_notify_new_device),
                        checked = settings.notifyNewDevice,
                        onCheckedChange = { checked -> onSettingsChange { it.copy(notifyNewDevice = checked) } }
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_notify_disconnect),
                        checked = settings.notifyDisconnect,
                        onCheckedChange = { checked -> onSettingsChange { it.copy(notifyDisconnect = checked) } }
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_notify_weak_signal),
                        checked = settings.notifyWeakSignal,
                        onCheckedChange = { checked -> onSettingsChange { it.copy(notifyWeakSignal = checked) } }
                    )
                    SwitchRow(
                        title = stringResource(R.string.settings_notify_high_traffic),
                        checked = settings.notifyHighTraffic,
                        onCheckedChange = { checked -> onSettingsChange { it.copy(notifyHighTraffic = checked) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = SweepCharcoal, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = SweepCharcoalMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SweepRed, checkedTrackColor = SweepRed.copy(alpha = 0.5f))
        )
    }
}
