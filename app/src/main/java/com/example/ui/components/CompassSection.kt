package com.example.ui.components

import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CompassHelper
import com.example.util.CompassState
import com.example.util.DateFormatter
import com.example.util.rememberCompassState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompassSection(
    isBengali: Boolean,
    modifier: Modifier = Modifier,
    compassStateOverride: CompassState? = null
) {
    val liveCompassState = if (compassStateOverride == null) rememberCompassState() else compassStateOverride
    var targetBearing by rememberSaveable { mutableStateOf<Float?>(null) }
    var showCalibrationTips by rememberSaveable { mutableStateOf(false) }

    // Continuous unwrapped angle for smooth 359° <-> 1° animations without spinning 358°
    var continuousAzimuth by remember { mutableFloatStateOf(liveCompassState.azimuthDegrees) }
    LaunchedEffect(liveCompassState.azimuthDegrees) {
        val currentWrapped = CompassHelper.normalizeDegrees(continuousAzimuth)
        val delta = CompassHelper.shortestAngleDelta(currentWrapped, liveCompassState.azimuthDegrees)
        continuousAzimuth += delta
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = continuousAzimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "compass_azimuth_anim"
    )

    val headingInt = CompassHelper.normalizeDegrees(liveCompassState.azimuthDegrees).roundToInt() % 360
    val headingStr = DateFormatter.formatNumber(headingInt, isBengali)
    val shortDir = CompassHelper.getShortCardinalDirection(liveCompassState.azimuthDegrees, isBengali)
    val fullDir = CompassHelper.getFullCardinalDirection(liveCompassState.azimuthDegrees, isBengali)
    val accuracyLabel = CompassHelper.getAccuracyLabel(liveCompassState.accuracy, isBengali)

    val isHighOrMedAccuracy = liveCompassState.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH ||
        liveCompassState.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM

    val isLevel = abs(liveCompassState.pitchDegrees) <= 4f && abs(liveCompassState.rollDegrees) <= 4f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compass_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBengali) "ডিজিটাল কম্পাস" else "Digital Compass",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBengali) "চৌম্বকীয় উত্তর দিক নির্দেশক" else "Magnetic North Heading",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Accuracy / Calibration Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (!liveCompassState.hasCompassSensor) {
                        MaterialTheme.colorScheme.errorContainer
                    } else if (isHighOrMedAccuracy) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCalibrationTips = !showCalibrationTips }
                        .testTag("compass_accuracy_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (liveCompassState.hasCompassSensor) Icons.Default.CompassCalibration else Icons.Default.SensorsOff,
                            contentDescription = accuracyLabel,
                            tint = if (!liveCompassState.hasCompassSensor || !isHighOrMedAccuracy) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (liveCompassState.hasCompassSensor) accuracyLabel else (if (isBengali) "সেন্সর নেই" else "No Sensor"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!liveCompassState.hasCompassSensor || !isHighOrMedAccuracy) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!liveCompassState.hasCompassSensor) {
                CompassUnavailableBanner(isBengali = isBengali)
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val useWideLayout = maxWidth >= 680.dp
                    val dialSize = if (useWideLayout) 280.dp else maxWidth.coerceAtMost(300.dp)

                    if (useWideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CompassDialCanvas(
                                    animatedAzimuth = animatedAzimuth,
                                    pitchDegrees = liveCompassState.pitchDegrees,
                                    rollDegrees = liveCompassState.rollDegrees,
                                    targetBearing = targetBearing,
                                    isLevel = isLevel,
                                    isBengali = isBengali,
                                    modifier = Modifier
                                        .size(dialSize)
                                        .testTag("compass_dial")
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HeadingReadoutBanner(
                                    headingStr = headingStr,
                                    shortDir = shortDir,
                                    fullDir = fullDir,
                                    targetBearing = targetBearing,
                                    currentAzimuth = liveCompassState.azimuthDegrees,
                                    isBengali = isBengali
                                )

                                TargetBearingControls(
                                    currentAzimuth = liveCompassState.azimuthDegrees,
                                    targetBearing = targetBearing,
                                    onSetTarget = { targetBearing = it },
                                    onToggleCalibration = { showCalibrationTips = !showCalibrationTips },
                                    showCalibrationTips = showCalibrationTips,
                                    isBengali = isBengali
                                )

                                CompassTelemetryRow(
                                    compassState = liveCompassState,
                                    isLevel = isLevel,
                                    isBengali = isBengali
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HeadingReadoutBanner(
                                headingStr = headingStr,
                                shortDir = shortDir,
                                fullDir = fullDir,
                                targetBearing = targetBearing,
                                currentAzimuth = liveCompassState.azimuthDegrees,
                                isBengali = isBengali
                            )

                            CompassDialCanvas(
                                animatedAzimuth = animatedAzimuth,
                                pitchDegrees = liveCompassState.pitchDegrees,
                                rollDegrees = liveCompassState.rollDegrees,
                                targetBearing = targetBearing,
                                isLevel = isLevel,
                                isBengali = isBengali,
                                modifier = Modifier
                                    .size(dialSize)
                                    .testTag("compass_dial")
                            )

                            TargetBearingControls(
                                currentAzimuth = liveCompassState.azimuthDegrees,
                                targetBearing = targetBearing,
                                onSetTarget = { targetBearing = it },
                                onToggleCalibration = { showCalibrationTips = !showCalibrationTips },
                                showCalibrationTips = showCalibrationTips,
                                isBengali = isBengali
                            )

                            CompassTelemetryRow(
                                compassState = liveCompassState,
                                isLevel = isLevel,
                                isBengali = isBengali
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showCalibrationTips) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compass_calibration_guide")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isBengali) "কম্পাস ক্যালিব্রেশন গাইড" else "How to Calibrate Compass",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isBengali) {
                                            "সঠিক দিকনির্দেশনা পেতে আপনার ফোনটি হাত দিয়ে বাতাসে ইংরেজি '8' (আট) আকৃতিতে কয়েকবার ঘোরান এবং ধাতব বস্তু বা চুম্বক থেকে দূরে রাখুন।"
                                        } else {
                                            "Wave your phone in a figure-8 motion several times away from metal objects or magnetic cases, and keep the device flat for highest precision."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeadingReadoutBanner(
    headingStr: String,
    shortDir: String,
    fullDir: String,
    targetBearing: Float?,
    currentAzimuth: Float,
    isBengali: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$headingStr°",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("compass_heading_text")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = shortDir,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = fullDir,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (targetBearing != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val targetDegInt = CompassHelper.normalizeDegrees(targetBearing).roundToInt() % 360
                val targetDegStr = DateFormatter.formatNumber(targetDegInt, isBengali)
                val targetDir = CompassHelper.getShortCardinalDirection(targetBearing, isBengali)
                val turnPrompt = CompassHelper.formatRelativeBearingTurn(currentAzimuth, targetBearing, isBengali)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBengali) {
                                "লক্ষ্য: $targetDegStr° ($targetDir) • $turnPrompt"
                            } else {
                                "Target: $targetDegStr° ($targetDir) • $turnPrompt"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TargetBearingControls(
    currentAzimuth: Float,
    targetBearing: Float?,
    onSetTarget: (Float?) -> Unit,
    onToggleCalibration: () -> Unit,
    showCalibrationTips: Boolean,
    isBengali: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = targetBearing != null,
                onClick = {
                    if (targetBearing == null) {
                        onSetTarget(CompassHelper.normalizeDegrees(currentAzimuth))
                    } else {
                        onSetTarget(null)
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (targetBearing != null) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = if (targetBearing != null) {
                            if (isBengali) "লক্ষ্য আনলক করুন" else "Clear Lock"
                        } else {
                            if (isBengali) "দিক লক করুন" else "Lock Bearing"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.testTag("compass_lock_bearing_btn")
            )

            AssistChip(
                onClick = onToggleCalibration,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = {
                    Text(
                        text = if (showCalibrationTips) {
                            if (isBengali) "গাইড লুকান" else "Hide Guide"
                        } else {
                            if (isBengali) "ক্যালিব্রেশন" else "Calibrate"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = Modifier.testTag("compass_calibration_btn")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick cardinal target buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            val quickTargets = listOf(
                0f to (if (isBengali) "উ ০°" else "N 0°"),
                90f to (if (isBengali) "পূ ৯০°" else "E 90°"),
                180f to (if (isBengali) "দ ১৮০°" else "S 180°"),
                270f to (if (isBengali) "প ২৭০°" else "W 270°")
            )
            quickTargets.forEach { (deg, label) ->
                val isSelected = targetBearing != null && abs(CompassHelper.shortestAngleDelta(targetBearing, deg)) < 1f
                Surface(
                    onClick = { onSetTarget(if (isSelected) null else deg) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompassTelemetryRow(
    compassState: CompassState,
    isLevel: Boolean,
    isBengali: Boolean
) {
    val magStrengthStr = String.format(Locale.US, "%.1f", compassState.magneticFieldStrengthUt)
    val magFormatted = if (isBengali) {
        magStrengthStr.map { ch ->
            if (ch in '0'..'9') DateFormatter.formatNumber(ch - '0', true).first() else ch
        }.joinToString("")
    } else {
        magStrengthStr
    }

    val pitchInt = compassState.pitchDegrees.roundToInt()
    val rollInt = compassState.rollDegrees.roundToInt()
    val pitchStr = "${if (pitchInt < 0) "-" else ""}${DateFormatter.formatNumber(abs(pitchInt), isBengali)}°"
    val rollStr = "${if (rollInt < 0) "-" else ""}${DateFormatter.formatNumber(abs(rollInt), isBengali)}°"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Magnetic Field Strength Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isBengali) "চৌম্বক ক্ষেত্র" else "Magnetic Field",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (compassState.magneticFieldStrengthUt > 0f) "$magFormatted µT" else "-- µT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBengali) "স্বাভাবিক: ২৫–৬৫ µT" else "Normal: 25–65 µT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }
        }

        // Device Level / Tilt Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isLevel) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "ডিভাইস লেভেল" else "Device Tilt",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = if (isLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLevel) {
                        if (isBengali) "সমান্তরাল" else "Level"
                    } else {
                        "P:$pitchStr  R:$rollStr"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isBengali) {
                        if (compassState.usingRotationVector) "৩-অক্ষ সেন্সর সক্রিয়" else "ম্যাগনেটোমিটার সক্রিয়"
                    } else {
                        if (compassState.usingRotationVector) "Rotation Vector Active" else "Accel + Mag Active"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CompassUnavailableBanner(isBengali: Boolean) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compass_unavailable_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SensorsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isBengali) "কম্পাস সেন্সর পাওয়া যায়নি" else "Compass Sensor Unavailable",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isBengali) {
                    "এই ডিভাইস বা এমুলেটরে চৌম্বকীয় দিকনির্দেশনার জন্য ম্যাগনেটোমিটার সেন্সর যুক্ত নেই।"
                } else {
                    "This device or emulator does not have a magnetometer hardware sensor required for compass headings."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CompassDialCanvas(
    animatedAzimuth: Float,
    pitchDegrees: Float,
    rollDegrees: Float,
    targetBearing: Float?,
    isLevel: Boolean,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurface = MaterialTheme.colorScheme.onSurface
    val outline = MaterialTheme.colorScheme.outline

    val cardinalLabels = remember(isBengali) {
        if (isBengali) listOf("উ", "পূ", "দ", "প") else listOf("N", "E", "S", "W")
    }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer ambient bezel gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    surfaceVariant.copy(alpha = 0.25f),
                    surfaceVariant.copy(alpha = 0.75f)
                ),
                center = center,
                radius = radius
            ),
            radius = radius - 4.dp.toPx(),
            center = center
        )

        // Outer border ring
        drawCircle(
            color = outline.copy(alpha = 0.35f),
            radius = radius - 4.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Fixed inner ring
        drawCircle(
            color = outline.copy(alpha = 0.2f),
            radius = radius * 0.62f,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // Rotate the compass rose by -animatedAzimuth so North aligns with real magnetic North
        rotate(degrees = -animatedAzimuth, pivot = center) {
            val dialRadius = radius - 10.dp.toPx()

            // Degree tick marks every 5 degrees (72 ticks)
            for (deg in 0 until 360 step 5) {
                val isCardinal = deg % 90 == 0
                val isMajor = deg % 30 == 0
                val isMedium = deg % 15 == 0

                val tickLength = when {
                    isCardinal -> 14.dp.toPx()
                    isMajor -> 11.dp.toPx()
                    isMedium -> 7.dp.toPx()
                    else -> 4.dp.toPx()
                }
                val tickStroke = when {
                    isCardinal -> 2.8.dp.toPx()
                    isMajor -> 2.dp.toPx()
                    else -> 1.dp.toPx()
                }
                val tickColor = when {
                    deg == 0 -> errorColor
                    isCardinal -> primaryColor
                    isMajor -> onSurface.copy(alpha = 0.75f)
                    else -> outline.copy(alpha = 0.55f)
                }

                val angleRad = deg * (PI / 180.0)
                val outerR = dialRadius
                val innerR = dialRadius - tickLength

                val start = Offset(
                    center.x + (innerR * sin(angleRad)).toFloat(),
                    center.y - (innerR * cos(angleRad)).toFloat()
                )
                val end = Offset(
                    center.x + (outerR * sin(angleRad)).toFloat(),
                    center.y - (outerR * cos(angleRad)).toFloat()
                )

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )

                // Degree numbers every 30 degrees (except cardinals? Let's draw outer/inner nicely)
                if (isMajor && !isCardinal) {
                    val degLabel = DateFormatter.formatNumber(deg, isBengali)
                    val measured = textMeasurer.measure(
                        text = degLabel,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurface.copy(alpha = 0.72f)
                        )
                    )
                    val labelR = dialRadius - 24.dp.toPx()
                    val lx = center.x + (labelR * sin(angleRad)).toFloat()
                    val ly = center.y - (labelR * cos(angleRad)).toFloat()
                    rotate(degrees = animatedAzimuth, pivot = Offset(lx, ly)) {
                        drawText(
                            textLayoutResult = measured,
                            topLeft = Offset(
                                lx - measured.size.width / 2f,
                                ly - measured.size.height / 2f
                            )
                        )
                    }
                }
            }

            // Cardinal letters N / E / S / W
            for (i in 0 until 4) {
                val deg = i * 90
                val angleRad = deg * (PI / 180.0)
                val isNorth = i == 0
                val label = cardinalLabels[i]
                val measured = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        fontSize = if (isNorth) 20.sp else 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isNorth) errorColor else onSurface
                    )
                )
                val labelR = dialRadius - 34.dp.toPx()
                val lx = center.x + (labelR * sin(angleRad)).toFloat()
                val ly = center.y - (labelR * cos(angleRad)).toFloat()

                // Keep text upright relative to the user's screen
                rotate(degrees = animatedAzimuth, pivot = Offset(lx, ly)) {
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            lx - measured.size.width / 2f,
                            ly - measured.size.height / 2f
                        )
                    )
                }
            }

            // Central Needle (Red tip pointing North, muted tip pointing South)
            val needleTipR = radius * 0.42f
            val needleWidth = 10.dp.toPx()
            val northPath = Path().apply {
                moveTo(center.x, center.y - needleTipR)
                lineTo(center.x - needleWidth / 2f, center.y)
                lineTo(center.x + needleWidth / 2f, center.y)
                close()
            }
            drawPath(path = northPath, color = errorColor)

            val southPath = Path().apply {
                moveTo(center.x, center.y + needleTipR)
                lineTo(center.x - needleWidth / 2f, center.y)
                lineTo(center.x + needleWidth / 2f, center.y)
                close()
            }
            drawPath(path = southPath, color = outline.copy(alpha = 0.55f))

            // Optional Target Bearing Marker Wedge on the outer rim
            if (targetBearing != null) {
                val tRad = targetBearing * (PI / 180.0)
                val markerOuter = dialRadius + 2.dp.toPx()
                val markerInner = dialRadius - 14.dp.toPx()
                val tip = Offset(
                    center.x + (markerInner * sin(tRad)).toFloat(),
                    center.y - (markerInner * cos(tRad)).toFloat()
                )
                val leftAngle = (targetBearing - 4f) * (PI / 180.0)
                val rightAngle = (targetBearing + 4f) * (PI / 180.0)
                val leftPt = Offset(
                    center.x + (markerOuter * sin(leftAngle)).toFloat(),
                    center.y - (markerOuter * cos(leftAngle)).toFloat()
                )
                val rightPt = Offset(
                    center.x + (markerOuter * sin(rightAngle)).toFloat(),
                    center.y - (markerOuter * cos(rightAngle)).toFloat()
                )
                val targetPath = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(leftPt.x, leftPt.y)
                    lineTo(rightPt.x, rightPt.y)
                    close()
                }
                drawPath(path = targetPath, color = tertiaryColor)
            }
        }

        // Fixed Top Lubber Triangle Indicator (shows current device heading at 12 o'clock)
        val lubberTopY = 0.dp.toPx()
        val lubberPath = Path().apply {
            moveTo(center.x, lubberTopY + 12.dp.toPx())
            lineTo(center.x - 7.dp.toPx(), lubberTopY)
            lineTo(center.x + 7.dp.toPx(), lubberTopY)
            close()
        }
        drawPath(path = lubberPath, color = primaryColor)

        // Subtle Bubble Level Crosshair at Center
        val maxBubbleOffset = 18.dp.toPx()
        val clampedRoll = (rollDegrees / 45f).coerceIn(-1f, 1f)
        val clampedPitch = (pitchDegrees / 45f).coerceIn(-1f, 1f)
        val bubbleCenter = Offset(
            center.x + clampedRoll * maxBubbleOffset,
            center.y + clampedPitch * maxBubbleOffset
        )

        drawCircle(
            color = if (isLevel) primaryColor.copy(alpha = 0.25f) else secondaryColor.copy(alpha = 0.22f),
            radius = 12.dp.toPx(),
            center = bubbleCenter
        )
        drawCircle(
            color = if (isLevel) primaryColor else secondaryColor,
            radius = 5.dp.toPx(),
            center = bubbleCenter
        )

        // Center Crosshair
        drawLine(
            color = onSurface.copy(alpha = 0.28f),
            start = Offset(center.x - 10.dp.toPx(), center.y),
            end = Offset(center.x + 10.dp.toPx(), center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = onSurface.copy(alpha = 0.28f),
            start = Offset(center.x, center.y - 10.dp.toPx()),
            end = Offset(center.x, center.y + 10.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
}
