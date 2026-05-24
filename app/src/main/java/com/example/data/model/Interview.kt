package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InterviewMessage(
    val sender: String, // "AI" or "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "interviews")
data class InterviewSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val date: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val score: Int? = null,
    val generalFeedback: String? = null,
    val serializedMessages: String = "[]" // JSON list of InterviewMessage
)
