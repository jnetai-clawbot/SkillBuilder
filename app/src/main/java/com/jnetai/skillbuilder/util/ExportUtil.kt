package com.jnetai.skillbuilder.util

import android.content.Context
import com.jnetai.skillbuilder.data.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object ExportUtil {

    data class SkillExport(
        val skill: Skill,
        val practiceSessions: List<PracticeSession>,
        val progressHistory: List<ProgressEntry>,
        val milestones: List<Milestone>
    )

    data class FullExport(
        val exportDate: String,
        val skills: List<SkillExport>
    )

    suspend fun exportAllData(
        skillDao: SkillDao,
        practiceSessionDao: PracticeSessionDao,
        progressDao: ProgressDao,
        milestoneDao: MilestoneDao
    ): String = withContext(Dispatchers.IO) {
        val skills = skillDao.getAllSkills()
        val exports = skills.map { skill ->
            SkillExport(
                skill = skill,
                practiceSessions = practiceSessionDao.getSessionsForSkill(skill.id),
                progressHistory = progressDao.getProgressForSkill(skill.id),
                milestones = milestoneDao.getMilestonesForSkill(skill.id)
            )
        }

        val fullExport = FullExport(
            exportDate = LocalDate.now().toString(),
            skills = exports
        )

        GsonBuilder().setPrettyPrinting().create().toJson(fullExport)
    }
}