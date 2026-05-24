package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GrantProposal
import kotlinx.coroutines.flow.Flow

@Dao
interface GrantProposalDao {
    @Query("SELECT * FROM grant_proposals ORDER BY timestamp DESC")
    fun getAllProposals(): Flow<List<GrantProposal>>

    @Query("SELECT * FROM grant_proposals WHERE id = :id LIMIT 1")
    fun getProposalById(id: Int): Flow<GrantProposal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: GrantProposal): Long

    @Query("DELETE FROM grant_proposals WHERE id = :id")
    suspend fun deleteProposal(id: Int)
}
