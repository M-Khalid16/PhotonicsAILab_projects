package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AreaTraining
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaTrainingDao {
    @Query("SELECT * FROM area_trainings ORDER BY timestamp DESC")
    fun getAllTrainings(): Flow<List<AreaTraining>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(training: AreaTraining): Long

    @Query("DELETE FROM area_trainings WHERE id = :id")
    suspend fun deleteTraining(id: Int)
}
