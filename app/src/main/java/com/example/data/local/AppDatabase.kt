package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BackstageNote
import com.example.data.model.Creator
import com.example.data.model.LifecyclePhase
import com.example.data.model.Niche
import com.example.data.model.TikTokLeague
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Creator::class, BackstageNote::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun creatorDao(): CreatorDao
    abstract fun backstageNoteDao(): BackstageNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "angel_voice_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).creatorDao().insertAll(getSeedCreators())
                        }
                    }
                })
                .build()
                INSTANCE = instance

                // Also check if count is zero on launch (e.g. after destructive migration)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        if (instance.creatorDao().getCreatorCount() == 0) {
                            instance.creatorDao().insertAll(getSeedCreators())
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }

                instance
            }
        }

        private fun getSeedCreators(): List<Creator> {
            val now = System.currentTimeMillis()
            val day = 86400000L

            return listOf(
                // 1. En Racha (Verde) - Liga A
                Creator(
                    creatorId = "creator-1",
                    name = "Valentina Rivas",
                    tiktokHandle = "@valen_live_ve",
                    country = "VE",
                    niche = Niche.CONVERSACIONAL,
                    tiktokLeague = TikTokLeague.LIGA_A,
                    liveCadenceDaysWeek = 6,
                    lifecyclePhase = LifecyclePhase.MANAGEMENT,
                    assignedRecruiterName = "Carlos Scouting",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now - (1 * day),
                    lastLiveAuditTimestamp = now - (1 * day),
                    backstageNotesCount = 8,
                    metricsScreenshotsCount = 14,
                    phoneWhatsapp = "+584120001122",
                    diamondsEarned = 148000L,
                    peakViewers = 1850,
                    totalLiveHours = 94.0f,
                    avatarSeed = 1
                ),
                // 2. En Racha (Verde) - Liga B
                Creator(
                    creatorId = "creator-2",
                    name = "Mateo Silva",
                    tiktokHandle = "@mateogaming_mx",
                    country = "MX",
                    niche = Niche.GAMING,
                    tiktokLeague = TikTokLeague.LIGA_B,
                    liveCadenceDaysWeek = 5,
                    lifecyclePhase = LifecyclePhase.MANAGEMENT,
                    assignedRecruiterName = "Carlos Scouting",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now - (2 * day),
                    lastLiveAuditTimestamp = now - (2 * day),
                    backstageNotesCount = 5,
                    metricsScreenshotsCount = 9,
                    phoneWhatsapp = "+525512345678",
                    diamondsEarned = 52000L,
                    peakViewers = 890,
                    totalLiveHours = 52.0f,
                    avatarSeed = 2
                ),
                // 3. Mantenimiento (Amarillo) - Liga C (5 días sin contacto)
                Creator(
                    creatorId = "creator-3",
                    name = "Camila Duarte",
                    tiktokHandle = "@camiladuarte_tv",
                    country = "CO",
                    niche = Niche.TENDENCIAS,
                    tiktokLeague = TikTokLeague.LIGA_C,
                    liveCadenceDaysWeek = 4,
                    lifecyclePhase = LifecyclePhase.MANAGEMENT,
                    assignedRecruiterName = "Lucia Radar",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now - (5 * day),
                    lastLiveAuditTimestamp = now - (4 * day),
                    backstageNotesCount = 3,
                    metricsScreenshotsCount = 4,
                    phoneWhatsapp = "+573009876543",
                    diamondsEarned = 19500L,
                    peakViewers = 420,
                    totalLiveHours = 28.5f,
                    avatarSeed = 3
                ),
                // 4. En Riesgo (Rojo) - Liga B (8 días sin contacto, enfriándose)
                Creator(
                    creatorId = "creator-4",
                    name = "Nicolás 'Nico' V.",
                    tiktokHandle = "@nicovibes_ar",
                    country = "AR",
                    niche = Niche.VLOGS_LIFESTYLE,
                    tiktokLeague = TikTokLeague.LIGA_B,
                    liveCadenceDaysWeek = 5,
                    lifecyclePhase = LifecyclePhase.MANAGEMENT,
                    assignedRecruiterName = "Carlos Scouting",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now - (8 * day),
                    lastLiveAuditTimestamp = now - (7 * day),
                    backstageNotesCount = 2,
                    metricsScreenshotsCount = 3,
                    phoneWhatsapp = "+5491122334455",
                    diamondsEarned = 41000L,
                    peakViewers = 760,
                    totalLiveHours = 46.0f,
                    avatarSeed = 4
                ),
                // 5. En Racha (Verde) - Liga D
                Creator(
                    creatorId = "creator-5",
                    name = "Sofía Méndez",
                    tiktokHandle = "@sofi_charla_es",
                    country = "ES",
                    niche = Niche.CONVERSACIONAL,
                    tiktokLeague = TikTokLeague.LIGA_D,
                    liveCadenceDaysWeek = 3,
                    lifecyclePhase = LifecyclePhase.MANAGEMENT,
                    assignedRecruiterName = "Lucia Radar",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now - (0 * day),
                    lastLiveAuditTimestamp = now - (1 * day),
                    backstageNotesCount = 4,
                    metricsScreenshotsCount = 6,
                    phoneWhatsapp = "+34612345678",
                    diamondsEarned = 8400L,
                    peakViewers = 210,
                    totalLiveHours = 18.0f,
                    avatarSeed = 5
                ),
                // 6. En Riesgo (Rojo) - Liga C (10 días sin contacto)
                Creator(
                    creatorId = "creator-6",
                    name = "Lucas Torres",
                    tiktokHandle = "@lucastorres_cl",
                    country = "CL",
                    niche = Niche.OTROS,
                    tiktokLeague = TikTokLeague.LIGA_C,
                    liveCadenceDaysWeek = 4,
                    lifecyclePhase = LifecyclePhase.MANAGEMENT,
                    assignedRecruiterName = "Lucia Radar",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now - (10 * day),
                    lastLiveAuditTimestamp = now - (9 * day),
                    backstageNotesCount = 1,
                    metricsScreenshotsCount = 2,
                    phoneWhatsapp = "+56987654321",
                    diamondsEarned = 15300L,
                    peakViewers = 310,
                    totalLiveHours = 22.0f,
                    avatarSeed = 6
                ),
                // RECRUITER PROSPECTS (Pipeline de Caza & Scouting)
                // Prospecto en Fase 1: Observación
                Creator(
                    creatorId = "prospect-1",
                    name = "Andrés Galvis",
                    tiktokHandle = "@andres_humor_ve",
                    country = "VE",
                    niche = Niche.TENDENCIAS,
                    tiktokLeague = TikTokLeague.LIGA_D,
                    liveCadenceDaysWeek = 4,
                    lifecyclePhase = LifecyclePhase.PROSPECCION,
                    assignedRecruiterName = "Carlos Scouting",
                    assignedManagerName = "Por Asignar",
                    lastContactTimestamp = now - (1 * day),
                    lastLiveAuditTimestamp = now - (1 * day),
                    phoneWhatsapp = "+584145556677",
                    diamondsEarned = 3200L,
                    peakViewers = 95,
                    totalLiveHours = 12.0f,
                    avatarSeed = 7
                ),
                // Prospecto en Fase 2: Charla / Inducción (Checklist)
                Creator(
                    creatorId = "prospect-2",
                    name = "Estefanía Paz",
                    tiktokHandle = "@estefaniapaz_mx",
                    country = "MX",
                    niche = Niche.CONVERSACIONAL,
                    tiktokLeague = TikTokLeague.LIGA_C,
                    liveCadenceDaysWeek = 5,
                    lifecyclePhase = LifecyclePhase.INDUCCION,
                    assignedRecruiterName = "Carlos Scouting",
                    assignedManagerName = "Por Asignar",
                    lastContactTimestamp = now - (0 * day),
                    lastLiveAuditTimestamp = now - (2 * day),
                    phoneWhatsapp = "+525544332211",
                    inductionContactDone = true,
                    inductionCallDone = true,
                    inductionRulesDone = false,
                    inductionSigned = false,
                    diamondsEarned = 16800L,
                    peakViewers = 380,
                    totalLiveHours = 29.0f,
                    avatarSeed = 8
                ),
                // Prospecto Listo para Fichaje
                Creator(
                    creatorId = "prospect-3",
                    name = "Julián Rojas",
                    tiktokHandle = "@julian_streamer_ar",
                    country = "AR",
                    niche = Niche.GAMING,
                    tiktokLeague = TikTokLeague.LIGA_B,
                    liveCadenceDaysWeek = 6,
                    lifecyclePhase = LifecyclePhase.INDUCCION,
                    assignedRecruiterName = "Lucia Radar",
                    assignedManagerName = "Elena Voice",
                    lastContactTimestamp = now,
                    lastLiveAuditTimestamp = now - (1 * day),
                    phoneWhatsapp = "+5491199887766",
                    inductionContactDone = true,
                    inductionCallDone = true,
                    inductionRulesDone = true,
                    inductionSigned = true,
                    diamondsEarned = 48000L,
                    peakViewers = 820,
                    totalLiveHours = 65.0f,
                    avatarSeed = 9
                )
            )
        }
    }
}
