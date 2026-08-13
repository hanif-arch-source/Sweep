package com.sweep.networkmonitor.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.sweep.networkmonitor.data.model.TrafficPoint
import com.sweep.networkmonitor.ui.theme.SweepBorder
import com.sweep.networkmonitor.ui.theme.SweepCharcoalMuted
import com.sweep.networkmonitor.ui.theme.SweepRed

/**
 * Live upload/download graph. Renders a rolling window of [TrafficPoint]s as
 * two simple line traces (download in red, upload in muted charcoal) scaled
 * to the largest value currently on screen.
 */
@Composable
fun TrafficGraph(
    points: List<TrafficPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        // Baseline grid
        drawLine(
            color = SweepBorder,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )

        if (points.size < 2) return@Canvas

        val maxValue = (points.maxOf { maxOf(it.downloadSpeedMbps, it.uploadSpeedMbps) })
            .coerceAtLeast(1.0)

        fun pathFor(values: List<Double>): List<Offset> {
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            return values.mapIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value / maxValue * size.height).toFloat()
                Offset(x, y)
            }
        }

        val downloadOffsets = pathFor(points.map { it.downloadSpeedMbps })
        val uploadOffsets = pathFor(points.map { it.uploadSpeedMbps })

        drawPolyline(downloadOffsets, SweepRed)
        drawPolyline(uploadOffsets, SweepCharcoalMuted)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPolyline(
    points: List<Offset>,
    color: Color
) {
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}
