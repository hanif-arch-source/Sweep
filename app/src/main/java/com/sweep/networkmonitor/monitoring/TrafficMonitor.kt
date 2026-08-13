package com.sweep.networkmonitor.monitoring

import android.net.TrafficStats as AndroidTrafficStats
import com.sweep.networkmonitor.data.model.TrafficStats

/**
 * Responsible for collecting traffic statistics for the device's active
 * network interface. A rate is calculated from two readings: the
 * difference in transferred bytes divided by the elapsed time.
 *
 *   byteDifference = currentBytes - previousBytes
 *   speed = byteDifference / elapsedTime
 *
 * This deliberately reads device-wide totals (TrafficStats.getTotalRxBytes
 * / getTotalTxBytes), not just this app's own usage — Sweep is meant to
 * show what the whole device is doing on the network, and an app that is
 * mostly idle in the background would otherwise always report ~0 Mbps.
 */
class TrafficMonitor {

    private var previousRxBytes: Long? = null
    private var previousTxBytes: Long? = null
    private var previousTimestamp: Long = 0L

    /**
     * Reads current totals and returns a [TrafficStats] snapshot with
     * calculated Mbps rates since the previous call. The first call
     * establishes a baseline and reports zero rates; every call after
     * that reflects real, current throughput.
     */
    fun readCurrentStats(): TrafficStats {
        val rxBytes = deviceTotalBytes(isReceive = true)
        val txBytes = deviceTotalBytes(isReceive = false)
        val now = System.currentTimeMillis()

        val prevRx = previousRxBytes
        val prevTx = previousTxBytes
        val prevTime = previousTimestamp

        val stats = if (prevRx == null || prevTx == null || prevTime == 0L) {
            TrafficStats(
                downloadBytes = rxBytes,
                uploadBytes = txBytes,
                downloadSpeedMbps = 0.0,
                uploadSpeedMbps = 0.0,
                timestampMillis = now
            )
        } else {
            val elapsedSeconds = ((now - prevTime).coerceAtLeast(1)) / 1000.0
            // Counters can reset on some devices (e.g. after a reboot or
            // when switching networks); treat a negative delta as "no
            // data yet" rather than letting it produce a bogus negative
            // rate.
            val rxDelta = (rxBytes - prevRx).coerceAtLeast(0)
            val txDelta = (txBytes - prevTx).coerceAtLeast(0)

            TrafficStats(
                downloadBytes = rxBytes,
                uploadBytes = txBytes,
                downloadSpeedMbps = bytesToMbps(rxDelta, elapsedSeconds),
                uploadSpeedMbps = bytesToMbps(txDelta, elapsedSeconds),
                timestampMillis = now
            )
        }

        previousRxBytes = rxBytes
        previousTxBytes = txBytes
        previousTimestamp = now

        return stats
    }

    /** Resets the baseline so the next reading starts a fresh measurement window. */
    fun reset() {
        previousRxBytes = null
        previousTxBytes = null
        previousTimestamp = 0L
    }

    private fun deviceTotalBytes(isReceive: Boolean): Long {
        val value = if (isReceive) {
            AndroidTrafficStats.getTotalRxBytes()
        } else {
            AndroidTrafficStats.getTotalTxBytes()
        }
        // UNSUPPORTED (-1) on very rare devices without traffic stats support.
        return if (value == AndroidTrafficStats.UNSUPPORTED.toLong()) 0L else value
    }

    private fun bytesToMbps(bytes: Long, elapsedSeconds: Double): Double {
        if (elapsedSeconds <= 0) return 0.0
        // bytes -> bits -> megabits per second
        return (bytes * 8.0) / elapsedSeconds / 1_000_000.0
    }
}
