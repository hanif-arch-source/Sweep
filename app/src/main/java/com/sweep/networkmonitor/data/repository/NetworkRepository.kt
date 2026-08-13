package com.sweep.networkmonitor.data.repository

import com.sweep.networkmonitor.data.model.Device
import com.sweep.networkmonitor.data.model.Network
import com.sweep.networkmonitor.data.model.NetworkEvent
import com.sweep.networkmonitor.data.model.TrafficStats
import com.sweep.networkmonitor.monitoring.NetworkMonitor
import com.sweep.networkmonitor.monitoring.TrafficMonitor
import com.sweep.networkmonitor.scanner.NetworkScanner
import kotlinx.coroutines.flow.Flow

/**
 * Sits between the Android networking APIs (via [NetworkMonitor],
 * [TrafficMonitor], [NetworkScanner]) and the ViewModel layer. Keeps the
 * Compose UI free of direct Android networking calls and makes the app
 * easier to test.
 */
class NetworkRepository(
    private val networkMonitor: NetworkMonitor,
    private val trafficMonitor: TrafficMonitor,
    private val networkScanner: NetworkScanner
) {

    /** Live network connection state; emits on every connectivity change. */
    fun observeNetwork(): Flow<Network> = networkMonitor.observe()

    /** One-shot read of the current network state (used for manual refresh). */
    fun readNetwork(): Network = networkMonitor.readCurrentState()

    /** Current traffic snapshot, with rates computed since the previous call. */
    fun readTraffic(): TrafficStats = trafficMonitor.readCurrentStats()

    /** Runs a local-network device scan off the main thread. */
    suspend fun scanDevices(): List<Device> = networkScanner.scan()

    /**
     * Compares a new scan against the previous device list and derives the
     * events that should drive alerts/notifications (new devices, devices
     * that dropped off the network).
     */
    fun diffDevices(previous: List<Device>, current: List<Device>): List<NetworkEvent> {
        val events = mutableListOf<NetworkEvent>()

        val previousByIp = previous.associateBy { it.ipAddress }
        val currentByIp = current.associateBy { it.ipAddress }

        current.forEach { device ->
            if (!previousByIp.containsKey(device.ipAddress)) {
                events += NetworkEvent.NewDevice(device)
            }
        }

        previous.forEach { device ->
            if (!currentByIp.containsKey(device.ipAddress)) {
                events += NetworkEvent.DeviceOffline(device.copy(isOnline = false))
            }
        }

        return events
    }
}
