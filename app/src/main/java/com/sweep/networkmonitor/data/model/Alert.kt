package com.sweep.networkmonitor.data.model

enum class AlertType {
    NEW_DEVICE,
    DEVICE_OFFLINE,
    WEAK_SIGNAL,
    HIGH_USAGE,
    NETWORK_DISCONNECTED
}

/**
 * A user-visible network warning or event, shown on the Alerts screen and,
 * depending on settings, as an Android notification.
 */
data class Alert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
