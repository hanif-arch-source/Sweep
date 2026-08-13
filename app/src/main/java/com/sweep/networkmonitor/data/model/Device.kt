package com.sweep.networkmonitor.data.model

/**
 * Represents a device discovered on the local network during a scan.
 */
data class Device(
    val ipAddress: String,
    val macAddress: String?,
    val hostname: String?,
    val manufacturer: String?,
    val isOnline: Boolean,
    val lastSeenMillis: Long = System.currentTimeMillis()
) {
    /** Best-effort display name: hostname, then manufacturer, then raw IP. */
    val displayName: String
        get() = hostname?.takeIf { it.isNotBlank() }
            ?: manufacturer?.takeIf { it.isNotBlank() }
            ?: ipAddress
}
