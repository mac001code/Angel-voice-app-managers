package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.data.model.Creator
import com.example.data.model.LifecyclePhase
import com.example.data.model.TikTokLeague
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad [Creator].
 * Proporciona métodos para insertar, actualizar, eliminar y consultar
 * los creadores de contenido de TikTok LIVE en la base de datos local SQLite (Room).
 */
@Dao
interface CreatorDao {

    // ==========================================
    // 1. MÉTODOS DE INSERCIÓN (CREATE)
    // ==========================================

    /**
     * Inserta un nuevo creador de contenido o reemplaza si ya existe el mismo creatorId.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreator(creator: Creator)

    /**
     * Inserta una lista de creadores en lote.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(creators: List<Creator>)

    /**
     * Inserta o actualiza un creador (Upsert).
     */
    @Upsert
    suspend fun upsertCreator(creator: Creator)

    // ==========================================
    // 2. MÉTODOS DE ACTUALIZACIÓN (UPDATE)
    // ==========================================

    /**
     * Actualiza todos los campos de un creador existente.
     */
    @Update
    suspend fun updateCreator(creator: Creator): Int

    /**
     * Actualiza una lista de creadores.
     */
    @Update
    suspend fun updateCreators(creators: List<Creator>): Int

    /**
     * Registra contacto reciente (Hablé hoy) actualizando el timestamp.
     */
    @Query("UPDATE creators SET lastContactTimestamp = :timestamp WHERE creatorId = :creatorId")
    suspend fun updateLastContact(creatorId: String, timestamp: Long)

    /**
     * Registra auditoría en directo e incrementa el contador de notas backstage.
     */
    @Query("UPDATE creators SET lastLiveAuditTimestamp = :timestamp, backstageNotesCount = backstageNotesCount + 1 WHERE creatorId = :creatorId")
    suspend fun updateLastLiveAudit(creatorId: String, timestamp: Long)

    /**
     * Incrementa el contador de capturas de estadísticas/métricas.
     */
    @Query("UPDATE creators SET metricsScreenshotsCount = metricsScreenshotsCount + 1 WHERE creatorId = :creatorId")
    suspend fun incrementScreenshots(creatorId: String)

    /**
     * Suma diamantes obtenidos en directos de TikTok LIVE.
     */
    @Query("UPDATE creators SET diamondsEarned = diamondsEarned + :diamonds WHERE creatorId = :creatorId")
    suspend fun addDiamonds(creatorId: String, diamonds: Long)

    /**
     * Actualiza el pico de espectadores si el nuevo valor supera al anterior.
     */
    @Query("UPDATE creators SET peakViewers = :peakViewers WHERE creatorId = :creatorId AND peakViewers < :peakViewers")
    suspend fun updatePeakViewers(creatorId: String, peakViewers: Int)

    /**
     * Actualiza la Liga oficial de TikTok LIVE del creador (Ascenso D -> C -> B -> A).
     */
    @Query("UPDATE creators SET tiktokLeague = :newLeague WHERE creatorId = :creatorId")
    suspend fun updateLeague(creatorId: String, newLeague: TikTokLeague)

    /**
     * Actualiza la fase del ciclo de vida del creador (ej. Transferencia a Cartera).
     */
    @Query("UPDATE creators SET lifecyclePhase = :phase WHERE creatorId = :creatorId")
    suspend fun updateLifecyclePhase(creatorId: String, phase: LifecyclePhase)

    // ==========================================
    // 3. MÉTODOS DE ELIMINACIÓN (DELETE)
    // ==========================================

    /**
     * Elimina un creador específico.
     */
    @Delete
    suspend fun deleteCreator(creator: Creator): Int

    /**
     * Elimina un creador por su identificador único.
     */
    @Query("DELETE FROM creators WHERE creatorId = :creatorId")
    suspend fun deleteCreatorById(creatorId: String): Int

    /**
     * Elimina todos los registros de la tabla creadores.
     */
    @Query("DELETE FROM creators")
    suspend fun deleteAllCreators(): Int

    // ==========================================
    // 4. MÉTODOS DE CONSULTA (READ / QUERY)
    // ==========================================

    /**
     * Obtiene todos los creadores ordenados por fecha de último contacto (ascendente).
     */
    @Query("SELECT * FROM creators ORDER BY lastContactTimestamp ASC")
    fun getAllCreators(): Flow<List<Creator>>

    /**
     * Obtiene los creadores filtrados por fase del ciclo de vida (observación, inducción, cartera).
     */
    @Query("SELECT * FROM creators WHERE lifecyclePhase = :phase ORDER BY lastContactTimestamp ASC")
    fun getCreatorsByPhase(phase: LifecyclePhase): Flow<List<Creator>>

    /**
     * Obtiene exclusivamente los creadores en cartera activa (Modo Manager).
     */
    @Query("SELECT * FROM creators WHERE lifecyclePhase = 'MANAGEMENT' ORDER BY lastContactTimestamp ASC")
    fun getManagerCreators(): Flow<List<Creator>>

    /**
     * Obtiene exclusivamente los prospectos del radar de captación (Modo Reclutador).
     */
    @Query("SELECT * FROM creators WHERE lifecyclePhase != 'MANAGEMENT' ORDER BY lastContactTimestamp DESC")
    fun getRecruiterProspects(): Flow<List<Creator>>

    /**
     * Obtiene los creadores pertenecientes a una Liga específica de TikTok LIVE.
     */
    @Query("SELECT * FROM creators WHERE tiktokLeague = :league ORDER BY diamondsEarned DESC")
    fun getCreatorsByLeague(league: TikTokLeague): Flow<List<Creator>>

    /**
     * Busca creadores por coincidencia en nombre o handle de TikTok.
     */
    @Query("SELECT * FROM creators WHERE name LIKE '%' || :query || '%' OR tiktokHandle LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCreators(query: String): Flow<List<Creator>>

    /**
     * Obtiene un creador por su ID único.
     */
    @Query("SELECT * FROM creators WHERE creatorId = :id")
    suspend fun getCreatorById(id: String): Creator?

    /**
     * Obtiene un creador por su handle de TikTok.
     */
    @Query("SELECT * FROM creators WHERE tiktokHandle = :handle LIMIT 1")
    suspend fun getCreatorByHandle(handle: String): Creator?

    /**
     * Devuelve el total de creadores registrados en la base de datos.
     */
    @Query("SELECT COUNT(*) FROM creators")
    suspend fun getCreatorCount(): Int

    /**
     * Devuelve el total de creadores en cartera activa de Management.
     */
    @Query("SELECT COUNT(*) FROM creators WHERE lifecyclePhase = 'MANAGEMENT'")
    suspend fun getManagerCreatorCount(): Int

    /**
     * Devuelve el total de prospectos en proceso de Reclutamiento.
     */
    @Query("SELECT COUNT(*) FROM creators WHERE lifecyclePhase != 'MANAGEMENT'")
    suspend fun getRecruiterProspectCount(): Int
}
