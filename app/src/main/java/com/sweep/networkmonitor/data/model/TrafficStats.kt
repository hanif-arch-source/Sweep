package com.sweep.networkmonitor.data.model

/**
 * Stores upload/download byte counts and the calculated rates for the
 * Android device. Rates are approximate: (byteDifference / elapsedTime)
 * between two consecutive readings.
 */
data class TrafficStats(
    val downloadBytes: Long,
    val uploadBytes: Long,
    val downloadSpeedMbps: Double,
    val uploadSpeedMbps: Double,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    companion object {
        fun empty(): TrafficStats = TrafficStats(0L, 0L, 0.0, 0.0)
    }
}

/** One point on the rolling traffic graph. */
data class TrafficPoint(
    val timestampMillis: Long,
    val downloadSpeedMbps: Double,
    val uploadSpeedMbps: Double
)
