package com.sweep.networkmonitor.data.model

/**
 * Represents the current network connection and its properties, as reported
 * by the Android networking APIs available to this device.
 */
data class Network(
    val ssid: String?,
    val ipAddress: String?,
    val gateway: String?,
    val signalStrength: Int,   // dBm, e.g. -48. 0 when unknown.
    val linkSpeed: Int,        // Mbps. 0 when unknown.
    val isConnected: Boolean
) {
    companion object {
        /** Default state before the first read, or when there is no active connection. */
        fun disconnected(): Network = Network(
            ssid = null,
            ipAddress = null,
            gateway = null,
            signalStrength = 0,
            linkSpeed = 0,
            isConnected = false
        )
    }

    /**
     * A coarse 0-4 bar rating derived from signal strength, used for the
     * dashboard signal bars. Android Wi-Fi RSSI generally ranges from
     * about -100 dBm (unusable) to -30 dBm (excellent).
     */
    val signalBars: Int
        get() = when {
            !isConnected -> 0
            signalStrength >= -50 -> 4
            signalStrength >= -60 -> 3
            signalStrength >= -70 -> 2
            signalStrength >= -80 -> 1
            else -> 0
        }
}
