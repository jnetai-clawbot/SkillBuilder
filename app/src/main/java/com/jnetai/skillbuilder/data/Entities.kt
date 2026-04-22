package com.jnetai.skillbuilder.data

import androidx.room.*
import java.time.LocalDate

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val difficultyLevel: String, // Beginner, Intermediate, Advanced, Expert
    val targetProficiency: Int, // 1-10
    val currentProficiency: Int, // 1-10
    val createdAt: String, // ISO date string
    val lastPracticedAt: String? = null,
    val nextPracticeDate: String? = null,
    val streakDays: Int = 0
)

@Entity(tableName = "practice_sessions")
data class PracticeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillId: Long,
    val date: String, // ISO date string
    val durationMinutes: Int,
    val notes: String,
    val selfRating: Int, // 1-10
    val createdAt: String = LocalDate.now().toString()
)

@Entity(tableName = "progress_history")
data class ProgressEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillId: Long,
    val proficiency: Int, // 1-10
    val date: String // ISO date string
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillId: Long,
    val name: String,
    val description: String,
    val achievedAt: String, // ISO date string
    val level: Int // proficiency level achieved
)