package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TikTokLeague(val displayName: String, val level: Int) {
    LIGA_D("Liga D (Iniciación)", 1),
    LIGA_C("Liga C (Crecimiento)", 2),
    LIGA_B("Liga B (Consolidado)", 3),
    LIGA_A("Liga A (Top Tier)", 4)
}

enum class Niche(val displayName: String) {
    CONVERSACIONAL("Conversacional / Charla"),
    TENDENCIAS("Tendencias"),
    VLOGS_LIFESTYLE("Vlogs / Lifestyle"),
    GAMING("Gaming"),
    OTROS("Otros")
}

enum class LifecyclePhase(val displayName: String) {
    PROSPECCION("1. Observación"),
    INDUCCION("2. Charla / Inducción"),
    INCORPORADO("3. Incorporado"),
    MANAGEMENT("En Cartera Activa")
}

enum class HealthStatus(val displayName: String) {
    EN_RACHA_VERDE("En Racha"),
    MANTENIMIENTO_AMARILLO("Mantenimiento"),
    EN_RIESGO_ROJO("En Riesgo")
}

@Entity(tableName = "creators")
data class Creator(
    @PrimaryKey
    val creatorId: String = UUID.randomUUID().toString(),
    val name: String,
    val tiktokHandle: String,
    val country: String, // e.g. "VE", "MX", "AR", "CO", "ES"
    val niche: Niche = Niche.CONVERSACIONAL,
    val tiktokLeague: TikTokLeague = TikTokLeague.LIGA_D,
    val liveCadenceDaysWeek: Int = 5,
    val lifecyclePhase: LifecyclePhase = LifecyclePhase.MANAGEMENT,
    val assignedRecruiterName: String = "Reclutador Core",
    val assignedManagerName: String = "Manager Táctico",
    val lastContactTimestamp: Long = System.currentTimeMillis(),
    val lastLiveAuditTimestamp: Long = System.currentTimeMillis() - 86400000L,
    val backstageNotesCount: Int = 0,
    val metricsScreenshotsCount: Int = 0,
    val phoneWhatsapp: String = "",
    val diamondsEarned: Long = 12500L,
    val peakViewers: Int = 450,
    val totalLiveHours: Float = 36.5f,
    val inductionContactDone: Boolean = false,
    val inductionCallDone: Boolean = false,
    val inductionRulesDone: Boolean = false,
    val inductionSigned: Boolean = false,
    val avatarSeed: Int = 1
) {
    fun calculateHealthStatus(now: Long = System.currentTimeMillis()): HealthStatus {
        val daysSinceContact = (now - lastContactTimestamp) / (1000 * 60 * 60 * 24)
        val daysSinceLiveAudit = (now - lastLiveAuditTimestamp) / (1000 * 60 * 60 * 24)

        return when {
            daysSinceContact >= 7 || daysSinceLiveAudit > 5 -> HealthStatus.EN_RIESGO_ROJO
            daysSinceContact in 4..6 -> HealthStatus.MANTENIMIENTO_AMARILLO
            else -> HealthStatus.EN_RACHA_VERDE
        }
    }

    fun daysSinceContact(now: Long = System.currentTimeMillis()): Long {
        val diff = (now - lastContactTimestamp) / (1000 * 60 * 60 * 24)
        return if (diff < 0) 0 else diff
    }

    fun contactFormatted(now: Long = System.currentTimeMillis()): String {
        val days = daysSinceContact(now)
        return when {
            days == 0L -> "Hablé hoy"
            days == 1L -> "Ayer"
            else -> "Hace $days días"
        }
    }
}
