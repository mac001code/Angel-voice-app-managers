package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Creator
import com.example.data.model.HealthStatus
import com.example.data.model.TikTokLeague
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HoloDotWhite
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.PaletteVoid
import com.example.ui.theme.Stone
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreatorCard(
    creator: Creator,
    onCardClick: () -> Unit,
    onHableHoyClick: () -> Unit,
    onAuditeLiveClick: () -> Unit,
    onQueDecirleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val health = creator.calculateHealthStatus()
    val healthColor = when (health) {
        HealthStatus.EN_RACHA_VERDE -> EmeraldGreen
        HealthStatus.MANTENIMIENTO_AMARILLO -> AmberGold
        HealthStatus.EN_RIESGO_ROJO -> CrimsonCoral
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (health == HealthStatus.EN_RIESGO_ROJO) CrimsonCoral.copy(alpha = 0.55f) else CardBorder,
        label = "cardBorderColor"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.8.dp, animatedBorderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .testTag("creator_card_${creator.creatorId}"),
        color = ElevatedSlate.copy(alpha = 0.88f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HolographicAvatarBadge(
                        initial = creator.name.take(1).uppercase(),
                        statusColor = healthColor,
                        modifier = Modifier.size(46.dp)
                    )
                    Text(
                        text = creator.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = HoloDotWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = creator.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        CountryTag(country = creator.country)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = creator.tiktokHandle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Mist
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                LeagueChip(league = creator.tiktokLeague)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElevatedSlateVariant.copy(alpha = 0.65f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${creator.niche.displayName} · ${creator.liveCadenceDaysWeek} días/sem",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Stone,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(healthColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = creator.contactFormatted(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = healthColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatNumber(creator.diamondsEarned)} diamantes",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Ink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "${creator.peakViewers} viewers pico",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Mist,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${creator.totalLiveHours}h LIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Mist,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onHableHoyClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_hable_hoy_${creator.creatorId}"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (creator.daysSinceContact() == 0L) EmeraldGreen.copy(alpha = 0.16f) else ElevatedSlateVariant,
                        contentColor = if (creator.daysSinceContact() == 0L) EmeraldGreen else TextPrimary
                    ),
                    border = if (creator.daysSinceContact() == 0L) {
                        BorderStroke(0.8.dp, EmeraldGreen.copy(alpha = 0.6f))
                    } else {
                        BorderStroke(0.8.dp, CardBorder)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Hablé hoy",
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (creator.daysSinceContact() == 0L) "Al día" else "Hablé hoy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onAuditeLiveClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_audite_live_${creator.creatorId}"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElevatedSlateVariant,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(0.8.dp, CardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Auditar",
                        modifier = Modifier.size(13.dp),
                        tint = Stone
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Audité LIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onQueDecirleClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("btn_que_decirle_${creator.creatorId}"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Ink,
                        contentColor = PaletteVoid
                    ),
                    border = BorderStroke(0.8.dp, CardBorderSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Qué decirle",
                        modifier = Modifier.size(13.dp),
                        tint = PaletteVoid
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Qué decirle",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PaletteVoid,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun LeagueChip(
    league: TikTokLeague,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (league) {
        TikTokLeague.LIGA_A -> "Liga A" to Ink
        TikTokLeague.LIGA_B -> "Liga B" to EmeraldGreen
        TikTokLeague.LIGA_C -> "Liga C" to AmberGold
        TikTokLeague.LIGA_D -> "Liga D" to Mist
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(0.8.dp, color.copy(alpha = 0.45f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Medium,
                fontSize = 10.5.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
fun CountryTag(
    country: String,
    modifier: Modifier = Modifier
) {
    val flagEmoji = when (country.uppercase()) {
        "VE" -> "🇻🇪"
        "MX" -> "🇲🇽"
        "AR" -> "🇦🇷"
        "CO" -> "🇨🇴"
        "ES" -> "🇪🇸"
        "CL" -> "🇨🇱"
        "PE" -> "🇵🇪"
        else -> "🌎"
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(ElevatedSlateVariant)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = flagEmoji,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = country.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun formatNumber(num: Long): String {
    return when {
        num >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", num / 1_000_000.0)
        num >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", num / 1_000.0)
        else -> num.toString()
    }
}