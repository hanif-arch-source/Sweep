package com.sweep.networkmonitor.monitoring

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network as AndroidNetwork
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.sweep.networkmonitor.data.model.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.InetAddress

/**
 * Responsible for reading current network state from Android APIs
 * (ConnectivityManager / WifiManager) and turning it into a [Network]
 * model. Emits a new value whenever connectivity or Wi-Fi info changes,
 * and can also be polled with [readCurrentState] for manual refreshes.
 */
class NetworkMonitor(private val context: Context) {

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager: WifiManager
        get() = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /** One-shot read of the current network state. Safe to call repeatedly. */
    fun readCurrentState(): Network {
        val activeNetwork: AndroidNetwork? = connectivityManager.activeNetwork
        val capabilities: NetworkCapabilities? =
            activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!isConnected) return Network.disconnected()

        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        if (!isWifi) {
            // Connected, but not over Wi-Fi (e.g. cellular). Sweep still
            // reports the connection but without SSID/signal information.
            return Network(
                ssid = null,
                ipAddress = localIpAddress(),
                gateway = null,
                signalStrength = 0,
                linkSpeed = 0,
                isConnected = true
            )
        }

        @Suppress("DEPRECATION")
        val wifiInfo: WifiInfo? = wifiManager.connectionInfo

        val rawSsid = wifiInfo?.ssid
        val ssid = rawSsid
            ?.removePrefix("\"")
            ?.removeSuffix("\"")
            ?.takeIf { it.isNotBlank() && it != WifiManager.UNKNOWN_SSID }

        val signalStrength = wifiInfo?.rssi ?: 0
        val linkSpeed = wifiInfo?.linkSpeed ?: 0 // Mbps

        return Network(
            ssid = ssid,
            ipAddress = localIpAddress(),
            gateway = gatewayAddress(),
            signalStrength = signalStrength,
            linkSpeed = linkSpeed,
            isConnected = true
        )
    }

    /** Emits network state whenever Android reports a connectivity change. */
    fun observe(): Flow<Network> = callbackFlow {
        trySend(readCurrentState())

        val request = android.net.NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: AndroidNetwork) {
                trySend(readCurrentState())
            }

            override fun onLost(network: AndroidNetwork) {
                trySend(Network.disconnected())
            }

            override fun onCapabilitiesChanged(
                network: AndroidNetwork,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(readCurrentState())
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    private fun localIpAddress(): String? {
        @Suppress("DEPRECATION")
        val ipInt = wifiManager.connectionInfo?.ipAddress ?: return null
        if (ipInt == 0) return null
        val bytes = byteArrayOf(
            (ipInt and 0xff).toByte(),
            (ipInt shr 8 and 0xff).toByte(),
            (ipInt shr 16 and 0xff).toByte(),
            (ipInt shr 24 and 0xff).toByte()
        )
        return try {
            InetAddress.getByAddress(bytes).hostAddress
        } catch (e: Exception) {
            null
        }
    }

    private fun gatewayAddress(): String? {
        @Suppress("DEPRECATION")
        val dhcpInfo = wifiManager.dhcpInfo ?: return null
        val gatewayInt = dhcpInfo.gateway
        if (gatewayInt == 0) return null
        val bytes = byteArrayOf(
            (gatewayInt and 0xff).toByte(),
            (gatewayInt shr 8 and 0xff).toByte(),
            (gatewayInt shr 16 and 0xff).toByte(),
            (gatewayInt shr 24 and 0xff).toByte()
        )
        return try {
            InetAddress.getByAddress(bytes).hostAddress
        } catch (e: Exception) {
            null
        }
    }
}
