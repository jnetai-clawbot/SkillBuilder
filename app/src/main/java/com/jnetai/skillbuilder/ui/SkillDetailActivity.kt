package com.jnetai.skillbuilder.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.jnetai.skillbuilder.R
import com.jnetai.skillbuilder.data.Milestone
import com.jnetai.skillbuilder.data.PracticeSession
import com.jnetai.skillbuilder.data.Skill
import com.jnetai.skillbuilder.databinding.ActivitySkillDetailBinding
import com.jnetai.skillbuilder.reminder.ReminderScheduler
import com.jnetai.skillbuilder.util.SpacedRepetition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SkillDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySkillDetailBinding
    private var currentSkill: Skill? = null
    private var skillId: Long = -1
    private lateinit var sessionAdapter: PracticeSessionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        skillId = intent.getLongExtra("skill_id", -1)
        if (skillId == -1L) {
            finish()
            return
        }

        sessionAdapter = PracticeSessionAdapter()
        binding.rvPracticeSessions.layoutManager = LinearLayoutManager(this)
        binding.rvPracticeSessions.adapter = sessionAdapter

        binding.fabAddPractice.setOnClickListener {
            val intent = Intent(this, AddPracticeActivity::class.java)
            intent.putExtra("skill_id", skillId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSkill()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_skill_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                confirmDelete()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Skill")
            .setMessage("Are you sure you want to delete this skill and all its practice history?")
            .setPositiveButton("Delete") { _, _ -> deleteSkill() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSkill() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database
                currentSkill?.let { skill ->
                    ReminderScheduler.cancelReminder(this@SkillDetailActivity, skill.id)
                    db.skillDao().deleteSkill(skill)
                }
            }
            finish()
        }
    }

    private fun loadSkill() {
        lifecycleScope.launch {
            val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database

            val skill = withContext(Dispatchers.IO) { db.skillDao().getSkillById(skillId) }
            if (skill == null) {
                finish()
                return@launch
            }
            currentSkill = skill

            supportActionBar?.title = skill.name

            binding.tvSkillDetailName.text = skill.name
            binding.chipCategory.text = skill.category
            binding.chipDifficulty.text = skill.difficultyLevel
            binding.tvCurrentLevel.text = "Level ${skill.currentProficiency}"
            binding.tvTargetLevel.text = "Target: ${skill.targetProficiency}"
            binding.pbProficiency.max = skill.targetProficiency.coerceAtLeast(1)
            binding.pbProficiency.progress = skill.currentProficiency

            if (skill.streakDays > 0) {
                binding.tvStreak.text = "🔥 ${skill.streakDays} day streak"
            } else {
                binding.tvStreak.text = "No active streak"
            }

            if (skill.nextPracticeDate != null) {
                val nextDate = LocalDate.parse(skill.nextPracticeDate)
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                binding.tvNextPractice.text = "Next practice: ${nextDate.format(formatter)}"
            } else {
                binding.tvNextPractice.text = "No upcoming practice"
            }

            // Load milestones
            val milestones = withContext(Dispatchers.IO) { db.milestoneDao().getMilestonesForSkill(skillId) }
            binding.milestonesContainer.removeAllViews()
            if (milestones.isEmpty()) {
                binding.tvNoMilestones.visibility = View.VISIBLE
            } else {
                binding.tvNoMilestones.visibility = View.GONE
                for (milestone in milestones) {
                    val view = layoutInflater.inflate(R.layout.item_milestone, binding.milestonesContainer, false)
                    view.findViewById<TextView>(R.id.tvMilestoneName).text = milestone.name
                    view.findViewById<TextView>(R.id.tvMilestoneDesc).text = milestone.description
                    view.findViewById<TextView>(R.id.tvMilestoneDate).text = "Level ${milestone.level} — ${milestone.achievedAt}"
                    binding.milestonesContainer.addView(view)
                }
            }

            // Load practice sessions
            val sessions = withContext(Dispatchers.IO) { db.practiceSessionDao().getSessionsForSkill(skillId) }
            if (sessions.isEmpty()) {
                binding.rvPracticeSessions.visibility = View.GONE
                binding.tvNoSessions.visibility = View.VISIBLE
            } else {
                binding.rvPracticeSessions.visibility = View.VISIBLE
                binding.tvNoSessions.visibility = View.GONE
                sessionAdapter.submitList(sessions)
            }
        }
    }
}