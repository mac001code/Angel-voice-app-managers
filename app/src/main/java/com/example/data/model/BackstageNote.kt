package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "backstage_notes")
data class BackstageNote(
    @PrimaryKey
    val noteId: String = UUID.randomUUID().toString(),
    val creatorId: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveAudit: Boolean = true,
    val isVoiceDictated: Boolean = false,
    val metricsAttached: String = ""
)
