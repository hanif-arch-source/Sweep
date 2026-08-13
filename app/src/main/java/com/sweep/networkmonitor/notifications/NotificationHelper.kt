package com.sweep.networkmonitor.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.sweep.networkmonitor.R
import com.sweep.networkmonitor.data.model.Alert
import com.sweep.networkmonitor.data.model.AlertType

/**
 * Responsible for Android notifications: creating the Sweep alert channel
 * and posting notifications for user-facing alerts.
 */
class NotificationHelper(private val context: Context) {

    init {
        createChannelIfNeeded()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
        }
        manager.createNotificationChannel(channel)
    }

    /** Posts a notification for the given alert, if notification permission is granted. */
    fun notify(alert: Alert) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_wifi)
            .setColor(0xFFE53935.toInt())
            .setContentTitle(alert.title)
            .setContentText(alert.message)
            .setPriority(priorityFor(alert.type))
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alert.id.hashCode(), notification)
    }

    private fun priorityFor(type: AlertType): Int = when (type) {
        AlertType.NETWORK_DISCONNECTED -> NotificationCompat.PRIORITY_HIGH
        AlertType.WEAK_SIGNAL -> NotificationCompat.PRIORITY_DEFAULT
        AlertType.NEW_DEVICE -> NotificationCompat.PRIORITY_DEFAULT
        AlertType.DEVICE_OFFLINE -> NotificationCompat.PRIORITY_LOW
        AlertType.HIGH_USAGE -> NotificationCompat.PRIORITY_DEFAULT
    }

    private companion object {
        const val CHANNEL_ID = "sweep_alerts"
    }
}
