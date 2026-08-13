package com.sweep.networkmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweep.networkmonitor.data.model.Alert
import com.sweep.networkmonitor.data.model.AlertType
import com.sweep.networkmonitor.data.model.Device
import com.sweep.networkmonitor.data.model.Network
import com.sweep.networkmonitor.data.model.NetworkEvent
import com.sweep.networkmonitor.data.model.TrafficPoint
import com.sweep.networkmonitor.data.model.TrafficStats
import com.sweep.networkmonitor.data.repository.NetworkRepository
import com.sweep.networkmonitor.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** User-configurable monitoring/scan/alert preferences (Settings screen). */
data class SweepSettings(
    val autoUpdateEnabled: Boolean = true,
    val monitoringIntervalSeconds: Int = 5,
    val autoScanEnabled: Boolean = false,
    val weakSignalThresholdDbm: Int = -75,
    val highTrafficThresholdMbps: Double = 50.0,
    val notifyNewDevice: Boolean = true,
    val notifyDisconnect: Boolean = true,
    val notifyWeakSignal: Boolean = true,
    val notifyHighTraffic: Boolean = true
)

/** Aggregate UI state consumed by every Sweep screen. */
data class SweepUiState(
    val network: Network = Network.disconnected(),
    val traffic: TrafficStats = TrafficStats.empty(),
    val trafficHistory: List<TrafficPoint> = emptyList(),
    val devices: List<Device> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val settings: SweepSettings = SweepSettings(),
    val isScanning: Boolean = false,
    val isRefreshing: Boolean = false
)

/**
 * Coordinates application state and user actions for the UI. Talks only to
 * [NetworkRepository] and [NotificationHelper] — no direct Android
 * networking calls live here, keeping the ViewModel testable.
 */
