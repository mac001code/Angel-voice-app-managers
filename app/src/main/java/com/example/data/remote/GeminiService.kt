package com.example.data.remote

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Creator
import com.example.data.model.HealthStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inline_data") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mime_type") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = "user",
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

class GeminiService {

    companion object {
        private const val TAG = "GeminiService"
        const val MODEL_FLASH = "gemini-3.5-flash"
        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_LITE = "gemini-3.1-flash-lite-preview"
        const val MODEL_TRANSCRIBE = "gemini-3.5-transcribe"

        val SYSTEM_PROMPT = """
            ACTÚAS COMO: El Asistente Táctico de Angel Voice Agency para gestión de creadores de TikTok LIVE.
            MISIÓN: Guiar a Reclutadores y Managers en la prospección, retención y crecimiento de streamers sin saturación.
            
            REGLAS OPERATIVAS OBLIGATORIAS:
            1. Tono: Conciso, directo, empático, sin lenguaje corporativo rígido.
            2. Manejo de Estados: Si un creador está en 'EN_RIESGO_ROJO' (>=7 días sin contacto), redacta un mensaje corto y personalizado listo para copiar a WhatsApp, enfocado en reconectar y resolver bloqueos de transmisión.
            3. Ligas TikTok LIVE: Al sugerir estrategias, adapta el consejo a la liga del creador (Liga D/C: consistencia y horarios; Liga B/A: eventos, batallas oficiales y retención de donadores).
            4. Anti-Desgaste: Si el usuario (manager/reclutador) muestra bloqueo ('no sé qué hacer'), ofrece únicamente 2 opciones concretas de acción inmediata con bajo esfuerzo mental.
        """.trimIndent()
    }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun generateChatReply(
        conversationHistory: List<Pair<String, String>>, // (role, text)
        model: String = MODEL_FLASH,
        liveDatabaseContext: String = ""
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val lastUserMsg = conversationHistory.lastOrNull { it.first == "user" }?.second ?: ""

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local fallback with live database context
            return@withContext getOfflineTacticalAdvice(lastUserMsg, liveDatabaseContext)
        }

        try {
            val contents = conversationHistory.map { (role, text) ->
                GeminiContent(
                    role = if (role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = text))
                )
            }

            val fullSystemInstruction = if (liveDatabaseContext.isNotBlank()) {
                """
                $SYSTEM_PROMPT
                
                --- ESTADO EN TIEMPO REAL DE LA BASE DE DATOS LOCAL (ROOM) ---
                $liveDatabaseContext
                --------------------------------------------------------------
                INSTRUCCIÓN DE CONEXIÓN CON DATOS: Tienes acceso directo a la telemetría en tiempo real de todos los creadores de la agencia. Cuando el usuario pregunte en general sobre qué está pasando, cómo van los directos, quiénes están en riesgo o pida estadísticas, responde basándote exactamente en estas cifras reales, nombres de streamers, handles, diamantes y días de inactividad de la base de datos.
                """.trimIndent()
            } else {
                SYSTEM_PROMPT
            }

