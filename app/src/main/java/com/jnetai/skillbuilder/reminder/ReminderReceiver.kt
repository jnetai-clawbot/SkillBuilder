package com.jnetai.skillbuilder.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jnetai.skillbuilder.R

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "skill_reminders"
        private const val CHANNEL_NAME = "Skill Practice Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val skillId = intent.getLongExtra("skill_id", -1)
        val skillName = intent.getStringExtra("skill_name") ?: "Skill"

        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_practice)
            .setContentTitle("Time to practice!")
            .setContentText("It's time to practice $skillName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(skillId.toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to practice your skills"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}