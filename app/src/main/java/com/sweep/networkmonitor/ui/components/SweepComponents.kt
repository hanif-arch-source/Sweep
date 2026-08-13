package com.sweep.networkmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sweep.networkmonitor.ui.theme.SweepBorder
import com.sweep.networkmonitor.ui.theme.SweepCharcoal
import com.sweep.networkmonitor.ui.theme.SweepCharcoalMuted
import com.sweep.networkmonitor.ui.theme.SweepOnlineGreen
import com.sweep.networkmonitor.ui.theme.SweepRed
import com.sweep.networkmonitor.ui.theme.SweepSurface

/** A clean white card with a subtle border, used across every Sweep screen. */
@Composable
fun SweepCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScopeAlias.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SweepSurface)
            .border(1.dp, SweepBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

// Alias to avoid importing ColumnScope directly in call sites' signatures.
typealias ColumnScopeAlias = androidx.compose.foundation.layout.ColumnScope

/** Small uppercase section label, e.g. "NETWORK TRAFFIC". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = SweepCharcoalMuted,
        modifier = modifier
    )
}

/** Green/red status dot with label, e.g. "● CONNECTED". */
@Composable
fun StatusPill(isPositive: Boolean, positiveText: String, negativeText: String) {
    val color = if (isPositive) SweepOnlineGreen else SweepRed
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Row(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = if (isPositive) positiveText else negativeText,
                style = MaterialTheme.typography.titleMedium,
                color = SweepCharcoal,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 4-bar Wi-Fi signal strength indicator, 0-4 bars filled. */
@Composable
fun SignalBars(bars: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        for (i in 1..4) {
            val filled = i <= bars
            Box(
                modifier = Modifier
                    .padding(end = 3.dp)
                    .width(6.dp)
                    .height((10 + i * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (filled) SweepRed else SweepBorder)
            )
        }
    }
}

/** A labeled value pair, e.g. "Local IP" / "192.168.1.25". */
@Composable
fun MetricRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = SweepCharcoalMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = SweepCharcoal, fontWeight = FontWeight.Medium)
    }
}

/** Round colored dot used for online/offline status in lists. */
@Composable
fun StatusDot(online: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (online) SweepOnlineGreen else Color(0xFFBDBDBD))
    )
}
