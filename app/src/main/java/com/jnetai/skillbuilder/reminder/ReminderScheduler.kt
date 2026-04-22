package com.jnetai.skillbuilder.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.jnetai.skillbuilder.data.Skill
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {

    fun scheduleReminder(context: Context, skill: Skill) {
        val nextDate = skill.nextPracticeDate ?: return
        val practiceDate = LocalDate.parse(nextDate)
        val reminderTime = practiceDate.atTime(9, 0) // 9 AM on practice day

        val now = LocalDateTime.now()
        if (reminderTime.isBefore(now)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("skill_id", skill.id)
            putExtra("skill_name", skill.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            skill.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, skillId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            skillId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}