package com.jnetai.skillbuilder.data

import androidx.room.*

@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill): Long

    @Update
    suspend fun updateSkill(skill: Skill)

    @Delete
    suspend fun deleteSkill(skill: Skill)

    @Query("SELECT * FROM skills ORDER BY name ASC")
    fun getAllSkills(): List<Skill>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getSkillById(id: Long): Skill?

    @Query("SELECT * FROM skills WHERE category = :category ORDER BY name ASC")
    fun getSkillsByCategory(category: String): List<Skill>

    @Query("SELECT DISTINCT category FROM skills ORDER BY category ASC")
    fun getAllCategories(): List<String>
}

@Dao
interface PracticeSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSession): Long

    @Delete
    suspend fun deleteSession(session: PracticeSession)

    @Query("SELECT * FROM practice_sessions WHERE skillId = :skillId ORDER BY date DESC")
    fun getSessionsForSkill(skillId: Long): List<PracticeSession>

    @Query("SELECT * FROM practice_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSessionsBetweenDates(startDate: String, endDate: String): List<PracticeSession>

    @Query("SELECT SUM(durationMinutes) FROM practice_sessions WHERE skillId = :skillId")
    fun getTotalMinutesForSkill(skillId: Long): Int?

    @Query("SELECT COUNT(*) FROM practice_sessions WHERE skillId = :skillId")
    fun getSessionCountForSkill(skillId: Long): Int
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(entry: ProgressEntry)

    @Query("SELECT * FROM progress_history WHERE skillId = :skillId ORDER BY date ASC")
    fun getProgressForSkill(skillId: Long): List<ProgressEntry>

    @Query("SELECT * FROM progress_history WHERE skillId = :skillId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestProgress(skillId: Long): ProgressEntry?
}

@Dao
interface MilestoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Query("SELECT * FROM milestones WHERE skillId = :skillId ORDER BY achievedAt DESC")
    fun getMilestonesForSkill(skillId: Long): List<Milestone>

    @Query("SELECT * FROM milestones ORDER BY achievedAt DESC")
    fun getAllMilestones(): List<Milestone>
}