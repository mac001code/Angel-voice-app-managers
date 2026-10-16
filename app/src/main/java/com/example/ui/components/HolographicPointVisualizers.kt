package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Aluminio
import com.example.ui.theme.CardBorder
import com.example.ui.theme.Ceniza
import com.example.ui.theme.HoloDotDim
import com.example.ui.theme.HoloDotSilver
import com.example.ui.theme.HoloDotWhite
import com.example.ui.theme.HoloLine
import com.example.ui.theme.HoloLineBright
import com.example.ui.theme.PantallaApagada
import com.example.ui.theme.Papel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 2026 LiDAR Holographic Sphere
 * Instrumental 3D point cloud sphere rendered against Pantalla Apagada (#0B0B0C).
 * Employs latitude/longitude orbit points, calibrated gimbal ring calipers in Aluminio (#A9ABAE),
 * and high-contrast Papel (#EEE8E4) phosphorus dots.
 */
@Composable
fun HolographicSphereVisualizer(
    modifier: Modifier = Modifier,
    statusColor: Color = HoloDotWhite
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sphereRotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val wobblePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wobble"
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2.15f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Caliper Gimbal Ring (Aluminio #A9ABAE)
        drawCircle(
            color = HoloLine,
            radius = radius + 4.dp.toPx(),
            center = center,
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Caliper cardinal ticks
        val tickCount = 12
        for (t in 0 until tickCount) {
            val tickAngle = 2.0 * Math.PI * (t.toDouble() / tickCount)
            val innerR = radius + 2.dp.toPx()
            val outerR = radius + if (t % 3 == 0) 6.dp.toPx() else 4.dp.toPx()
            val start = Offset((center.x + innerR * cos(tickAngle)).toFloat(), (center.y + innerR * sin(tickAngle)).toFloat())
            val end = Offset((center.x + outerR * cos(tickAngle)).toFloat(), (center.y + outerR * sin(tickAngle)).toFloat())
            drawLine(
                color = if (t % 3 == 0) Aluminio.copy(alpha = 0.6f) else Ceniza.copy(alpha = 0.4f),
                start = start,
                end = end,
                strokeWidth = if (t % 3 == 0) 1.2.dp.toPx() else 0.8.dp.toPx()
            )
        }

        val rotRad = Math.toRadians(rotationAngle.toDouble())

        // 2. 3D Sphere Point Matrix
        val numLatitudes = 13
        for (i in 1..numLatitudes) {
            val lat = Math.PI * (i.toDouble() / (numLatitudes + 1).toDouble() - 0.5)
            val ringRadius = radius * cos(lat).toFloat()
            val yPos = (center.y + radius * sin(lat)).toFloat()

            val numDotsInRing = (28 * cos(lat)).toInt().coerceAtLeast(8)
            for (j in 0 until numDotsInRing) {
                val lon = 2.0 * Math.PI * (j.toDouble() / numDotsInRing) + rotRad
                val xPos = (center.x + ringRadius * sin(lon)).toFloat()
                val zDepth = cos(lon) // -1.0 to 1.0 (perspective)

                val isFront = zDepth > -0.2
                if (isFront) {
                    val depthFactor = ((zDepth + 0.2) / 1.2).toFloat().coerceIn(0f, 1f)
                    val dotRadius = (1.0f + 1.8f * depthFactor).dp.toPx()
                    val alpha = (0.25f + 0.75f * depthFactor)

                    val dotColor = if (depthFactor > 0.8f && j % 4 == 0) {
                        statusColor.copy(alpha = alpha)
                    } else if (depthFactor > 0.4f) {
                        Papel.copy(alpha = alpha * 0.95f)
                    } else {
                        Aluminio.copy(alpha = alpha * 0.7f)
                    }

                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(xPos, yPos)
                    )
                }
            }
        }
    }
}

/**
 * 2026 Generative LiDAR Topographic Mesh
 * High-definition CAD topography in Aluminio (#A9ABAE) and Papel (#EEE8E4) over Pantalla Apagada.
 */
@Composable
fun TopographicTerrainHeader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "terrainWave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "terrainWaveAnim"
    )

    Canvas(modifier = modifier.fillMaxWidth()) {
        val width = size.width
        val height = size.height

        val numLines = 8
        for (lineIdx in 0 until numLines) {
            val progress = lineIdx.toFloat() / numLines
            val baseY = height * 0.22f + lineIdx * (height * 0.09f)
            val lineAlpha = 0.20f + 0.75f * progress

            val path = Path()
            val points = mutableListOf<Offset>()
            val steps = 36

            for (step in 0..steps) {
                val x = (step.toFloat() / steps) * width
                val nx = (x / width) * 4.0
                val mountainPeak = Math.exp(-Math.pow((nx - 2.0) * 1.6, 2.0))
                val wave = sin(nx * 3.2 + waveOffset + lineIdx * 0.45) * 5.dp.toPx()
                val y = (baseY - (mountainPeak * height * 0.38f * (1.0 - progress * 0.45)) + wave).toFloat()

                if (step == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                points.add(Offset(x, y))
            }

            // Draw elevation contour line (Aluminio #A9ABAE)
            drawPath(
                path = path,
                color = Aluminio.copy(alpha = lineAlpha * 0.5f),
                style = Stroke(width = if (lineIdx % 2 == 0) 1.2.dp.toPx() else 0.7.dp.toPx())
            )

            // Draw precision crest points (Papel #EEE8E4)
            for (pt in points.filterIndexed { idx, _ -> idx % 3 == 0 }) {
                drawCircle(
                    color = Papel.copy(alpha = (lineAlpha * 0.9f).coerceIn(0f, 1f)),
                    radius = 1.1.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

/**
 * 2026 Audio-Visual Oscilloscope Deck
 * Renders real-time harmonic waveform, frequency spectrum bars, and particle nodes.
 * Perfect for the combined Tactical AI + Voice Studio screen.
 */
@Composable
fun IndustrialAudioOscilloscope(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioScope")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        // Center baseline in Ceniza
        drawLine(
            color = Ceniza.copy(alpha = 0.35f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 0.8.dp.toPx()
        )

        // Waveform Path
        val path = Path()
        val steps = 60
        for (i in 0..steps) {
            val progress = i.toFloat() / steps
            val x = progress * width
            val envelope = sin(progress * Math.PI).toFloat() // zero at ends, 1 at center

            val amplitude = if (isRecording) {
                (sin(progress * 14.0 + phase) * 0.6 + sin(progress * 28.0 - phase * 1.5) * 0.4).toFloat() * 16.dp.toPx() * envelope
            } else {
                sin(progress * 6.0 + phase).toFloat() * 2.5.dp.toPx() * envelope
            }

            val y = centerY + amplitude

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Periodic particle node in Papel
            if (isRecording && i % 4 == 0) {
                drawCircle(
                    color = Papel,
                    radius = 1.4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Draw main harmonic waveform line (Aluminio / Papel)
        drawPath(
            path = path,
            color = if (isRecording) Papel else Aluminio.copy(alpha = 0.7f),
            style = Stroke(width = if (isRecording) 1.6.dp.toPx() else 1.0.dp.toPx())
        )
    }
}

/**
 * 2026 Laser-Engraved Avatar Badge
 * Precision circular chassis in Aluminio (#A9ABAE) with recessed status LED dot.
 */
@Composable
fun HolographicAvatarBadge(
    initial: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 2.dp.toPx()

        // 1. Chassis Outer Bezel
        drawCircle(
            color = CardBorder,
            radius = radius + 1.dp.toPx(),
            center = center,
            style = Stroke(width = 0.8.dp.toPx())
        )

        // 2. Recessed Background
        drawCircle(
            color = PantallaApagada,
            radius = radius,
            center = center
        )

        // 3. Precision Dot Halo
        val haloDots = 16
        for (i in 0 until haloDots) {
            val angle = 2.0 * Math.PI * (i.toDouble() / haloDots)
            val hx = (center.x + (radius - 2.dp.toPx()) * cos(angle)).toFloat()
            val hy = (center.y + (radius - 2.dp.toPx()) * sin(angle)).toFloat()
            drawCircle(
                color = if (i % 4 == 0) statusColor else Ceniza.copy(alpha = 0.4f),
                radius = 1.0.dp.toPx(),
                center = Offset(hx, hy)
            )
        }
    }
}
