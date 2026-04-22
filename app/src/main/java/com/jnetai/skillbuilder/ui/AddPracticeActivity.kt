package com.jnetai.skillbuilder.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jnetai.skillbuilder.data.Milestone
import com.jnetai.skillbuilder.data.PracticeSession
import com.jnetai.skillbuilder.data.ProgressEntry
import com.jnetai.skillbuilder.data.Skill
import com.jnetai.skillbuilder.databinding.ActivityAddPracticeBinding
import com.jnetai.skillbuilder.reminder.ReminderScheduler
import com.jnetai.skillbuilder.util.SpacedRepetition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AddPracticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPracticeBinding
    private var skillId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Log Practice"

        skillId = intent.getLongExtra("skill_id", -1)
        if (skillId == -1L) {
            finish()
            return
        }

        // Set today's date
        binding.etDate.setText(LocalDate.now().toString())

        // Self-rating slider
        binding.sbSelfRating.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvSelfRatingValue.text = "${progress + 1} / 10"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        binding.tvSelfRatingValue.text = "${binding.sbSelfRating.progress + 1} / 10"

        binding.btnSavePractice.setOnClickListener {
            savePractice()
        }

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun savePractice() {
        val date = binding.etDate.text.toString().trim()
        val durationStr = binding.etDuration.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()
        val selfRating = binding.sbSelfRating.progress + 1

        if (durationStr.isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show()
            return
        }

        val durationMinutes = durationStr.toIntOrNull() ?: 0
        if (durationMinutes <= 0) {
            Toast.makeText(this, "Duration must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database
                val skill = db.skillDao().getSkillById(skillId) ?: return@withContext

                // Save practice session
                val session = PracticeSession(
                    skillId = skillId,
                    date = date,
                    durationMinutes = durationMinutes,
                    notes = notes,
                    selfRating = selfRating
                )
                db.practiceSessionDao().insertSession(session)

                // Calculate new proficiency
                val oldProficiency = skill.currentProficiency
                val newProficiency = SpacedRepetition.calculateNewProficiency(
                    oldProficiency, selfRating, skill.streakDays
                )

                // Update streak
                val today = LocalDate.now()
                val lastPracticed = skill.lastPracticedAt?.let { LocalDate.parse(it) }
                val newStreak = if (lastPracticed != null) {
                    val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(lastPracticed, today).toInt()
                    if (daysDiff <= 1) skill.streakDays + 1 else 1
                } else {
                    1
                }

                // Calculate next practice date
                val nextPractice = SpacedRepetition.getNextPracticeDate(newProficiency, today)

                // Update skill
                val updatedSkill = skill.copy(
                    currentProficiency = newProficiency,
                    lastPracticedAt = today.toString(),
                    nextPracticeDate = nextPractice.toString(),
                    streakDays = newStreak
                )
                db.skillDao().updateSkill(updatedSkill)

                // Save progress entry
                db.progressDao().insertProgress(ProgressEntry(
                    skillId = skillId,
                    proficiency = newProficiency,
                    date = today.toString()
                ))

                // Check for milestone
                val milestoneName = SpacedRepetition.getMilestoneName(oldProficiency, newProficiency)
                if (milestoneName != null) {
                    db.milestoneDao().insertMilestone(Milestone(
                        skillId = skillId,
                        name = milestoneName,
                        description = "Reached level $newProficiency in ${skill.name}",
                        achievedAt = today.toString(),
                        level = newProficiency
                    ))

                    // Show level up notification
                    withContext(Dispatchers.Main) {
                        showMilestoneNotification(skill.name, milestoneName, newProficiency)
                    }
                }

                // Schedule next reminder
                ReminderScheduler.scheduleReminder(applicationContext, updatedSkill)
            }

            Toast.makeText(this@AddPracticeActivity, "Practice logged!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showMilestoneNotification(skillName: String, milestoneName: String, level: Int) {
        val channelName = "Skill Milestones"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "skill_milestones",
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        // Simple toast for now - the reminder system handles proper notifications
        Toast.makeText(
            this,
            "🎉 Level Up! $skillName reached $milestoneName (Level $level)",
            Toast.LENGTH_LONG
        ).show()
    }
}