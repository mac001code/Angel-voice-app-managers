package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BackstageNote
import kotlinx.coroutines.flow.Flow

@Dao
interface BackstageNoteDao {
    @Query("SELECT * FROM backstage_notes WHERE creatorId = :creatorId ORDER BY timestamp DESC")
    fun getNotesForCreator(creatorId: String): Flow<List<BackstageNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: BackstageNote)

    @Query("SELECT COUNT(*) FROM backstage_notes")
    suspend fun getTotalNotesCount(): Int
}
