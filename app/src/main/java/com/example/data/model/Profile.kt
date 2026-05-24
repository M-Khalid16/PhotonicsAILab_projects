package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val department: String = "",
    val areaOfExpertise: String = "",
    val phdWork: String = "",
    val recentWork: String = ""
)
