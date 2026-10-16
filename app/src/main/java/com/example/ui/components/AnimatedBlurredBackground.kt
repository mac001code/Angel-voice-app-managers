package com.angelvoice.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.angelvoice.app.ui.theme.EarthCharcoal
import com.angelvoice.app.ui.theme.Ink
import com.angelvoice.app.ui.theme.Stone
import com.angelvoice.app.ui.theme.Void
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedBlurredBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "cosmosAmbientTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambientPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Void)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Nebulosa superior cálida en movimiento lento
            val orb1Center = Offset(
                x = w * (0.5f + 0.18f * sin(phase)),
                y = h * (0.24f + 0.08f * cos(phase * 0.7f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Stone.copy(alpha = 0.08f),
                        EarthCharcoal.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = w * 0.85f
                ),
                center = orb1Center,
                radius = w * 0.85f
            )

            // Nebulosa inferior de profundidad
            val orb2Center = Offset(
                x = w * (0.45f + 0.22f * cos(phase * 0.8f)),
                y = h * (0.72f + 0.10f * sin(phase * 0.6f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EarthCharcoal.copy(alpha = 0.09f),
                        Ink.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = w * 0.95f
                ),
                center = orb2Center,
                radius = w * 0.95f
            )
        }
        content()
    }
}