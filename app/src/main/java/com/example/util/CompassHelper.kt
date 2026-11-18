package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class CompassState(
    val azimuthDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val magneticFieldStrengthUt: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val hasCompassSensor: Boolean = true,
    val usingRotationVector: Boolean = false
)

object CompassHelper {

    fun normalizeDegrees(degrees: Float): Float {
        var d = degrees % 360f
        if (d < 0f) d += 360f
        return d
    }

    /**
     * Returns shortest signed angular difference from [current] to [target] in [-180, 180].
     */
    fun shortestAngleDelta(current: Float, target: Float): Float {
        return ((target - current + 540f) % 360f) - 180f
    }

    fun getCardinalIndex(azimuthDegrees: Float): Int {
        val normalized = normalizeDegrees(azimuthDegrees)
        return (((normalized + 22.5f) % 360f) / 45f).toInt().coerceIn(0, 7)
    }

    fun getShortCardinalDirection(azimuthDegrees: Float, isBengali: Boolean): String {
        val idx = getCardinalIndex(azimuthDegrees)
        val en = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val bn = arrayOf("উ", "উ-পূ", "পূ", "দ-পূ", "দ", "দ-প", "প", "উ-প")
        return if (isBengali) bn[idx] else en[idx]
    }

    fun getFullCardinalDirection(azimuthDegrees: Float, isBengali: Boolean): String {
        val idx = getCardinalIndex(azimuthDegrees)
        val en = arrayOf(
            "North", "Northeast", "East", "Southeast",
            "South", "Southwest", "West", "Northwest"
        )
        val bn = arrayOf(
            "উত্তর", "উত্তর-পূর্ব", "পূর্ব", "দক্ষিণ-পূর্ব",
            "দক্ষিণ", "দক্ষিণ-পশ্চিম", "পশ্চিম", "উত্তর-পশ্চিম"
        )
        return if (isBengali) bn[idx] else en[idx]
    }

    fun getAccuracyLabel(accuracy: Int, isBengali: Boolean): String {
        return when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH ->
                if (isBengali) "উচ্চ নির্ভুলতা" else "High Accuracy"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM ->
                if (isBengali) "মাঝারি নির্ভুলতা" else "Medium Accuracy"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW ->
                if (isBengali) "নিম্ন নির্ভুলতা" else "Low Accuracy"
            else ->
                if (isBengali) "ক্যালিব্রেশন প্রয়োজন" else "Needs Calibration"
        }
    }

    fun formatRelativeBearingTurn(currentAzimuth: Float, targetAzimuth: Float, isBengali: Boolean): String {
        val delta = shortestAngleDelta(currentAzimuth, targetAzimuth)
        val absDeg = abs(delta).roundToInt()
        val degStr = DateFormatter.formatNumber(absDeg, isBengali)
        return when {
            absDeg <= 2 -> if (isBengali) "লক্ষ্য বরাবর ($degStr°)" else "On Target ($degStr°)"
            delta > 0f -> if (isBengali) "ডানে $degStr° ঘুরুন" else "Turn $degStr° Right"
            else -> if (isBengali) "বামে $degStr° ঘুরুন" else "Turn $degStr° Left"
        }
    }
}

