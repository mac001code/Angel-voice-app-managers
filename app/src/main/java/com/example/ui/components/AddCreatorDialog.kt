package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Niche
import com.example.data.model.TikTokLeague
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreatorDialog(
    initialIsRecruiterProspect: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        handle: String,
        country: String,
        niche: Niche,
        league: TikTokLeague,
        cadence: Int,
        phone: String,
        isRecruiterProspect: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("VE") }
    var selectedNiche by remember { mutableStateOf(Niche.CONVERSACIONAL) }
    var selectedLeague by remember { mutableStateOf(TikTokLeague.LIGA_D) }
    var cadenceDays by remember { mutableIntStateOf(5) }
    var phone by remember { mutableStateOf("") }
    var isProspect by remember { mutableStateOf(initialIsRecruiterProspect) }

    var nicheExpanded by remember { mutableStateOf(false) }
    var leagueExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(0.8.dp, CardBorder, RoundedCornerShape(20.dp))
                .testTag("dialog_nuevo_creador"),
            color = ElevatedSlate,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Nuevo",
                            tint = ElectricSky,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProspect) "Nuevo Prospecto (Radar)" else "Nuevo Creador (Cartera)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                Text("Nombre Real / Artístico:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Ej. Sofía Méndez", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth().testTag("input_nombre_creador"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricSky,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = ElevatedSlateVariant,
                        unfocusedContainerColor = ElevatedSlateVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // TikTok Handle
                Text("Usuario TikTok (@):", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    placeholder = { Text("@usuario_live", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth().testTag("input_handle_creador"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricSky,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = ElevatedSlateVariant,
                        unfocusedContainerColor = ElevatedSlateVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Country & Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.width(90.dp)) {
                        Text("País (ISO):", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it.take(2).uppercase() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricSky,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ElevatedSlateVariant,
                                unfocusedContainerColor = ElevatedSlateVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("WhatsApp:", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = { Text("+58414...", style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricSky,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ElevatedSlateVariant,
                                unfocusedContainerColor = ElevatedSlateVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Niche Selector
                Text("Categoría / Nicho:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = nicheExpanded,
                    onExpandedChange = { nicheExpanded = !nicheExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedNiche.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nicheExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricSky,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ElevatedSlateVariant,
                            unfocusedContainerColor = ElevatedSlateVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = nicheExpanded,
                        onDismissRequest = { nicheExpanded = false },
                        modifier = Modifier.background(ElevatedSlate)
                    ) {
                        Niche.values().forEach { niche ->
                            DropdownMenuItem(
                                text = { Text(niche.displayName, color = TextPrimary) },
                                onClick = {
                                    selectedNiche = niche
                                    nicheExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // League Selector
                Text("Liga TikTok LIVE:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = leagueExpanded,
                    onExpandedChange = { leagueExpanded = !leagueExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLeague.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leagueExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricSky,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ElevatedSlateVariant,
                            unfocusedContainerColor = ElevatedSlateVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = leagueExpanded,
                        onDismissRequest = { leagueExpanded = false },
                        modifier = Modifier.background(ElevatedSlate)
                    ) {
                        TikTokLeague.values().forEach { league ->
                            DropdownMenuItem(
                                text = { Text(league.displayName, color = TextPrimary) },
                                onClick = {
                                    selectedLeague = league
                                    leagueExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pipeline Phase Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElevatedSlateVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isProspect) "Fase: Prospección Radar" else "Fase: En Cartera Activa",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isProspect) "Asignado al equipo de Reclutamiento" else "Gestionado por Manager de Aceleración",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Switch(
                        checked = isProspect,
                        onCheckedChange = { isProspect = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricSky,
                            checkedTrackColor = ElectricSky.copy(alpha = 0.3f),
                            uncheckedThumbColor = EmeraldGreen,
                            uncheckedTrackColor = EmeraldGreen.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = TextSecondary
                        )
                    ) {
                        Text("Cancelar", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank() && handle.isNotBlank()) {
                                onConfirm(
                                    name,
                                    handle,
                                    country,
                                    selectedNiche,
                                    selectedLeague,
                                    cadenceDays,
                                    phone,
                                    isProspect
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.5f).height(42.dp).testTag("btn_guardar_creador"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricSky,
                            contentColor = DeepCharcoal
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Guardar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Talento", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
