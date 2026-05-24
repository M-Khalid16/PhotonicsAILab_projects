package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "research_papers")
data class ResearchPaper(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val authors: String,
    val queryTopic: String,
    val summary: String,
    val bscProjects: String = "",
    val mscProjects: String = "",
    val phdProjects: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