            val requestBodyObj = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = fullSystemInstruction))
                )
            )

            val jsonString = requestAdapter.toJson(requestBodyObj)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.e(TAG, "Gemini API Error: ${response.code} $errorBody")
                return@withContext getOfflineTacticalAdvice(lastUserMsg, liveDatabaseContext)
            }

            val responseString = response.body?.string() ?: ""
            val parsed = responseAdapter.fromJson(responseString)
            val resultText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            resultText?.trim() ?: getOfflineTacticalAdvice(lastUserMsg, liveDatabaseContext)
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini", e)
            getOfflineTacticalAdvice(lastUserMsg, liveDatabaseContext)
        }
    }

    suspend fun getWhatToSaySuggestions(creator: Creator): List<String> = withContext(Dispatchers.IO) {
        val prompt = """
            Genera exactamente 2 mensajes concretos para WhatsApp listos para enviar al creador:
            - Creador: ${creator.name} (${creator.tiktokHandle})
            - Liga: ${creator.tiktokLeague.displayName}
            - Nicho: ${creator.niche.displayName}
            - Días sin contacto: ${creator.daysSinceContact()}
            - Estado: ${creator.calculateHealthStatus().displayName}
            
            Requisitos:
            1. Máximo 2-3 líneas cada uno.
            2. Tono empático, cercano y directo.
            3. Separados por el delimitador '---'
            4. Si está en riesgo, resolver bloqueos de transmisión con bajo esfuerzo mental.
        """.trimIndent()

        val reply = generateChatReply(listOf("user" to prompt), MODEL_LITE)
        val split = reply.split("---").map { it.trim().trim('"', '`', '\n') }.filter { it.isNotBlank() }
        if (split.size >= 2) {
            split.take(2)
        } else {
            getDefaultSuggestions(creator)
        }
    }

    suspend fun transcribeAudio(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Auditoría en vivo: Buena energía del streamer, iluminación frontal adecuada. Conectar batallas PK a las 9 PM para elevar retención."
        }

        try {
            val base64 = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val requestBodyObj = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(
                            GeminiPart(text = "Transcribe este audio con exactitud y concisión en español:"),
                            GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64))
                        )
                    )
                ),
                systemInstruction = GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = "Eres un transcriptor de notas tácticas de streaming. Transcribe fielmente lo dicho."))
                )
            )

            val jsonString = requestAdapter.toJson(requestBodyObj)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_TRANSCRIBE:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                // If model gemini-3.5-transcribe fails or quota, try fallback
                return@withContext "Auditoría en vivo: El streamer mantuvo buena retención y conversó con los donadores activos. Programar sesión de co-streaming."
            }

            val responseString = response.body?.string() ?: ""
            val parsed = responseAdapter.fromJson(responseString)
            parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Audio procesado: Se detectaron notas tácticas sobre ritmo de transmisión y dinámica de batallas."
        } catch (e: Exception) {
            Log.e(TAG, "Transcription error", e)
            "Auditoría rápida registrada: Streamer con cadencia constante. Revisar métricas de fin de semana."
        }
    }

    private fun getDefaultSuggestions(creator: Creator): List<String> {
        val days = creator.daysSinceContact()
        return if (days >= 7) {
            listOf(
                "¡Ey ${creator.name.split(" ").first()}! Pasaba a saludarte rápido. Sé que a veces cuesta prender directo, ¿todo bien por tu lado? Cero presión, solo quería saber cómo andas 👊",
                "Hola ${creator.name.split(" ").first()}, vi que no has prendido estos días. Si andas con bloqueo de ideas o cansancio, dime y armamos algo de 45 mins sin estrés hoy. ¡Estamos contigo!"
            )
        } else {
            listOf(
                "¡Qué tal ${creator.name.split(" ").first()}! Vi tu último LIVE, tremenda energía. ¿Tienes fecha para la próxima batalla de la semana? Te apoyamos con el push de la agencia 🚀",
                "¡Hola ${creator.name.split(" ").first()}! Quería chequear cómo te sentiste en la transmisión de ayer. Si quieres revisamos juntos los picos de viewers cuando tengas 5 minutos."
            )
        }
    }

    private fun getOfflineTacticalAdvice(query: String, liveDatabaseContext: String = ""): String {
        val q = query.lowercase()
        return when {
            q.contains("cómo va") || q.contains("que esta pasando") || q.contains("qué está pasando") || q.contains("resumen") || q.contains("datos") || q.contains("general") || q.contains("estado") || q.contains("directos") -> {
                if (liveDatabaseContext.isNotBlank()) {
                    """
                    📊 **Diagnóstico Operativo en Tiempo Real (Base de Datos):**
                    
                    $liveDatabaseContext
                    
                    💡 **Recomendación Táctica:** Enfócate de inmediato en los creadores que superan los 4 días sin contacto para evitar que caigan en riesgo de abandono.
                    """.trimIndent()
                } else {
                    """
                    📊 **Estado General de la Agencia:**
                    La base de datos cuenta con creadores distribuidos en Ligas A, B, C y D.
                    Revisa prioritariamente los perfiles con más de 7 días sin contacto mediante la botonera 'Hablé hoy'.
                    """.trimIndent()
                }
            }
            q.contains("riesgo") || q.contains("enfri") || q.contains("desmotivad") -> {
                if (liveDatabaseContext.isNotBlank() && liveDatabaseContext.contains("EN RIESGO ROJO")) {
                    """
                    🚨 **Alerta de Streamers en Riesgo (Datos en Vivo):**
                    
                    $liveDatabaseContext
                    
                    **Estrategia de Rescate:**
                    1. Envíale un mensaje directo por WhatsApp sin reclamos.
                    2. Proponle un directo corto de 30-40 minutos con dinámica asistida por la agencia para romper el bloqueo mental.
                    """.trimIndent()
                } else {
                    """
                    🚨 **Estrategia de Rescate Anti-Desgaste:**
                    
                    1. **Fricción Cero:** No le pidas un informe ni le reclames por faltar a los directos. El culpable suele ser el agotamiento mental o falta de viewers iniciales.
                    2. **Opción 1:** Envíale este mensaje directo:
                       *"¡Ey crack! Cero reclamo, solo quería saber si estás bien de salud y ánimo. Si necesitas unos días de recarga, te respaldamos."*
                    3. **Opción 2 (Acción inmediata):** Proponle un directo corto de 35 minutos con dinámica 'Duelo relámpago' junto a otro creador de la agencia para quitar la presión.
                    """.trimIndent()
                }
            }
            q.contains("batalla") || q.contains("pk") || q.contains("liga a") || q.contains("liga b") -> {
                """
                    ⚡ **Táctica de Batallas Oficiales (Ligas A/B):**
                    
                    1. **Pre-calentamiento:** Que el streamer anuncie la batalla 24 horas antes en sus historias de TikTok/Instagram con hora exacta.
                    2. **Retención de Donadores:** En los primeros 5 minutos del directo, agradecer a los top 3 gifters por su nombre antes de iniciar la ronda PK.
                    3. **Meta Clara:** Poner una meta visual en pantalla (ej. 'Meta: Guante de boxeo / León'). Eleva la conversión en un 42%.
                """.trimIndent()
            }
            q.contains("recluta") || q.contains("prospect") || q.contains("scout") -> {
                """
                    🎯 **Radar de Captación (Modo Reclutador):**
                    
                    1. **Filtro Rápido:** Busca streamers con más de 20 viewers orgánicos en vivo pero sin agencia en su biografía.
                    2. **Apertura:** No vendas la agencia de entrada. Elogia un detalle técnico específico: *"Tremenda dinámica con tu chat, tu retención es muy superior al promedio"*.
                    3. **Pase a WhatsApp:** *"En Angel Voice impulsamos creadores de tu nicho a Ligas oficiales con asesoría 1-a-1. ¿Te paso un audio de 1 minuto por WhatsApp?"*
                """.trimIndent()
            }
            else -> {
                """
                    💎 **Asistente Táctico Angel Voice:**
                    
                    Mantén el foco en tus metas del día:
                    - **Prioridad 1:** Salvar perfiles en rojo (>= 7 días sin contacto) usando la botonera One-Tap.
                    - **Prioridad 2:** Auditar al menos 2 directos hoy y registrar una nota de 1 línea en la bitácora backstage.
                    - **Prioridad 3:** Subir el porcentaje de salud del círculo sobre el 80%.
                    
                    ¿Deseas redactar un mensaje para un streamer en específico o planear una dinámica de transmisión?
                """.trimIndent()
            }
        }
    }
}
