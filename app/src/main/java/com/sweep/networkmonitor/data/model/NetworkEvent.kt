package com.sweep.networkmonitor.data.model

/**
 * Discrete events produced by the monitoring/scanning layer. Events allow
 * Sweep to react to changes rather than forcing every component to
 * continuously poll the same information.
 */
sealed class NetworkEvent {
    data object NetworkConnected : NetworkEvent()
    data object NetworkDisconnected : NetworkEvent()
    data class NewDevice(val device: Device) : NetworkEvent()
    data class DeviceOffline(val device: Device) : NetworkEvent()
    data class WeakSignal(val strength: Int) : NetworkEvent()
    data class HighTraffic(val speedMbps: Double) : NetworkEvent()
}
