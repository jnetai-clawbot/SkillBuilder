package com.jnetai.skillbuilder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.skillbuilder.data.Skill
import com.jnetai.skillbuilder.databinding.ItemSkillBinding

class SkillAdapter(
    private val onClick: (Skill) -> Unit
) : RecyclerView.Adapter<SkillAdapter.ViewHolder>() {

    private var skills: List<Skill> = emptyList()

    fun submitList(newSkills: List<Skill>) {
        skills = newSkills
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSkillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val skill = skills[position]
        holder.bind(skill)
    }

    override fun getItemCount() = skills.size

    inner class ViewHolder(private val binding: ItemSkillBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(skill: Skill) {
            binding.skillName.text = skill.name
            binding.skillCategory.text = skill.category
            binding.difficultyLevel.text = skill.difficultyLevel
            binding.proficiencyText.text = "${skill.currentProficiency}/${skill.targetProficiency}"
            binding.proficiencyBar.progress = skill.currentProficiency
            binding.proficiencyBar.max = skill.targetProficiency.coerceAtLeast(1)

            if (skill.streakDays > 0) {
                binding.streakText.text = "🔥 ${skill.streakDays} day streak"
            } else {
                binding.streakText.text = ""
            }

            binding.root.setOnClickListener { onClick(skill) }
        }
    }
}