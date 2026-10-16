package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Creator
import com.example.ui.components.HolographicSphereVisualizer
import com.example.ui.components.TopographicTerrainHeader
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.DeepCharcoal
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.ElevatedSlateVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.HoloDotDim
import com.example.ui.theme.HoloDotWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AngelVoiceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Estudio Táctico de Voz y Dictado Independiente.
 * Apartado exclusivo ("un apartado en solo y aparte") para grabar notas de voz,
 * auditar streams en tiempo real con Gemini y persistirlas en Room.
 */
@Composable
fun VoiceStudioScreen(
    viewModel: AngelVoiceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allCreators by viewModel.allCreators.collectAsState()

    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var isTranscribing by remember { mutableStateOf(false) }
    var recordedAudioBytes by remember { mutableStateOf<ByteArray?>(null) }
    var transcribedText by remember { mutableStateOf("") }
    var isLiveAuditType by remember { mutableStateOf(true) }
    var selectedCreator by remember { mutableStateOf<Creator?>(null) }
    var noteSavedMessage by remember { mutableStateOf<String?>(null) }

    var audioRecord: AudioRecord? by remember { mutableStateOf(null) }

    // Auto-select first creator if none selected
    LaunchedEffect(allCreators) {
        if (selectedCreator == null && allCreators.isNotEmpty()) {
            selectedCreator = allCreators.first()
        }
    }

    // Timer while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
        }
    }

    fun startRecordingInternal() {
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            record.startRecording()
            audioRecord = record
            isRecording = true
            transcribedText = ""
            noteSavedMessage = null

            coroutineScope.launch(Dispatchers.IO) {
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(bufferSize)
                while (isRecording && audioRecord != null) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        outputStream.write(buffer, 0, read)
                    }
                }
                recordedAudioBytes = outputStream.toByteArray()
            }
        } catch (e: Exception) {
            isRecording = false
        }
    }

    fun stopRecordingInternal() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            // ignore
        }

        // Process audio with Gemini
        isTranscribing = true
        coroutineScope.launch {
            val bytes = recordedAudioBytes ?: ByteArray(1024)
            viewModel.transcribeAudioRecord(bytes) { text ->
                transcribedText = text
                isTranscribing = false
            }
        }
    }

    fun saveToRoomDatabase() {
        val creator = selectedCreator ?: return
        if (transcribedText.isBlank()) return

        if (isLiveAuditType) {
            viewModel.confirmAuditLive(creator.creatorId, transcribedText, isVoice = true)
        } else {
            viewModel.addBackstageNote(creator.creatorId, transcribedText, isVoice = true)
        }
        noteSavedMessage = "✓ Nota guardada en el perfil de ${creator.name} (${creator.tiktokHandle})"
        transcribedText = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .testTag("pantalla_estudio_voz")
    ) {
        // Topographic Header
        TopographicTerrainHeader(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(ElevatedSlate)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTUDIO DE VOZ // DICTADO TÁCTICO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HoloDotWhite,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dictado de auditorías en vivo y notas backstage",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElevatedSlateVariant)
                            .border(0.6.dp, CardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MIC PRO 16KHZ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HoloDotWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            // Target Streamer Selector (Chips from Room)
            item {
                Column {
                    Text(
                        text = "Vincular a Streamer de TikTok LIVE:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCreators) { creator ->
                            val isSelected = selectedCreator?.creatorId == creator.creatorId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else ElevatedSlateVariant)
                                    .border(
                                        0.6.dp,
                                        if (isSelected) Color.White else CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCreator = creator }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${creator.name} (${creator.tiktokHandle})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.Black else HoloDotWhite,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Note Type Selector: Auditoría en Vivo vs Nota de Gestión
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElevatedSlateVariant)
                        .border(0.6.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLiveAuditType) Color.White else Color.Transparent)
                            .clickable { isLiveAuditType = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Auditoría en Directo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLiveAuditType) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isLiveAuditType) Color.White else Color.Transparent)
                            .clickable { isLiveAuditType = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nota de Gestión Backstage",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (!isLiveAuditType) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Big Central Standalone Recording Hub
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(0.8.dp, CardBorderSubtle, RoundedCornerShape(20.dp)),
                    color = ElevatedSlate
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Timer display
                        Text(
                            text = if (isRecording) {
                                String.format(Locale.US, "%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)
                            } else {
                                "00:00"
                            },
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = if (isRecording) EmeraldGreen else HoloDotWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isRecording) "GRABANDO DICTADO EN VIVO..." else "PULSA PARA INICIAR DICTADO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isRecording) EmeraldGreen else TextMuted,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Minimalist Microphone Orb
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) Color(0xFF16251C) else ElevatedSlateVariant)
                                .border(
                                    2.dp,
                                    if (isRecording) EmeraldGreen else Color.White,
                                    CircleShape
                                )
                                .clickable {
                                    if (isRecording) {
                                        stopRecordingInternal()
                                    } else {
                                        startRecordingInternal()
                                    }
                                }
                                .testTag("btn_grabar_voz_estudio"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRecording) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Detener",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(44.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Grabar",
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Micro Waveform Visualizer
                        VoiceWaveformBar(isRecording = isRecording)
                    }
                }
            }

            // Transcribing Indicator
            if (isTranscribing) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElevatedSlateVariant)
                            .border(0.6.dp, CardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HolographicSphereVisualizer(
                            modifier = Modifier.size(24.dp),
                            statusColor = HoloDotWhite
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TRANSCRIBIENDO AUDIO CON GEMINI 3.5...",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HoloDotWhite,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Procesando audio y extrayendo patrones del directo",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Transcribed Text Result & Save Button
            if (transcribedText.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElevatedSlate)
                            .border(0.8.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "TRANSCRIPCIÓN OBTENIDA:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HoloDotWhite,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = transcribedText,
                            onValueChange = { transcribedText = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = ElevatedSlateVariant,
                                unfocusedContainerColor = ElevatedSlateVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { saveToRoomDatabase() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_guardar_dictado_room"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Guardar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Guardar en Base de Datos Room",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Save Confirmation Banner
            if (noteSavedMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF14241B))
                            .border(0.6.dp, EmeraldGreen, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = noteSavedMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceWaveformBar(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val count = 28
        val spacing = size.width / count.toFloat()
        val centerY = size.height / 2f

        for (i in 0 until count) {
            val h = if (isRecording) {
                val wave = sin((i * 0.45f + phase).toDouble()).toFloat()
                (4.dp.toPx() + wave * 10.dp.toPx()).coerceAtLeast(3.dp.toPx())
            } else {
                3.dp.toPx()
            }
            val x = i.toFloat() * spacing + spacing / 2f
            drawLine(
                color = if (isRecording) Color.White else HoloDotDim,
                start = Offset(x, centerY - h / 2f),
                end = Offset(x, centerY + h / 2f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
