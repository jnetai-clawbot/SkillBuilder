package com.jnetai.skillbuilder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jnetai.skillbuilder.data.PracticeSession
import com.jnetai.skillbuilder.databinding.ItemPracticeSessionBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PracticeSessionAdapter : RecyclerView.Adapter<PracticeSessionAdapter.ViewHolder>() {

    private var sessions: List<PracticeSession> = emptyList()

    fun submitList(newSessions: List<PracticeSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPracticeSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount() = sessions.size

    inner class ViewHolder(private val binding: ItemPracticeSessionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(session: PracticeSession) {
            val date = try {
                LocalDate.parse(session.date).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
            } catch (e: Exception) {
                session.date
            }
            binding.tvSessionDate.text = date
            binding.tvSessionDuration.text = "${session.durationMinutes} min"
            binding.tvSessionRating.text = "Rating: ${session.selfRating}/10"
            if (session.notes.isNotEmpty()) {
                binding.tvSessionNotes.text = session.notes
                binding.tvSessionNotes.visibility = android.view.View.VISIBLE
            } else {
                binding.tvSessionNotes.visibility = android.view.View.GONE
            }
        }
    }
}