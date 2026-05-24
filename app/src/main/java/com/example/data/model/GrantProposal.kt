package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grant_proposals")
data class GrantProposal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAgency: String,
    val researchArea: String,
    val abstractText: String,
    val objectives: String,
    val methodology: String,
    val budgetPlan: String,
    val rawFullProposalText: String,
    val timestamp: Long = System.currentTimeMillis()
)