@Composable
fun rememberCompassState(): CompassState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }

    val rotationVectorSensor = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }
    val accelerometerSensor = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val magnetometerSensor = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    val hasCompassSensor = remember(rotationVectorSensor, accelerometerSensor, magnetometerSensor) {
        rotationVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null)
    }

    var compassState by remember(hasCompassSensor, rotationVectorSensor) {
        mutableStateOf(
            CompassState(
                hasCompassSensor = hasCompassSensor,
                usingRotationVector = rotationVectorSensor != null,
                accuracy = if (hasCompassSensor) SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM else SensorManager.SENSOR_STATUS_UNRELIABLE
            )
        )
    }

    DisposableEffect(context, lifecycleOwner, sensorManager, hasCompassSensor) {
        if (sensorManager == null || !hasCompassSensor) {
            onDispose {}
        } else {
            val rotationMatrix = FloatArray(9)
            val remappedMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var hasGravity = false
            var hasGeomagnetic = false
            var smoothedAzimuth: Float? = null
            var smoothedPitch = 0f
            var smoothedRoll = 0f
            var latestMagUt = 0f
            var latestAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM

            val displayRotationProvider: () -> Int = {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        context.display?.rotation ?: Surface.ROTATION_0
                    } else {
                        @Suppress("DEPRECATION")
                        (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                            ?.defaultDisplay?.rotation ?: Surface.ROTATION_0
                    }
                } catch (_: Exception) {
                    Surface.ROTATION_0
                }
            }

            fun updateOrientationFromRotationMatrix(rawMatrix: FloatArray) {
                val (axisX, axisY) = when (displayRotationProvider()) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }

                val targetMatrix = if (axisX == SensorManager.AXIS_X && axisY == SensorManager.AXIS_Y) {
                    rawMatrix
                } else {
                    SensorManager.remapCoordinateSystem(rawMatrix, axisX, axisY, remappedMatrix)
                    remappedMatrix
                }

                SensorManager.getOrientation(targetMatrix, orientationAngles)
                val rawAzimuth = CompassHelper.normalizeDegrees(
                    Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                )
                val rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                val alpha = 0.18f
                val prev = smoothedAzimuth
                val nextAzimuth = if (prev == null) {
                    rawAzimuth
                } else {
                    val delta = CompassHelper.shortestAngleDelta(prev, rawAzimuth)
                    CompassHelper.normalizeDegrees(prev + alpha * delta)
                }
                smoothedAzimuth = nextAzimuth
                smoothedPitch += alpha * (rawPitch - smoothedPitch)
                smoothedRoll += alpha * (rawRoll - smoothedRoll)

                compassState = CompassState(
                    azimuthDegrees = nextAzimuth,
                    pitchDegrees = smoothedPitch,
                    rollDegrees = smoothedRoll,
                    magneticFieldStrengthUt = latestMagUt,
                    accuracy = latestAccuracy,
                    hasCompassSensor = true,
                    usingRotationVector = rotationVectorSensor != null
                )
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            updateOrientationFromRotationMatrix(rotationMatrix)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            val alpha = 0.8f
                            gravity[0] = alpha * gravity[0] + (1f - alpha) * event.values[0]
                            gravity[1] = alpha * gravity[1] + (1f - alpha) * event.values[1]
                            gravity[2] = alpha * gravity[2] + (1f - alpha) * event.values[2]
                            hasGravity = true
                            if (rotationVectorSensor == null && hasGeomagnetic) {
                                if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                    updateOrientationFromRotationMatrix(rotationMatrix)
                                }
                            }
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            val mx = event.values[0]
                            val my = event.values[1]
                            val mz = event.values[2]
                            latestMagUt = sqrt(mx * mx + my * my + mz * mz)

                            val alpha = 0.8f
                            geomagnetic[0] = alpha * geomagnetic[0] + (1f - alpha) * mx
                            geomagnetic[1] = alpha * geomagnetic[1] + (1f - alpha) * my
                            geomagnetic[2] = alpha * geomagnetic[2] + (1f - alpha) * mz
                            hasGeomagnetic = true

                            if (rotationVectorSensor == null && hasGravity) {
                                if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                    updateOrientationFromRotationMatrix(rotationMatrix)
                                }
                            } else if (rotationVectorSensor != null) {
                                // Refresh field strength even when orientation comes from rotation vector
                                compassState = compassState.copy(
                                    magneticFieldStrengthUt = latestMagUt,
                                    accuracy = latestAccuracy
                                )
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD || sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        latestAccuracy = accuracy
                        compassState = compassState.copy(accuracy = accuracy)
                    }
                }
            }

            fun registerSensors() {
                if (rotationVectorSensor != null) {
                    sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
                } else {
                    accelerometerSensor?.let {
                        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
                    }
                }
                // Always register magnetometer when available for field strength & accuracy
                magnetometerSensor?.let {
                    sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
                }
            }

            fun unregisterSensors() {
                sensorManager.unregisterListener(listener)
            }

            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                registerSensors()
            }

            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> registerSensors()
                    Lifecycle.Event.ON_PAUSE -> unregisterSensors()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                unregisterSensors()
            }
        }
    }

    return compassState
}
