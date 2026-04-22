package com.jnetai.skillbuilder.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jnetai.skillbuilder.R
import com.jnetai.skillbuilder.data.Skill
import com.jnetai.skillbuilder.databinding.ActivityAddSkillBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AddSkillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSkillBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSkillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Skill"

        // Setup category dropdown
        val defaultCategories = resources.getStringArray(R.array.default_categories).toList()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, defaultCategories)
        binding.actvCategory.setAdapter(categoryAdapter)

        // Setup difficulty dropdown
        val difficulties = resources.getStringArray(R.array.difficulty_levels).toList()
        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, difficulties)
        binding.actvDifficulty.setAdapter(difficultyAdapter)

        // Setup proficiency slider
        binding.sbTargetProficiency.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvTargetProficiencyValue.text = "${progress + 1} / 10"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        binding.tvTargetProficiencyValue.text = "${binding.sbTargetProficiency.progress + 1} / 10"

        binding.btnSaveSkill.setOnClickListener {
            saveSkill()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun saveSkill() {
        val name = binding.etSkillName.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val difficulty = binding.actvDifficulty.text.toString().trim()
        val targetProficiency = binding.sbTargetProficiency.progress + 1

        if (name.isEmpty()) {
            binding.tilSkillName.error = "Please enter a skill name"
            return
        }
        if (category.isEmpty()) {
            binding.tilCategory.error = "Please select a category"
            return
        }
        if (difficulty.isEmpty()) {
            binding.tilDifficulty.error = "Please select a difficulty"
            return
        }

        val skill = Skill(
            name = name,
            category = category,
            difficultyLevel = difficulty,
            targetProficiency = targetProficiency,
            currentProficiency = 1,
            createdAt = LocalDate.now().toString()
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database
                db.skillDao().insertSkill(skill)
            }
            Toast.makeText(this@AddSkillActivity, "Skill added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}