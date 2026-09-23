package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.model.TimeData
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClockView(
    timeData: TimeData,
    modifier: Modifier = Modifier
) {
    val dialColor = MaterialTheme.colorScheme.surfaceVariant
    val hourHandColor = MaterialTheme.colorScheme.onSurface
    val minuteHandColor = MaterialTheme.colorScheme.primary
    val secondHandColor = MaterialTheme.colorScheme.error
    val markerColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.size(100.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Draw clock dial background
        drawCircle(
            color = dialColor,
            radius = radius,
            center = center
        )

        // Draw 12 hour ticks
        for (i in 0 until 12) {
            val angle = (i * 30.0) * (PI / 180.0)
            val isMajor = i % 3 == 0
            val tickLength = if (isMajor) radius * 0.18f else radius * 0.10f
            val startR = radius - tickLength - 4f
            val endR = radius - 4f

            val startX = center.x + (startR * sin(angle)).toFloat()
            val startY = center.y - (startR * cos(angle)).toFloat()
            val endX = center.x + (endR * sin(angle)).toFloat()
            val endY = center.y - (endR * cos(angle)).toFloat()

            drawLine(
                color = markerColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 2.5f else 1.2f,
                cap = StrokeCap.Round
            )
        }

        // Calculate hand angles
        // Hour hand: 360 deg in 12 hours = 30 deg/hr + 0.5 deg/min
        val hourAngle = ((timeData.hour12 % 12 + timeData.minute / 60.0) * 30.0) * (PI / 180.0)
        // Minute hand: 360 deg in 60 mins = 6 deg/min + 0.1 deg/sec
        val minuteAngle = ((timeData.minute + timeData.second / 60.0) * 6.0) * (PI / 180.0)
        // Second hand: 360 deg in 60 secs = 6 deg/sec
        val secondAngle = (timeData.second * 6.0) * (PI / 180.0)

        // Draw Hour Hand
        val hourLength = radius * 0.52f
        val hourEnd = Offset(
            center.x + (hourLength * sin(hourAngle)).toFloat(),
            center.y - (hourLength * cos(hourAngle)).toFloat()
        )
        drawLine(
            color = hourHandColor,
            start = center,
            end = hourEnd,
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        // Draw Minute Hand
        val minuteLength = radius * 0.72f
        val minuteEnd = Offset(
            center.x + (minuteLength * sin(minuteAngle)).toFloat(),
            center.y - (minuteLength * cos(minuteAngle)).toFloat()
        )
        drawLine(
            color = minuteHandColor,
            start = center,
            end = minuteEnd,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Draw Second Hand
        val secondLength = radius * 0.85f
        val secondTail = radius * 0.18f
        val secondEnd = Offset(
            center.x + (secondLength * sin(secondAngle)).toFloat(),
            center.y - (secondLength * cos(secondAngle)).toFloat()
        )
        val secondTailEnd = Offset(
            center.x - (secondTail * sin(secondAngle)).toFloat(),
            center.y + (secondTail * cos(secondAngle)).toFloat()
        )
        drawLine(
            color = secondHandColor,
            start = secondTailEnd,
            end = secondEnd,
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // Center pivot dot
        drawCircle(
            color = secondHandColor,
            radius = 3.5f,
            center = center
        )
    }
}
