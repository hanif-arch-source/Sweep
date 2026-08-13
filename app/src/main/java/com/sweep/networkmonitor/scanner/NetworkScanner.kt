package com.sweep.networkmonitor.scanner

import android.content.Context
import android.net.wifi.WifiManager
import com.sweep.networkmonitor.data.model.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Discovers devices reachable on the local network. The scanner first
 * determines the device's local /24 subnet, then performs a controlled,
 * concurrent reachability check across host addresses in the background.
 *
 * This is a best-effort discovery mechanism: it relies on ICMP/TCP
 * reachability and ARP-cache hostname/MAC resolution, which are subject to
 * Android permissions, OEM restrictions, and how the target devices respond.
 */
class NetworkScanner(private val context: Context) {

    /**
     * Scans the local subnet and returns the devices that responded.
     * Runs entirely off the calling thread; safe to call from a ViewModel.
     */
    suspend fun scan(): List<Device> = withContext(Dispatchers.IO) {
        val subnetPrefix = localSubnetPrefix() ?: return@withContext emptyList()

        // Probe all 254 host addresses concurrently, but cap concurrency so
        // we don't overwhelm the local network or the device's socket table.
        val hostRange = 1..254
        val semaphoreBatchSize = 32

        val results = mutableListOf<Device>()
        hostRange.chunked(semaphoreBatchSize).forEach { batch ->
            val batchResults = batch.map { host ->
                async { probeHost("$subnetPrefix.$host") }
            }.awaitAll()
            results.addAll(batchResults.filterNotNull())
        }

        results
    }

    private suspend fun probeHost(ip: String): Device? = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(ip)
            val reachable = address.isReachable(REACHABILITY_TIMEOUT_MS)
            if (!reachable) return@withContext null

            val hostname = address.canonicalHostName
                .takeIf { it != ip } // canonicalHostName falls back to the IP when unresolved

            Device(
                ipAddress = ip,
                macAddress = readArpMacAddress(ip),
                hostname = hostname,
                manufacturer = null, // requires an OUI lookup table/service; left for a future enhancement
                isOnline = true
            )
        } catch (e: Exception) {
            null
        }
    }

    /** Determines this device's subnet as "a.b.c" from its own IP address. */
    private fun localSubnetPrefix(): String? {
        @Suppress("DEPRECATION")
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val ipInt = wifiManager?.connectionInfo?.ipAddress
        if (ipInt == null || ipInt == 0) return localSubnetPrefixFromInterfaces()

        val a = ipInt and 0xff
        val b = ipInt shr 8 and 0xff
        val c = ipInt shr 16 and 0xff
        return "$a.$b.$c"
    }

    /** Fallback: inspect network interfaces directly (e.g. non-Wi-Fi networks). */
    private fun localSubnetPrefixFromInterfaces(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .filter { !it.isLoopbackAddress && it.hostAddress?.contains(":") == false }
                .map { it.hostAddress!! }
                .firstOrNull()
                ?.substringBeforeLast(".")
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Best-effort MAC address lookup via the kernel ARP cache
     * (/proc/net/arp). Not guaranteed to be populated or accessible on
     * every Android version/OEM build; returns null when unavailable.
     */
    private fun readArpMacAddress(ip: String): String? {
        return try {
            java.io.File("/proc/net/arp").readLines()
                .drop(1) // header row
                .map { it.trim().split(Regex("\\s+")) }
                .firstOrNull { it.size >= 4 && it[0] == ip }
                ?.get(3)
                ?.takeIf { it != "00:00:00:00:00:00" }
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        const val REACHABILITY_TIMEOUT_MS = 400
    }
}
