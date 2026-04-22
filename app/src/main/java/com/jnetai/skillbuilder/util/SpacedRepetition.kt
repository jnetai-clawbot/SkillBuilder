package com.jnetai.skillbuilder.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object SpacedRepetition {

    /**
     * Calculate the next practice date based on the current proficiency level.
     * Lower proficiency = more frequent practice.
     * Uses a simple exponential spacing model.
     */
    fun getNextPracticeDate(currentProficiency: Int, lastPracticedDate: LocalDate?): LocalDate {
        val today = LocalDate.now()

        if (lastPracticedDate == null) {
            return today.plusDays(1)
        }

        val daysSinceLastPractice = ChronoUnit.DAYS.between(lastPracticedDate, today).toInt()

        // Spacing: proficiency 1 = 1 day, proficiency 5 = 3 days, proficiency 10 = 7 days
        val intervalDays = when {
            currentProficiency <= 2 -> 1L
            currentProficiency <= 4 -> 2L
            currentProficiency <= 6 -> 3L
            currentProficiency <= 8 -> 5L
            else -> 7L
        }

        return today.plusDays(intervalDays)
    }

    /**
     * Calculate new proficiency after a practice session.
     * Self-rating and current proficiency determine the adjustment.
     */
    fun calculateNewProficiency(currentProficiency: Int, selfRating: Int, streakDays: Int): Int {
        // Base adjustment from self-rating
        val ratingDelta = when {
            selfRating >= 8 -> 1
            selfRating >= 5 -> 0
            else -> -1
        }

        // Streak bonus
        val streakBonus = when {
            streakDays >= 7 -> 1
            streakDays >= 30 -> 2
            else -> 0
        }

        val newProficiency = currentProficiency + ratingDelta + streakBonus
        return newProficiency.coerceIn(1, 10)
    }

    /**
     * Check if a milestone should be triggered
     */
    fun getMilestoneName(oldProficiency: Int, newProficiency: Int): String? {
        val milestones = mapOf(
            3 to "Getting Started",
            5 to "Intermediate",
            7 to "Proficient",
            9 to "Expert",
            10 to "Mastered"
        )

        for ((level, name) in milestones) {
            if (oldProficiency < level && newProficiency >= level) {
                return name
            }
        }
        return null
    }
}