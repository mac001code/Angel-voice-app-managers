package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.LifecyclePhase
import com.example.ui.components.CreatorCard
import com.example.ui.components.PyramidalWaveHealthIndicator
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonCoral
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.PaletteVoid
import com.example.ui.theme.PantallaApagada
import com.example.ui.theme.Papel
import com.example.ui.theme.Stone
import com.example.viewmodel.AngelVoiceViewModel

@Composable
fun ManagerScreen(
    viewModel: AngelVoiceViewModel,
    onOpenAddCreator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val creators by viewModel.filteredManagerCreators.collectAsState()
    val allCreators by viewModel.allCreators.collectAsState()
    val healthPercentage by viewModel.circleHealthPercentage.collectAsState()
    val activeFilter by viewModel.managerFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val managers = allCreators.filter { it.lifecyclePhase == LifecyclePhase.MANAGEMENT }
    val redCount = managers.count { it.calculateHealthStatus() == HealthStatus.EN_RIESGO_ROJO }

    var selectedNodeCreator by remember { mutableStateOf<Creator?>(null) }

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Esfera Orgánica Mística Protagonista
            item {
                PyramidalWaveHealthIndicator(
                    healthPercentage = healthPercentage,
                    creators = managers,
                    selectedCreator = selectedNodeCreator,
                    onSelectCreator = { creator -> selectedNodeCreator = creator },
                    onHableHoyClick = { creator -> viewModel.onOneTapHableHoy(creator) },
                    onAuditeLiveClick = { creator -> viewModel.openAuditLiveDialog(creator) },
                    onQueDecirleClick = { creator -> viewModel.openWhatToSaySheet(creator) },
                    onOpenCreatorDetail = { creator -> viewModel.openCreatorDetail(creator) }
                )
            }

            // 2. Encabezado limpio del panel inferior
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Creadores",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Ink,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${managers.size} en órbita",
                        style = MaterialTheme.typography.bodySmall.copy(color = Mist),
                        maxLines = 1
                    )
                }
            }

            // 3. Buscador orgánico tipo cápsula
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Buscar creador o @usuario...",
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
                        .height(50.dp)
                        .testTag("input_buscar_creador"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Stone,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = Papel,
                        unfocusedTextColor = Papel,
                        focusedContainerColor = ElevatedSlate.copy(alpha = 0.85f),
                        unfocusedContainerColor = ElevatedSlate.copy(alpha = 0.85f)
                    ),
                    shape = CircleShape,
                    singleLine = true
                )
            }

            // 4. Píldoras de filtro suaves
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrganicFilterPill(
                        label = "Todos (${managers.size})",
                        isSelected = activeFilter == "TODOS",
                        onClick = { viewModel.setManagerFilter("TODOS") }
                    )

                    OrganicFilterPill(
                        label = "En riesgo ($redCount)",
                        isSelected = activeFilter == "EN_RIESGO",
                        badgeColor = CrimsonCoral,
                        onClick = { viewModel.setManagerFilter("EN_RIESGO") }
                    )

                    OrganicFilterPill(
                        label = "Liga A",
                        isSelected = activeFilter == "LIGA_A",
                        onClick = { viewModel.setManagerFilter("LIGA_A") }
                    )

                    OrganicFilterPill(
                        label = "Liga B",
                        isSelected = activeFilter == "LIGA_B",
                        onClick = { viewModel.setManagerFilter("LIGA_B") }
                    )

                    OrganicFilterPill(
                        label = "Liga C",
                        isSelected = activeFilter == "LIGA_C",
                        onClick = { viewModel.setManagerFilter("LIGA_C") }
                    )

                    OrganicFilterPill(
                        label = "Liga D",
                        isSelected = activeFilter == "LIGA_D",
                        onClick = { viewModel.setManagerFilter("LIGA_D") }
                    )
                }
            }

            // 5. Lista de Creadores
            if (creators.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ElevatedSlate.copy(alpha = 0.7f))
                            .border(0.8.dp, CardBorder, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay creadores que coincidan con la búsqueda.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Mist)
                        )
                    }
                }
            } else {
                items(
                    items = creators,
                    key = { it.creatorId }
                ) { creator ->
                    CreatorCard(
                        creator = creator,
                        onCardClick = {
                            selectedNodeCreator = creator
                            viewModel.openCreatorDetail(creator)
                        },
                        onHableHoyClick = { viewModel.onOneTapHableHoy(creator) },
                        onAuditeLiveClick = { viewModel.openAuditLiveDialog(creator) },
                        onQueDecirleClick = { viewModel.openWhatToSaySheet(creator) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        FloatingActionButton(
            onClick = onOpenAddCreator,
            containerColor = Ink,
            contentColor = PaletteVoid,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_agregar_talento")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar Creador",
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun OrganicFilterPill(
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