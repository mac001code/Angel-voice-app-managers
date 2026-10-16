package com.example.data.repository

import com.example.data.local.BackstageNoteDao
import com.example.data.local.CreatorDao
import com.example.data.model.BackstageNote
import com.example.data.model.Creator
import com.example.data.model.HealthStatus
import com.example.data.model.LifecyclePhase
import com.example.data.model.TikTokLeague
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CreatorRepository(
    private val creatorDao: CreatorDao,
    private val backstageNoteDao: BackstageNoteDao
) {
    val allCreators: Flow<List<Creator>> = creatorDao.getAllCreators()
    val managerCreators: Flow<List<Creator>> = creatorDao.getManagerCreators()
    val recruiterProspects: Flow<List<Creator>> = creatorDao.getRecruiterProspects()

    suspend fun markContactedToday(creatorId: String) {
        creatorDao.updateLastContact(creatorId, System.currentTimeMillis())
    }

    suspend fun auditLive(creatorId: String, quickNote: String = "", isVoice: Boolean = false) {
        val now = System.currentTimeMillis()
        creatorDao.updateLastLiveAudit(creatorId, now)
        if (quickNote.isNotBlank()) {
            backstageNoteDao.insertNote(
                BackstageNote(
                    creatorId = creatorId,
                    noteText = quickNote.trim(),
                    timestamp = now,
                    isLiveAudit = true,
                    isVoiceDictated = isVoice
                )
            )
        }
    }

    suspend fun addBackstageNote(creatorId: String, text: String, isVoice: Boolean = false) {
        val now = System.currentTimeMillis()
        backstageNoteDao.insertNote(
            BackstageNote(
                creatorId = creatorId,
                noteText = text.trim(),
                timestamp = now,
                isLiveAudit = false,
                isVoiceDictated = isVoice
            )
        )
    }

    fun getNotesForCreator(creatorId: String): Flow<List<BackstageNote>> {
        return backstageNoteDao.getNotesForCreator(creatorId)
    }

    suspend fun promoteLeague(creator: Creator): TikTokLeague? {
        val nextLeague = when (creator.tiktokLeague) {
            TikTokLeague.LIGA_D -> TikTokLeague.LIGA_C
            TikTokLeague.LIGA_C -> TikTokLeague.LIGA_B
            TikTokLeague.LIGA_B -> TikTokLeague.LIGA_A
            TikTokLeague.LIGA_A -> null
        }

        if (nextLeague != null) {
            val updated = creator.copy(tiktokLeague = nextLeague)
            creatorDao.updateCreator(updated)
            return nextLeague
        }
        return null
    }

    suspend fun transferToAgency(creator: Creator) {
        val updated = creator.copy(
            lifecyclePhase = LifecyclePhase.MANAGEMENT,
            inductionSigned = true,
            lastContactTimestamp = System.currentTimeMillis()
        )
        creatorDao.updateCreator(updated)
    }

    suspend fun updateInductionStep(creator: Creator, step: String, isDone: Boolean) {
        val updated = when (step) {
            "contact" -> creator.copy(inductionContactDone = isDone)
            "call" -> creator.copy(inductionCallDone = isDone)
            "rules" -> creator.copy(inductionRulesDone = isDone)
            "signed" -> creator.copy(inductionSigned = isDone)
            else -> creator
        }
        creatorDao.updateCreator(updated)
    }

    suspend fun insertCreator(creator: Creator) {
        creatorDao.insertCreator(creator)
    }

    suspend fun incrementScreenshot(creatorId: String) {
        creatorDao.incrementScreenshots(creatorId)
    }
}
