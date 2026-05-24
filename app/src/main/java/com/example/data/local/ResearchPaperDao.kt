package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ResearchPaper
import kotlinx.coroutines.flow.Flow

@Dao
interface ResearchPaperDao {
    @Query("SELECT * FROM research_papers ORDER BY timestamp DESC")
    fun getAllPapers(): Flow<List<ResearchPaper>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: ResearchPaper): Long

    @Query("DELETE FROM research_papers WHERE id = :id")
    suspend fun deletePaper(id: Int)
}
