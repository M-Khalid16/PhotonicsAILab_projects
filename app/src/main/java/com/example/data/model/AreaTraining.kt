package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "area_trainings")
data class AreaTraining(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val field: String,
    val title: String,
    val description: String,
    val syllabusMarkdown: String, // Course details, reading list, resources
    val difficultyLevel: String, // "General", "Post-doc", "Senior Researches" etc.
    val timestamp: Long = System.currentTimeMillis()
)
