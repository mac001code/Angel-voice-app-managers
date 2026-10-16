package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Creator
import com.example.data.model.HealthStatus
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.EarthCharcoal
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.PaletteVoid
import com.example.ui.theme.Stone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PyramidalWaveHealthIndicator(
    healthPercentage: Int,
    creators: List<Creator>,
    selectedCreator: Creator?,
    onSelectCreator: (Creator?) -> Unit,
    onHableHoyClick: (Creator) -> Unit,
    onAuditeLiveClick: (Creator) -> Unit,
    onQueDecirleClick: (Creator) -> Unit,
    onOpenCreatorDetail: (Creator) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "organicSphereTransition")

    val fluidTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fluidTimeAnim"
    )

    val twinklePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinklePhase"
    )

    val haptic = LocalHapticFeedback.current
    var nodePositions by remember { mutableStateOf<List<Pair<Creator, Offset>>>(emptyList()) }

    val enRachaCount = creators.count { it.calculateHealthStatus() == HealthStatus.EN_RACHA_VERDE }
    val mantCount = creators.count { it.calculateHealthStatus() == HealthStatus.MANTENIMIENTO_AMARILLO }
    val riesgoCount = creators.count { it.calculateHealthStatus() == HealthStatus.EN_RIESGO_ROJO }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("indicador_onda_piramidal_central")
    ) {
        // Encabezado limpio "Estado del ecosistema" (sin colisiones)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estado del ecosistema",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Ink,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (selectedCreator != null) {
                        "Nodo activo · ${selectedCreator.name}"
                    } else {
                        "Toca un punto luminoso en la esfera para interactuar"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = Mist),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ElevatedSlate.copy(alpha = 0.85f))
                    .border(0.8.dp, CardBorder, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    riesgoCount > 0 -> CrimsonCoral
                                    mantCount > 0 -> AmberGold
                                    else -> EmeraldGreen
                                }
                            )
                    )
                    Text(
                        text = "$healthPercentage% armonía",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Stone,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Esfera Orgánica Central con Displacement y Rim Light (Flotando en el vacío)[cite: 2]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(265.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(creators) {
                        detectTapGestures { tapOffset ->
                            val touchRadiusPx = 42.dp.toPx()
                            val hit = nodePositions.minByOrNull { (_, offset) ->
                                val dx = offset.x - tapOffset.x
                                val dy = offset.y - tapOffset.y
                                sqrt(dx * dx + dy * dy)
                            }
                            if (hit != null) {
                                val dist = sqrt(
                                    (hit.second.x - tapOffset.x) * (hit.second.x - tapOffset.x) +
                                        (hit.second.y - tapOffset.y) * (hit.second.y - tapOffset.y)
                                )
                                if (dist <= touchRadiusPx) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectCreator(if (selectedCreator?.creatorId == hit.first.creatorId) null else hit.first)
                                } else {
                                    onSelectCreator(null)
                                }
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val cx = width * 0.5f
                val cy = height * 0.5f
                val baseRadius = width.coerceAtMost(height) * 0.38f

                // Atmósfera exterior suave[cite: 1]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Stone.copy(alpha = 0.08f),
                            EarthCharcoal.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = baseRadius * 1.45f
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 1.45f
                )

                // Función de desplazamiento orgánico esférico 3D (Blob con Rim Light)[cite: 2]
                fun evaluateOrganicSphere(theta: Float, phi: Float): Triple<Offset, Float, Float> {
                    val nx = sin(phi.toDouble()).toFloat() * cos(theta.toDouble()).toFloat()
                    val ny = cos(phi.toDouble()).toFloat()
                    val nz = sin(phi.toDouble()).toFloat() * sin(theta.toDouble()).toFloat()

                    val w1 = sin(3f * theta + fluidTime) * cos(2f * phi - fluidTime * 0.8f)
                    val w2 = sin(4f * phi - fluidTime * 1.2f) * cos(3f * theta + fluidTime * 0.6f)
                    val w3 = cos(2f * (theta + phi) + fluidTime * 1.4f)
                    val disp = 1f + 0.13f * w1 + 0.08f * w2 + 0.05f * w3

                    val r = baseRadius * disp
                    val x3d = nx * r
                    val y3d = ny * r
                    val z3d = nz * r

                    val tilt = 0.26f
                    val yRot = y3d * cos(tilt) - z3d * sin(tilt)
                    val zRot = y3d * sin(tilt) + z3d * cos(tilt)

                    val persp = 1f + (zRot / (baseRadius * 3.2f))
                    val sx = cx + x3d * persp
                    val sy = cy + yRot * persp

                    val viewZ = (zRot / r).coerceIn(-1f, 1f)
                    val rim = (1f - abs(viewZ)).coerceIn(0f, 1f)
                    val fresnel = rim * rim * (0.38f + 0.62f * ((disp - 0.8f) / 0.45f).coerceIn(0f, 1f))

                    return Triple(Offset(sx, sy), zRot / baseRadius, fresnel.coerceIn(0f, 1f))
                }

                // Oclusión central negra profunda[cite: 2]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PaletteVoid,
                            PaletteVoid.copy(alpha = 0.94f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = baseRadius * 1.06f
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 1.06f
                )

                // Malla de partículas finas y Rim Light especular en los pliegues[cite: 2]
                val rings = 26
                val steps = 72
                for (i in 1 until rings) {
                    val phi = (i.toFloat() / rings.toFloat()) * Math.PI.toFloat()
                    var prevPt: Offset? = null

                    for (j in 0..steps) {
                        val theta = (j.toFloat() / steps.toFloat()) * (2f * Math.PI.toFloat())
                        val (pt, depth, fresnel) = evaluateOrganicSphere(theta, phi)

                        if (j % 2 == 0 && depth > -0.35f) {
                            val dotAlpha = (fresnel * 0.72f + 0.04f).coerceIn(0.03f, 0.85f)
                            drawCircle(
                                color = Ink.copy(alpha = dotAlpha),
                                radius = (0.9f + fresnel * 1.3f).dp.toPx(),
                                center = pt
                            )
                        }

                        if (prevPt != null && depth > -0.25f && fresnel > 0.22f) {
                            val lineAlpha = ((fresnel - 0.2f) * 0.95f).coerceIn(0.05f, 0.9f)
                            drawLine(
                                color = Ink.copy(alpha = lineAlpha),
                                start = prevPt,
                                end = pt,
                                strokeWidth = (0.6f + fresnel * 1.4f).dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                        prevPt = pt
                    }
                }

                // Nodos místicos titilantes de los creadores
                val calculatedNodes = mutableListOf<Pair<Creator, Offset>>()
                val goldenAngle = Math.PI.toFloat() * (3f - sqrt(5f))

                creators.forEachIndexed { idx, creator ->
                    val yNorm = if (creators.size == 1) 0f else {
                        1f - (idx.toFloat() / (creators.size - 1).coerceAtLeast(1)) * 1.5f
                    }.coerceIn(-0.75f, 0.75f)
                    val phi = acos(yNorm.toDouble()).toFloat()
                    val theta = idx * goldenAngle + fluidTime * 0.25f

                    val (nodeOffset, depth, _) = evaluateOrganicSphere(theta, phi)
                    calculatedNodes.add(creator to nodeOffset)

                    val health = creator.calculateHealthStatus()
                    val nodeColor = when (health) {
                        HealthStatus.EN_RACHA_VERDE -> EmeraldGreen
                        HealthStatus.MANTENIMIENTO_AMARILLO -> AmberGold
                        HealthStatus.EN_RIESGO_ROJO -> CrimsonCoral
                    }

                    val isSelected = selectedCreator?.creatorId == creator.creatorId
                    val twinkle = 0.55f + 0.45f * sin((twinklePhase + idx * 1.3f).toDouble()).toFloat()
                    val scale = (0.75f + 0.35f * ((depth + 1f) * 0.5f)).coerceIn(0.65f, 1.2f)

                    // Halo místico (Glow)
                    val glowRadius = (if (isSelected) 22.dp else 13.dp).toPx() * scale * (0.85f + 0.25f * twinkle)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                nodeColor.copy(alpha = if (isSelected) 0.55f else 0.34f * twinkle),
                                Ink.copy(alpha = 0.08f * twinkle),
                                Color.Transparent
                            ),
                            center = nodeOffset,
                            radius = max(glowRadius, 4f)
                        ),
                        center = nodeOffset,
                        radius = max(glowRadius, 4f)
                    )

                    if (isSelected || health == HealthStatus.EN_RIESGO_ROJO) {
                        drawCircle(
                            color = if (isSelected) Ink.copy(alpha = 0.85f) else nodeColor.copy(alpha = 0.45f * twinkle),
                            radius = (if (isSelected) 9.dp else 6.5.dp).toPx() * scale,
                            center = nodeOffset,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Punto estelar central
                    drawCircle(
                        color = nodeColor.copy(alpha = (0.75f + 0.25f * twinkle).coerceIn(0f, 1f)),
                        radius = (if (isSelected) 4.8.dp else 3.4.dp).toPx() * scale,
                        center = nodeOffset
                    )
                    drawCircle(
                        color = Ink,
                        radius = (if (isSelected) 2.1.dp else 1.4.dp).toPx() * scale,
                        center = nodeOffset
                    )
                }
                nodePositions = calculatedNodes
            }
        }

        // Leyenda sutil debajo de la esfera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SphereLegendDot(label = "En racha ($enRachaCount)", color = EmeraldGreen)
            Spacer(modifier = Modifier.width(16.dp))
            SphereLegendDot(label = "Estables ($mantCount)", color = AmberGold)
            Spacer(modifier = Modifier.width(16.dp))
            SphereLegendDot(label = "En riesgo ($riesgoCount)", color = CrimsonCoral)
        }

        // Tarjeta interactiva desplegable al tocar un creador en la esfera
        AnimatedVisibility(
            visible = selectedCreator != null,
            enter = fadeIn(tween(220)) + expandVertically(tween(260)),
            exit = fadeOut(tween(180)) + shrinkVertically(tween(220))
        ) {
            selectedCreator?.let { creator ->
                val health = creator.calculateHealthStatus()
                val statusColor = when (health) {
                    HealthStatus.EN_RACHA_VERDE -> EmeraldGreen
                    HealthStatus.MANTENIMIENTO_AMARILLO -> AmberGold
                    HealthStatus.EN_RIESGO_ROJO -> CrimsonCoral
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ElevatedSlate.copy(alpha = 0.94f))
                        .border(0.8.dp, CardBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .testTag("tarjeta_nodo_seleccionado")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = creator.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Ink,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${creator.tiktokHandle} · ${creator.tiktokLeague.displayName} · ${creator.contactFormatted()}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Mist),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onOpenCreatorDetail(creator) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Ver ficha",
                                    tint = Stone,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            IconButton(
                                onClick = { onSelectCreator(null) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = Mist,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SphereActionPill(
                            label = "Hablé hoy",
                            icon = Icons.Default.Check,
                            onClick = { onHableHoyClick(creator) },
                            modifier = Modifier.weight(1f)
                        )
                        SphereActionPill(
                            label = "Audité LIVE",
                            icon = Icons.Default.Videocam,
                            onClick = { onAuditeLiveClick(creator) },
                            modifier = Modifier.weight(1f)
                        )
                        SphereActionPill(
                            label = "Qué decirle",
                            icon = Icons.Default.AutoAwesome,
                            isPrimary = true,
                            onClick = { onQueDecirleClick(creator) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SphereLegendDot(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Mist)
        )
    }
}

@Composable
private fun SphereActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isPrimary) Ink else ElevatedSlateVariant)
            .border(0.8.dp, if (isPrimary) Ink else CardBorder, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isPrimary) PaletteVoid else Stone,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isPrimary) PaletteVoid else Ink,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}