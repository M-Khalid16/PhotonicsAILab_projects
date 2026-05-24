package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InterviewSession
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interviews ORDER BY date DESC")
    fun getAllInterviews(): Flow<List<InterviewSession>>

    @Query("SELECT * FROM interviews WHERE id = :id LIMIT 1")
    fun getInterviewById(id: Int): Flow<InterviewSession?>

    @Query("SELECT * FROM interviews WHERE id = :id LIMIT 1")
    suspend fun getInterviewByIdOneShot(id: Int): InterviewSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(session: InterviewSession): Long

    @Query("DELETE FROM interviews WHERE id = :id")
    suspend fun deleteInterview(id: Int)
}
