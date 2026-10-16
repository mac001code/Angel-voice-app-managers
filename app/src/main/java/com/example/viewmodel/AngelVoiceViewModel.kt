package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BackstageNote
import com.example.data.model.Creator
import com.example.data.model.HealthStatus
import com.example.data.model.LifecyclePhase
import com.example.data.model.Niche
import com.example.data.model.TikTokLeague
import com.example.data.remote.GeminiService
import com.example.data.repository.CreatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppRole {
    MANAGER,
    RECRUITER
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = GeminiService.MODEL_FLASH
)

data class LeaguePromotionEvent(
    val creator: Creator,
    val oldLeague: TikTokLeague,
    val newLeague: TikTokLeague,
    val xpAwarded: Int = 500
)

class AngelVoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = CreatorRepository(db.creatorDao(), db.backstageNoteDao())
    private val geminiService = GeminiService()

    // Shared Preferences for simple persistent goal counter
    private val prefs = application.getSharedPreferences("angel_voice_prefs", Context.MODE_PRIVATE)

    // Current Role (Switcher in Header)
    private val _currentRole = MutableStateFlow(AppRole.MANAGER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Manager Filter
    private val _managerFilter = MutableStateFlow("TODOS")
    val managerFilter: StateFlow<String> = _managerFilter.asStateFlow()

    // Recruiter Filter
    private val _recruiterFilter = MutableStateFlow("TODOS")
    val recruiterFilter: StateFlow<String> = _recruiterFilter.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Daily Goal Tracker
    private val _dailyActionsCompleted = MutableStateFlow(
        prefs.getInt("daily_actions_${getTodayDateKey()}", 2)
    )
    val dailyActionsCompleted: StateFlow<Int> = _dailyActionsCompleted.asStateFlow()
    val dailyTarget: Int = 5

    // Raw creators
    val allCreators: StateFlow<List<Creator>> = repository.allCreators.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Manager Creators with Health Calculation
    val filteredManagerCreators: StateFlow<List<Creator>> = combine(
        allCreators,
        _managerFilter,
        _searchQuery
    ) { creators, filter, query ->
        val list = creators.filter { it.lifecyclePhase == LifecyclePhase.MANAGEMENT }
            .filter { c ->
                if (query.isBlank()) true
                else c.name.contains(query, ignoreCase = true) || c.tiktokHandle.contains(query, ignoreCase = true)
            }

        when (filter) {
            "EN_RIESGO" -> list.filter { it.calculateHealthStatus() == HealthStatus.EN_RIESGO_ROJO }
            "LIGA_A" -> list.filter { it.tiktokLeague == TikTokLeague.LIGA_A }
            "LIGA_B" -> list.filter { it.tiktokLeague == TikTokLeague.LIGA_B }
            "LIGA_C" -> list.filter { it.tiktokLeague == TikTokLeague.LIGA_C }
            "LIGA_D" -> list.filter { it.tiktokLeague == TikTokLeague.LIGA_D }
            else -> list.sortedWith(
                compareBy<Creator> {
                    // Sort En Riesgo first!
                    when (it.calculateHealthStatus()) {
                        HealthStatus.EN_RIESGO_ROJO -> 0
                        HealthStatus.MANTENIMIENTO_AMARILLO -> 1
                        HealthStatus.EN_RACHA_VERDE -> 2
                    }
                }.thenBy { it.lastContactTimestamp }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recruiter Prospects
    val filteredRecruiterProspects: StateFlow<List<Creator>> = combine(
        allCreators,
        _recruiterFilter,
        _searchQuery
    ) { creators, filter, query ->
        val list = creators.filter { it.lifecyclePhase != LifecyclePhase.MANAGEMENT }
            .filter { c ->
                if (query.isBlank()) true
                else c.name.contains(query, ignoreCase = true) || c.tiktokHandle.contains(query, ignoreCase = true)
            }

        when (filter) {
            "OBSERVACION" -> list.filter { it.lifecyclePhase == LifecyclePhase.PROSPECCION }
            "INDUCCION" -> list.filter { it.lifecyclePhase == LifecyclePhase.INDUCCION }
            "INCORPORADO" -> list.filter { it.lifecyclePhase == LifecyclePhase.INCORPORADO }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Health Circle metrics
    val circleHealthPercentage: StateFlow<Int> = allCreators.combine(_currentRole) { list, _ ->
        val managers = list.filter { it.lifecyclePhase == LifecyclePhase.MANAGEMENT }
        if (managers.isEmpty()) return@combine 100
        val greenCount = managers.count { it.calculateHealthStatus() == HealthStatus.EN_RACHA_VERDE }
        ((greenCount.toFloat() / managers.size) * 100).toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 85)

    // Count badges
    val riskCount: StateFlow<Int> = allCreators.combine(_currentRole) { list, _ ->
        list.filter { it.lifecyclePhase == LifecyclePhase.MANAGEMENT }
            .count { it.calculateHealthStatus() == HealthStatus.EN_RIESGO_ROJO }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Bottom Sheet: "Qué decirle" (AI Integrada)
    private val _selectedCreatorForAi = MutableStateFlow<Creator?>(null)
    val selectedCreatorForAi: StateFlow<Creator?> = _selectedCreatorForAi.asStateFlow()

    private val _aiSuggestions = MutableStateFlow<List<String>>(emptyList())
    val aiSuggestions: StateFlow<List<String>> = _aiSuggestions.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Dialog: "Audité LIVE" quick log
    private val _auditCreator = MutableStateFlow<Creator?>(null)
    val auditCreator: StateFlow<Creator?> = _auditCreator.asStateFlow()

    // Creator Detail Sheet
    private val _detailCreator = MutableStateFlow<Creator?>(null)
    val detailCreator: StateFlow<Creator?> = _detailCreator.asStateFlow()

    private val _creatorNotes = MutableStateFlow<List<BackstageNote>>(emptyList())
    val creatorNotes: StateFlow<List<BackstageNote>> = _creatorNotes.asStateFlow()

    // League Promotion Event
    private val _promotionEvent = MutableStateFlow<LeaguePromotionEvent?>(null)
    val promotionEvent: StateFlow<LeaguePromotionEvent?> = _promotionEvent.asStateFlow()

    // Tactical Chat Assistant
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "¡Hola! Soy tu Asistente Táctico de Angel Voice. Estoy listo para guiarte en el crecimiento de tus creadores TikTok LIVE, resolver enfriamientos de streamers o auditar batallas oficiales. ¿Con qué creador o reto arrancamos hoy?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private val _selectedChatModel = MutableStateFlow(GeminiService.MODEL_FLASH)
    val selectedChatModel: StateFlow<String> = _selectedChatModel.asStateFlow()

    // Audio Transcription State
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()

    private val _transcriptionResult = MutableStateFlow<String?>(null)
    val transcriptionResult: StateFlow<String?> = _transcriptionResult.asStateFlow()

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun setManagerFilter(filter: String) {
        _managerFilter.value = filter
    }

    fun setRecruiterFilter(filter: String) {
        _recruiterFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setChatModel(model: String) {
        _selectedChatModel.value = model
    }

    // ONE-TAP ACTIONS
    fun onOneTapHableHoy(creator: Creator) {
        viewModelScope.launch {
            repository.markContactedToday(creator.creatorId)
            incrementDailyAction()
        }
    }

    fun openAuditLiveDialog(creator: Creator) {
        _auditCreator.value = creator
    }

    fun closeAuditLiveDialog() {
        _auditCreator.value = null
    }

    fun confirmAuditLive(creatorId: String, note: String, isVoice: Boolean = false) {
        viewModelScope.launch {
            repository.auditLive(creatorId, note, isVoice)
            incrementDailyAction()
            closeAuditLiveDialog()
        }
    }

    fun openWhatToSaySheet(creator: Creator) {
        _selectedCreatorForAi.value = creator
        _isAiLoading.value = true
        _aiSuggestions.value = emptyList()

        viewModelScope.launch {
            val suggestions = geminiService.getWhatToSaySuggestions(creator)
            _aiSuggestions.value = suggestions
            _isAiLoading.value = false
        }
    }

    fun closeWhatToSaySheet() {
        _selectedCreatorForAi.value = null
        _aiSuggestions.value = emptyList()
    }

    fun openCreatorDetail(creator: Creator) {
        _detailCreator.value = creator
        viewModelScope.launch {
            repository.getNotesForCreator(creator.creatorId).collect {
                _creatorNotes.value = it
            }
        }
    }

    fun closeCreatorDetail() {
        _detailCreator.value = null
        _creatorNotes.value = emptyList()
    }

    fun addBackstageNote(creatorId: String, noteText: String, isVoice: Boolean = false) {
        viewModelScope.launch {
            repository.addBackstageNote(creatorId, noteText, isVoice)
        }
    }

    fun incrementScreenshot(creatorId: String) {
        viewModelScope.launch {
            repository.incrementScreenshot(creatorId)
        }
    }

    fun promoteLeague(creator: Creator) {
        viewModelScope.launch {
            val oldLeague = creator.tiktokLeague
            val newLeague = repository.promoteLeague(creator)
            if (newLeague != null) {
                _promotionEvent.value = LeaguePromotionEvent(
                    creator = creator,
                    oldLeague = oldLeague,
                    newLeague = newLeague
                )
            }
        }
    }

    fun dismissPromotionEvent() {
        _promotionEvent.value = null
    }

    // RECRUITER ACTIONS
    fun updateInductionChecklist(creator: Creator, step: String, isDone: Boolean) {
        viewModelScope.launch {
            repository.updateInductionStep(creator, step, isDone)
        }
    }

    fun transferToAgency(creator: Creator) {
        viewModelScope.launch {
            repository.transferToAgency(creator)
            incrementDailyAction()
            // trigger celebration
            _promotionEvent.value = LeaguePromotionEvent(
                creator = creator,
                oldLeague = TikTokLeague.LIGA_D,
                newLeague = TikTokLeague.LIGA_D,
                xpAwarded = 1000
            )
        }
    }

    fun addNewCreator(
        name: String,
        handle: String,
        country: String,
        niche: Niche,
        league: TikTokLeague,
        cadence: Int,
        phone: String,
        isRecruiterProspect: Boolean
    ) {
        viewModelScope.launch {
            val newCreator = Creator(
                name = name.trim(),
                tiktokHandle = if (handle.startsWith("@")) handle.trim() else "@${handle.trim()}",
                country = country.trim().uppercase(),
                niche = niche,
                tiktokLeague = league,
                liveCadenceDaysWeek = cadence,
                lifecyclePhase = if (isRecruiterProspect) LifecyclePhase.PROSPECCION else LifecyclePhase.MANAGEMENT,
                phoneWhatsapp = phone.trim(),
                lastContactTimestamp = System.currentTimeMillis(),
                lastLiveAuditTimestamp = System.currentTimeMillis()
            )
            repository.insertCreator(newCreator)
            incrementDailyAction()
        }
    }

    // MULTI-TURN CHAT CON CONEXIÓN EN TIEMPO REAL A BASE DE DATOS
    fun buildLiveDatabaseContext(): String {
        val creators = allCreators.value
        val healthPct = circleHealthPercentage.value
        val totalDiamonds = creators.sumOf { it.diamondsEarned }
        val maxViewers = creators.maxOfOrNull { it.peakViewers } ?: 0
        val totalHours = creators.sumOf { it.totalLiveHours.toDouble() }

        val enRacha = creators.filter { it.calculateHealthStatus() == HealthStatus.EN_RACHA_VERDE }
        val mantenimiento = creators.filter { it.calculateHealthStatus() == HealthStatus.MANTENIMIENTO_AMARILLO }
        val enRiesgo = creators.filter { it.calculateHealthStatus() == HealthStatus.EN_RIESGO_ROJO }
        val prospects = creators.filter { it.lifecyclePhase != LifecyclePhase.MANAGEMENT }
        val activePortfolio = creators.filter { it.lifecyclePhase == LifecyclePhase.MANAGEMENT }

        val sb = StringBuilder()
        sb.appendLine("• Total Creadores Registrados en Room: ${creators.size}")
        sb.appendLine("• Cartera Activa de Streamers (Manager): ${activePortfolio.size}")
        sb.appendLine("• Pipeline de Reclutamiento / Scouting: ${prospects.size}")
        sb.appendLine("• Salud Global del Círculo: $healthPct%")
        sb.appendLine("• Métricas TikTok LIVE: $totalDiamonds diamantes totales | Récord Peak Viewers: $maxViewers | Horas transmitidas: ${String.format(java.util.Locale.US, "%.1f", totalHours)}h")
        sb.appendLine()
        sb.appendLine("ESTADO DE ALERTAS:")
        sb.appendLine("- En Racha (${enRacha.size}): " + enRacha.joinToString { "${it.name} (${it.tiktokHandle}, ${it.tiktokLeague.displayName}, ${it.diamondsEarned} diamantes)" })
        sb.appendLine("- En Mantenimiento (${mantenimiento.size} - 4 a 6 días sin contacto): " + mantenimiento.joinToString { "${it.name} (${it.tiktokHandle}, hace ${it.daysSinceContact()} días)" })
        sb.appendLine("- EN RIESGO ROJO (${enRiesgo.size} - >= 7 días sin contacto): " + enRiesgo.joinToString { "¡URGENTE! ${it.name} (${it.tiktokHandle}, Tel: ${it.phoneWhatsapp}, hace ${it.daysSinceContact()} días)" })
        return sb.toString()
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(
            role = "user",
            text = userText.trim(),
            modelUsed = _selectedChatModel.value
        )
        val updatedHistory = _chatMessages.value + userMsg
        _chatMessages.value = updatedHistory
        _isChatSending.value = true

        viewModelScope.launch {
            val historyPairs = updatedHistory.map { it.role to it.text }
            val liveContext = buildLiveDatabaseContext()
            val replyText = geminiService.generateChatReply(historyPairs, _selectedChatModel.value, liveContext)

            val modelMsg = ChatMessage(
                role = "model",
                text = replyText,
                modelUsed = _selectedChatModel.value
            )
            _chatMessages.value = _chatMessages.value + modelMsg
            _isChatSending.value = false
        }
    }

    // AUDIO TRANSCRIPTION WITH GEMINI-3.5-TRANSCRIBE
    fun transcribeAudioRecord(audioBytes: ByteArray, onComplete: (String) -> Unit) {
        _isTranscribing.value = true
        _transcriptionResult.value = null

        viewModelScope.launch {
            val text = geminiService.transcribeAudio(audioBytes)
            _transcriptionResult.value = text
            _isTranscribing.value = false
            onComplete(text)
        }
    }

    private fun incrementDailyAction() {
        val newVal = _dailyActionsCompleted.value + 1
        _dailyActionsCompleted.value = newVal
        prefs.edit().putInt("daily_actions_${getTodayDateKey()}", newVal).apply()
    }

    private fun getTodayDateKey(): String {
        val c = java.util.Calendar.getInstance()
        return "${c.get(java.util.Calendar.YEAR)}_${c.get(java.util.Calendar.DAY_OF_YEAR)}"
    }
}
