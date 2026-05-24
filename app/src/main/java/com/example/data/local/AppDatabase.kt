package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Profile
import com.example.data.model.InterviewSession
import com.example.data.model.ResearchPaper
import com.example.data.model.GrantProposal
import com.example.data.model.AreaTraining

@Database(
    entities = [
        Profile::class,
        InterviewSession::class,
        ResearchPaper::class,
        GrantProposal::class,
        AreaTraining::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun interviewDao(): InterviewDao
    abstract fun researchPaperDao(): ResearchPaperDao
    abstract fun grantProposalDao(): GrantProposalDao
    abstract fun areaTrainingDao(): AreaTrainingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "facultyaid_db"
                )
                .fallbackToDestructiveMigration() // Supports smooth upgrades that the user mentioned
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
