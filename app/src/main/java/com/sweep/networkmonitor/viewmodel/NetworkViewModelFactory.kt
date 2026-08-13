package com.sweep.networkmonitor.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sweep.networkmonitor.data.repository.NetworkRepository
import com.sweep.networkmonitor.monitoring.NetworkMonitor
import com.sweep.networkmonitor.monitoring.TrafficMonitor
import com.sweep.networkmonitor.notifications.NotificationHelper
import com.sweep.networkmonitor.scanner.NetworkScanner

/**
 * Wires up [NetworkRepository] and [NotificationHelper] from an application
 * [Context] and hands the result to [NetworkViewModel]. Keeping this
 * assembly in one place is what lets the ViewModel itself stay
 * dependency-injection-framework-free.
 */
class NetworkViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = NetworkRepository(
            networkMonitor = NetworkMonitor(appContext),
            trafficMonitor = TrafficMonitor(),
            networkScanner = NetworkScanner(appContext)
        )
        val notificationHelper = NotificationHelper(appContext)

        @Suppress("UNCHECKED_CAST")
        return NetworkViewModel(repository, notificationHelper) as T
    }
}
