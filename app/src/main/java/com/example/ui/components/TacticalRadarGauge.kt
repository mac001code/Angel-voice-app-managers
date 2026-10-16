package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Aluminio
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.Ceniza
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PantallaApagada
import com.example.ui.theme.Papel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TacticalRadarGauge(
    healthPercentage: Int,
    greenCount: Int,
    yellowCount: Int,
    redCount: Int,
    dailyActionsCompleted: Int,
    dailyTarget: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = healthPercentage / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "radarProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ElevatedSlate)
            .border(0.8.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("widget_salud_circulo")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Info: 2026 Telemetry Console Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (healthPercentage >= 80) EmeraldGreen else AmberGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SALUD DEL CREADOR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Papel,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Text(
                    text = "LiDAR • 60 FPS // 2026",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Ceniza,
                        letterSpacing = 0.8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main 3D Sphere & Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 3D LiDAR Sphere with center readout
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HolographicSphereVisualizer(
                        modifier = Modifier.size(112.dp),
                        statusColor = if (healthPercentage >= 80) EmeraldGreen else AmberGold
                    )

                    // Translucent Center Readout in PantallaApagada
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PantallaApagada.copy(alpha = 0.88f))
                            .border(0.6.dp, CardBorder, CircleShape)
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "$healthPercentage%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Papel,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = if (healthPercentage >= 80) "ESTABLE" else "ATENCIÓN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (healthPercentage >= 80) EmeraldGreen else AmberGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Breakdown Badges and Status
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (healthPercentage >= 80) "Rendimiento Óptimo" else "Intervención Táctica Requerida",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Papel,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )

                    Text(
                        text = "Monitoreo continuo de Ligas y retención",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Ceniza,
                            fontSize = 11.sp
                        )
                    )

                    // Industrial Status Badges (Aluminio / Carcasa / Ceniza)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IndustrialStatusChip(label = "$greenCount Racha", color = EmeraldGreen)
                        IndustrialStatusChip(label = "$yellowCount Mant.", color = AmberGold)
                        if (redCount > 0) {
                            IndustrialStatusChip(label = "$redCount Riesgo", color = CrimsonCoral)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Goal Bar: Physical Instrument Progress Deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElevatedSlateVariant)
                    .border(0.6.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Metas",
                        tint = Papel,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Metas de Hoy:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$dailyActionsCompleted/$dailyTarget acciones",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Papel,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp
                        )
                    )
                }

                // Micro indicator dots (Point Matrix)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (step in 1..dailyTarget) {
                        val isDone = step <= dailyActionsCompleted
                        Box(
                            modifier = Modifier
                                .size(if (isDone) 6.dp else 4.dp)
                                .clip(CircleShape)
                                .background(if (isDone) Papel else Ceniza)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndustrialStatusChip(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ElevatedSlateVariant)
            .border(0.6.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.5.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Papel,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
    }
}
