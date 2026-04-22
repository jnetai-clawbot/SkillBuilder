package com.jnetai.skillbuilder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Skill::class, PracticeSession::class, ProgressEntry::class, Milestone::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDao
    abstract fun practiceSessionDao(): PracticeSessionDao
    abstract fun progressDao(): ProgressDao
    abstract fun milestoneDao(): MilestoneDao
}