class NetworkViewModel(
    private val repository: NetworkRepository,
    private val notificationHelper: NotificationHelper?
) : ViewModel() {

    private val _uiState = MutableStateFlow(SweepUiState())
    val uiState: StateFlow<SweepUiState> = _uiState.asStateFlow()

    private var monitoringJob: Job? = null
    private var wasConnected: Boolean = false

    init {
        observeConnectivity()
        startMonitoringLoop()
    }

    private fun observeConnectivity() {
        repository.observeNetwork()
            .onEach { network -> handleNetworkUpdate(network) }
            .launchIn(viewModelScope)
    }

    /** Coroutine-based monitoring loop, per Section 11 of the project spec:
     *  while monitoring: read network state -> read traffic -> update state -> wait.
     *  Restarted automatically whenever the interval setting changes.
     */
    private fun startMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (true) {
                val settings = _uiState.value.settings
                if (settings.autoUpdateEnabled) {
                    refresh(fromAutoLoop = true)
                    if (settings.autoScanEnabled) {
                        scanNetwork()
                    }
                }
                delay(settings.monitoringIntervalSeconds.coerceAtLeast(1) * 1000L)
            }
        }
    }

    /** Manual refresh, triggered by the Dashboard's Refresh button. */
    fun refresh() = viewModelScope.launch { refresh(fromAutoLoop = false) }

    private suspend fun refresh(fromAutoLoop: Boolean) {
        if (!fromAutoLoop) _uiState.update { it.copy(isRefreshing = true) }

        val network = repository.readNetwork()
        val traffic = repository.readTraffic()
        handleNetworkUpdate(network)
        handleTrafficUpdate(traffic)

        if (!fromAutoLoop) _uiState.update { it.copy(isRefreshing = false) }
    }

    /** Manual scan, triggered by the Dashboard's "Scan Network" button. */
    fun scanNetwork() {
        if (_uiState.value.isScanning) return
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }

            val previousDevices = _uiState.value.devices
            val discovered = repository.scanDevices()
            val events = repository.diffDevices(previousDevices, discovered)

            _uiState.update { it.copy(devices = discovered, isScanning = false) }
            events.forEach(::handleEvent)

            // Take a fresh network + traffic reading right after the scan
            // finishes, so real upload/download digits are visible
            // immediately instead of waiting for the next monitoring tick.
            val network = repository.readNetwork()
            val traffic = repository.readTraffic()
            handleNetworkUpdate(network)
            handleTrafficUpdate(traffic)
        }
    }

    private fun handleNetworkUpdate(network: Network) {
        val settings = _uiState.value.settings
        _uiState.update { it.copy(network = network) }

        if (wasConnected && !network.isConnected) {
            handleEvent(NetworkEvent.NetworkDisconnected)
        } else if (!wasConnected && network.isConnected) {
            handleEvent(NetworkEvent.NetworkConnected)
        }
        wasConnected = network.isConnected

        if (network.isConnected && network.signalStrength != 0 &&
            network.signalStrength <= settings.weakSignalThresholdDbm
        ) {
            handleEvent(NetworkEvent.WeakSignal(network.signalStrength))
        }
    }

    private fun handleTrafficUpdate(traffic: TrafficStats) {
        val settings = _uiState.value.settings
        _uiState.update { state ->
            val history = (state.trafficHistory + TrafficPoint(
                timestampMillis = traffic.timestampMillis,
                downloadSpeedMbps = traffic.downloadSpeedMbps,
                uploadSpeedMbps = traffic.uploadSpeedMbps
            )).takeLast(MAX_TRAFFIC_POINTS) // rolling window so the graph stays responsive

            state.copy(traffic = traffic, trafficHistory = history)
        }

        val combinedSpeed = traffic.downloadSpeedMbps + traffic.uploadSpeedMbps
        if (combinedSpeed >= settings.highTrafficThresholdMbps) {
            handleEvent(NetworkEvent.HighTraffic(combinedSpeed))
        }
    }

    private fun handleEvent(event: NetworkEvent) {
        val settings = _uiState.value.settings
        val alert = when (event) {
            is NetworkEvent.NetworkConnected -> null // informational only, no alert needed
            is NetworkEvent.NetworkDisconnected -> if (settings.notifyDisconnect) {
                buildAlert(AlertType.NETWORK_DISCONNECTED, "Network disconnected", "This device lost its network connection.")
            } else null

            is NetworkEvent.NewDevice -> if (settings.notifyNewDevice) {
                buildAlert(
                    AlertType.NEW_DEVICE,
                    "New device detected",
                    "${event.device.displayName} (${event.device.ipAddress}) joined the network."
                )
            } else null

            is NetworkEvent.DeviceOffline -> buildAlert(
                AlertType.DEVICE_OFFLINE,
                "Device offline",
                "${event.device.displayName} (${event.device.ipAddress}) is no longer reachable."
            )

            is NetworkEvent.WeakSignal -> if (settings.notifyWeakSignal) {
                buildAlert(AlertType.WEAK_SIGNAL, "Weak Wi-Fi signal", "Signal strength dropped to ${event.strength} dBm.")
            } else null

            is NetworkEvent.HighTraffic -> if (settings.notifyHighTraffic) {
                buildAlert(
                    AlertType.HIGH_USAGE,
                    "High traffic",
                    "Combined traffic reached ${"%.1f".format(event.speedMbps)} Mbps."
                )
            } else null
        } ?: return

        _uiState.update { it.copy(alerts = listOf(alert) + it.alerts) }
        notificationHelper?.notify(alert)
    }

    private fun buildAlert(type: AlertType, title: String, message: String) = Alert(
        id = UUID.randomUUID().toString(),
        type = type,
        title = title,
        message = message
    )

    fun updateSettings(transform: (SweepSettings) -> SweepSettings) {
        _uiState.update { it.copy(settings = transform(it.settings)) }
        startMonitoringLoop() // interval/auto-update flags may have changed
    }

    fun markAlertRead(alertId: String) {
        _uiState.update { state ->
            state.copy(alerts = state.alerts.map { if (it.id == alertId) it.copy(isRead = true) else it })
        }
    }

    fun deviceById(ipAddress: String): Device? =
        _uiState.value.devices.firstOrNull { it.ipAddress == ipAddress }

    private companion object {
        const val MAX_TRAFFIC_POINTS = 30
    }
}
