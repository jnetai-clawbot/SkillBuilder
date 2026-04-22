package com.jnetai.skillbuilder.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jnetai.skillbuilder.R
import com.jnetai.skillbuilder.data.PracticeSession
import com.jnetai.skillbuilder.data.Skill
import com.jnetai.skillbuilder.databinding.ActivityStatsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadWeeklyStats()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadWeeklyStats()
    }

    private fun loadWeeklyStats() {
        lifecycleScope.launch {
            val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database

            val weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            val sessions = withContext(Dispatchers.IO) {
                db.practiceSessionDao().getSessionsBetweenDates(weekStart.toString(), weekEnd.toString())
            }

            val skills = withContext(Dispatchers.IO) { db.skillDao().getAllSkills() }

            // Total time
            val totalMinutes = sessions.sumOf { it.durationMinutes }
            binding.tvTotalTime.text = totalMinutes.toString()

            // Total sessions
            binding.tvTotalSessions.text = sessions.size.toString()

            // Unique skills practiced
            val uniqueSkillIds = sessions.map { it.skillId }.toSet()
            binding.tvSkillsPracticed.text = uniqueSkillIds.size.toString()

            // Category breakdown
            binding.categoryStatsContainer.removeAllViews()

            val categoryStats = mutableMapOf<String, CategoryStats>()

            for (session in sessions) {
                val skill = skills.find { it.id == session.skillId } ?: continue
                val stats = categoryStats.getOrPut(skill.category) { CategoryStats() }
                stats.totalMinutes += session.durationMinutes
                stats.sessionCount++
                stats.skillNames.add(skill.name)
            }

            if (categoryStats.isEmpty()) {
                binding.tvNoCategoryStats.visibility = View.VISIBLE
                binding.categoryStatsContainer.visibility = View.GONE
            } else {
                binding.tvNoCategoryStats.visibility = View.GONE
                binding.categoryStatsContainer.visibility = View.VISIBLE

                for ((category, stats) in categoryStats) {
                    val view = layoutInflater.inflate(
                        R.layout.item_category_stat,
                        binding.categoryStatsContainer,
                        false
                    )
                    view.findViewById<TextView>(R.id.tvCategoryName).text = category
                    view.findViewById<TextView>(R.id.tvCategoryTime).text = "${stats.totalMinutes} min"
                    view.findViewById<TextView>(R.id.tvCategorySessions).text =
                        "${stats.sessionCount} sessions · ${stats.skillNames.joinToString(", ")}"
                    binding.categoryStatsContainer.addView(view)
                }
            }
        }
    }

    data class CategoryStats(
        var totalMinutes: Int = 0,
        var sessionCount: Int = 0,
        val skillNames: MutableSet<String> = mutableSetOf()
    )
}