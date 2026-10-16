package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Creator
import com.example.data.model.LifecyclePhase
import com.example.ui.components.CountryTag
import com.example.ui.components.HolographicAvatarBadge
import com.example.ui.components.LeagueChip
import com.example.ui.components.TopographicTerrainHeader
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.PaletteVoid
import com.example.ui.theme.Papel
import com.example.ui.theme.Stone
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AngelVoiceViewModel

@Composable
fun RecruiterScreen(
    viewModel: AngelVoiceViewModel,
    onOpenAddProspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prospects by viewModel.filteredRecruiterProspects.collectAsState()
    val activeFilter by viewModel.recruiterFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    val observacionCount = prospects.count { it.lifecyclePhase == LifecyclePhase.PROSPECCION }
    val induccionCount = prospects.count { it.lifecyclePhase == LifecyclePhase.INDUCCION }

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RadarBanner(
                    totalProspects = prospects.size,
                    observacionCount = observacionCount,
                    induccionCount = induccionCount
                )
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Buscar prospecto o nicho...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Mist, fontSize = 13.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Mist,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Stone,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = ElevatedSlate.copy(alpha = 0.85f),
                        unfocusedContainerColor = ElevatedSlate.copy(alpha = 0.85f)
                    ),
                    shape = CircleShape,
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipPill(
                        label = "Todos (${prospects.size})",
                        isSelected = activeFilter == "TODOS",
                        onClick = { viewModel.setRecruiterFilter("TODOS") }
                    )

                    FilterChipPill(
                        label = "1. Observación ($observacionCount)",
                        isSelected = activeFilter == "OBSERVACION",
                        badgeColor = Ink,
                        onClick = { viewModel.setRecruiterFilter("OBSERVACION") }
                    )

                    FilterChipPill(
                        label = "2. Inducción ($induccionCount)",
                        isSelected = activeFilter == "INDUCCION",
                        badgeColor = AmberGold,
                        onClick = { viewModel.setRecruiterFilter("INDUCCION") }
                    )
                }
            }

            if (prospects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ElevatedSlate.copy(alpha = 0.7f))
                            .border(0.8.dp, CardBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay prospectos en esta fase del embudo.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Mist)
                        )
                    }
                }
            } else {
                items(
                    items = prospects,
                    key = { it.creatorId }
                ) { prospect ->
                    RecruiterProspectCard(
                        prospect = prospect,
                        onStartContact = {
                            openWhatsAppRecruiter(context, prospect)
                            viewModel.updateInductionChecklist(prospect, "contact", true)
                        },
                        onToggleChecklist = { step, isDone ->
                            viewModel.updateInductionChecklist(prospect, step, isDone)
                        },
                        onTransferToAgency = {
                            viewModel.transferToAgency(prospect)
                        },
                        onOpenWhatToSay = {
                            viewModel.openWhatToSaySheet(prospect)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        FloatingActionButton(
            onClick = onOpenAddProspect,
            containerColor = Ink,
            contentColor = PaletteVoid,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_nuevo_prospecto")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Nuevo Prospecto",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RadarBanner(
    totalProspects: Int,
    observacionCount: Int,
    induccionCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ElevatedSlate.copy(alpha = 0.88f))
            .border(0.8.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("banner_radar_captacion")
    ) {
        Column {
            TopographicTerrainHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElevatedSlateVariant)
                    .border(0.6.dp, CardBorder, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Ink.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar",
                            tint = Ink,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Radar de scouting",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Embudo de captación activa",
                            style = MaterialTheme.typography.labelSmall.copy(color = Mist)
                        )
                    }
                }

                Text(
                    text = "$totalProspects talentos",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Stone,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FunnelPhaseItem(
                    stepNumber = "1",
                    title = "Observación",
                    count = observacionCount,
                    color = Ink,
                    modifier = Modifier.weight(1f)
                )
                FunnelPhaseItem(
                    stepNumber = "2",
                    title = "Inducción",
                    count = induccionCount,
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                FunnelPhaseItem(
                    stepNumber = "3",
                    title = "Fichaje",
                    count = null,
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FunnelPhaseItem(
    stepNumber: String,
    title: String,
    count: Int?,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ElevatedSlateVariant)
            .border(0.6.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fase $stepNumber",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.5.sp
                    )
                )
                if (count != null) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecruiterProspectCard(
    prospect: Creator,
    onStartContact: () -> Unit,
    onToggleChecklist: (String, Boolean) -> Unit,
    onTransferToAgency: () -> Unit,
    onOpenWhatToSay: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.8.dp, CardBorder, RoundedCornerShape(20.dp)),
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
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HolographicAvatarBadge(
                        initial = prospect.name.take(1).uppercase(),
                        statusColor = Ink,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = prospect.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
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
                            text = prospect.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        CountryTag(country = prospect.country)
                    }
                    Text(
                        text = prospect.tiktokHandle,
                        style = MaterialTheme.typography.bodySmall.copy(color = Mist),
                        maxLines = 1
                    )
                }

                LeagueChip(league = prospect.tiktokLeague)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${prospect.niche.displayName} · Cadencia: ${prospect.liveCadenceDaysWeek} d/sem",
                style = MaterialTheme.typography.bodySmall.copy(color = Stone)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (prospect.lifecyclePhase == LifecyclePhase.PROSPECCION) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStartContact,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ink,
                            contentColor = PaletteVoid
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Iniciar", modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Iniciar contacto",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }

                    Button(
                        onClick = onOpenWhatToSay,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElevatedSlateVariant,
                            contentColor = Stone
                        ),
                        border = BorderStroke(0.8.dp, CardBorder)
                    ) {
                        Text(
                            text = "Script IA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ElevatedSlateVariant.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "CHECKLIST DE INDUCCIÓN:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Stone,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ChecklistItem(
                        label = "1. Primer contacto realizado",
                        isChecked = prospect.inductionContactDone,
                        onCheckedChange = { onToggleChecklist("contact", it) }
                    )
                    ChecklistItem(
                        label = "2. Charla de bienvenida por WhatsApp/Meet",
                        isChecked = prospect.inductionCallDone,
                        onCheckedChange = { onToggleChecklist("call", it) }
                    )
                    ChecklistItem(
                        label = "3. Reglas y dinámicas explicadas",
                        isChecked = prospect.inductionRulesDone,
                        onCheckedChange = { onToggleChecklist("rules", it) }
                    )
                    ChecklistItem(
                        label = "4. Firma y aceptación de la agencia",
                        isChecked = prospect.inductionSigned,
                        onCheckedChange = { onToggleChecklist("signed", it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTransferToAgency,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_transferir_agencia_${prospect.creatorId}"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = PaletteVoid
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Fichar",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transferir a Cartera (Fichar)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistItem(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isChecked) EmeraldGreen else Mist,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isChecked) TextPrimary else Mist,
                fontSize = 12.sp
            )
        )
    }
}

private fun openWhatsAppRecruiter(context: Context, prospect: Creator) {
    try {
        val clean = prospect.phoneWhatsapp.replace("+", "").replace(" ", "").replace("-", "")
        val msg = "¡Hola ${prospect.name.split(" ").first()}! Vi tu perfil de TikTok LIVE (${prospect.tiktokHandle}) y tu dinámica es genial. En Angel Voice Agency impulsamos streamers de tu nicho a Ligas oficiales con asesoría 1-a-1. ¿Tienes 2 minutos para charlar?"
        val uri = if (clean.isNotBlank()) {
            Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=${Uri.encode(msg)}")
        } else {
            Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(msg)}")
        }
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        // ignore
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    badgeColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Papel else ElevatedSlate.copy(alpha = 0.85f))
            .border(
                0.8.dp,
                if (isSelected) Papel else CardBorder,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badgeColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) PantallaApagada else Stone,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                ),
                maxLines = 1
            )
        }
    }
}