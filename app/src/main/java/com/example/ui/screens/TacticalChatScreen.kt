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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.remote.GeminiService
import com.example.ui.components.HolographicSphereVisualizer
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.Mist
import com.example.ui.theme.PaletteVoid
import com.example.ui.theme.PantallaApagada
import com.example.ui.theme.Papel
import com.example.ui.theme.Stone
import com.example.viewmodel.AngelVoiceViewModel
import com.example.viewmodel.ChatMessage

@Composable
fun TacticalChatScreen(
    viewModel: AngelVoiceViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isSending by viewModel.isChatSending.collectAsState()
    val selectedModel by viewModel.selectedChatModel.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("pantalla_asistente_ia")
    ) {
        // Barra superior limpia sin duplicar títulos ni chocar con "En línea"[cite: 3]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
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
                    Text(
                        text = "Mente estratégica",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Ink
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Píldora "En línea" blindada en una sola línea[cite: 3]
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ElevatedSlate)
                        .border(0.8.dp, CardBorder, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Text(
                            text = "En línea",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Stone,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de modelos con contraste corregido
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModelChip(
                    label = "3.5 Flash",
                    isSelected = selectedModel == GeminiService.MODEL_FLASH,
                    onClick = { viewModel.setChatModel(GeminiService.MODEL_FLASH) }
                )

                ModelChip(
                    label = "3.1 Pro",
                    isSelected = selectedModel == GeminiService.MODEL_PRO,
                    onClick = { viewModel.setChatModel(GeminiService.MODEL_PRO) }
                )

                ModelChip(
                    label = "3.1 Lite",
                    isSelected = selectedModel == GeminiService.MODEL_LITE,
                    onClick = { viewModel.setChatModel(GeminiService.MODEL_LITE) }
                )
            }
        }

        // Lista de mensajes con lectura cómoda en Neue Montreal
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (isSending) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(ElevatedSlate.copy(alpha = 0.9f))
                            .border(0.8.dp, CardBorder, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HolographicSphereVisualizer(
                            modifier = Modifier.size(18.dp),
                            statusColor = Papel
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analizando el ecosistema...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Stone)
                        )
                    }
                }
            }
        }

        // Sugerencias rápidas tipo cápsula
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPromptChip("¿Cómo va el ecosistema?") {
                viewModel.sendChatMessage("¿Cómo va la agencia en general según los datos de la base de datos? Dame balance de streamers y diamantes.")
            }
            QuickPromptChip("¿Quiénes están en riesgo?") {
                viewModel.sendChatMessage("¿Quiénes están en riesgo rojo de desmotivación actualmente y cuántos días llevan sin contacto?")
            }
            QuickPromptChip("Estrategia para Liga B") {
                viewModel.sendChatMessage("Dame una táctica de batallas oficiales para los creadores de Liga B para aumentar viewers y donaciones.")
            }
        }

        // Barra de entrada inferior orgánica
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Pregunta sobre creadores, directos o estrategia...",
                        style = MaterialTheme.typography.bodySmall.copy(color = Mist, fontSize = 13.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("input_tactical_chat"),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Papel,
                    unfocusedTextColor = Papel,
                    focusedBorderColor = Stone,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = ElevatedSlate.copy(alpha = 0.9f),
                    unfocusedContainerColor = ElevatedSlate.copy(alpha = 0.9f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isSending) {
                        val text = inputText
                        inputText = ""
                        viewModel.sendChatMessage(text)
                    }
                },
                enabled = inputText.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) Ink else ElevatedSlateVariant)
                    .border(0.8.dp, if (inputText.isNotBlank()) Ink else CardBorder, CircleShape)
                    .testTag("btn_enviar_chat")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (inputText.isNotBlank()) PaletteVoid else Mist,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ModelChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Ink else ElevatedSlate)
            .border(
                0.8.dp,
                if (isSelected) Ink else CardBorder,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) PantallaApagada else Stone,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                fontSize = 11.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun QuickPromptChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(ElevatedSlate.copy(alpha = 0.85f))
            .border(0.8.dp, CardBorder, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Stone,
                fontSize = 11.5.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 315.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
                .background(if (isUser) Ink else ElevatedSlate.copy(alpha = 0.92f))
                .border(
                    0.8.dp,
                    if (isUser) Ink else CardBorder,
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (isUser) PaletteVoid else Ink,
                    lineHeight = 21.sp,
                    fontSize = 14.sp
                )
            )
        }
    }
}