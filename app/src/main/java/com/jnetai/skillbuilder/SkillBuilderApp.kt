package com.jnetai.skillbuilder

import android.app.Application
import androidx.room.Room
import com.jnetai.skillbuilder.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SkillBuilderApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "skillbuilder-db")
            .build()
    }
}