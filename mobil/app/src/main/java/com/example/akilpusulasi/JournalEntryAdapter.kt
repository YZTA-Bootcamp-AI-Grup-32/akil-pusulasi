package com.example.akilpusulasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JournalEntryAdapter(private val entries: List<JournalEntry>) :
    RecyclerView.Adapter<JournalEntryAdapter.JournalViewHolder>() {

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journalText: TextView = itemView.findViewById(R.id.journalTextView)
        val dateText: TextView = itemView.findViewById(R.id.dateTextView)
        val aiResponseLayout: LinearLayout = itemView.findViewById(R.id.aiResponseLayout)
        val aiResponseText: TextView = itemView.findViewById(R.id.aiResponseTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val entry = entries[position]
        holder.journalText.text = entry.content
        holder.dateText.text = entry.date

        if (!entry.aiResponse.isNullOrBlank()) {
            holder.aiResponseText.text = entry.aiResponse
            holder.aiResponseLayout.visibility = View.VISIBLE
        } else {
            holder.aiResponseLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = entries.size
}