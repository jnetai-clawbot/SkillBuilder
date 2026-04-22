package com.jnetai.skillbuilder.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.jnetai.skillbuilder.R
import com.jnetai.skillbuilder.data.Skill
import com.jnetai.skillbuilder.databinding.ActivityMainBinding
import com.jnetai.skillbuilder.util.ExportUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SkillAdapter
    private var allSkills: List<Skill> = emptyList()
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = SkillAdapter { skill ->
            val intent = Intent(this, SkillDetailActivity::class.java)
            intent.putExtra("skill_id", skill.id)
            startActivity(intent)
        }

        binding.skillsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.skillsRecyclerView.adapter = adapter

        binding.fabAddSkill.setOnClickListener {
            startActivity(Intent(this, AddSkillActivity::class.java))
        }

        loadSkills()
        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadSkills()
        loadCategories()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stats -> {
                startActivity(Intent(this, StatsActivity::class.java))
                true
            }
            R.id.action_export -> {
                exportData()
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSkills() {
        lifecycleScope.launch {
            val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database
            allSkills = withContext(Dispatchers.IO) { db.skillDao().getAllSkills() }

            val filtered = if (selectedCategory != null) {
                allSkills.filter { it.category == selectedCategory }
            } else {
                allSkills
            }

            adapter.submitList(filtered)

            binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            binding.skillsRecyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database
            val categories = withContext(Dispatchers.IO) { db.skillDao().getAllCategories() }

            binding.categoryChips.removeAllViews()

            // "All" chip
            val allChip = Chip(this@MainActivity).apply {
                text = "All"
                isCheckable = true
                isChecked = selectedCategory == null
                setOnClickListener {
                    selectedCategory = null
                    loadSkills()
                }
            }
            binding.categoryChips.addView(allChip)

            for (cat in categories) {
                val chip = Chip(this@MainActivity).apply {
                    text = cat
                    isCheckable = true
                    isChecked = cat == selectedCategory
                    setOnClickListener {
                        selectedCategory = if (isChecked) cat else null
                        loadSkills()
                    }
                }
                binding.categoryChips.addView(chip)
            }
        }
    }

    private fun exportData() {
        lifecycleScope.launch {
            val db = (application as com.jnetai.skillbuilder.SkillBuilderApp).database
            val json = ExportUtil.exportAllData(
                db.skillDao(),
                db.practiceSessionDao(),
                db.progressDao(),
                db.milestoneDao()
            )

            withContext(Dispatchers.IO) {
                val file = java.io.File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), "skillbuilder_export.json")
                file.parentFile?.mkdirs()
                file.writeText(json)

                runOnUiThread {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, android.net.Uri.fromFile(file))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareIntent, "Export Skill Data"))
                }
            }
        }
    }
}