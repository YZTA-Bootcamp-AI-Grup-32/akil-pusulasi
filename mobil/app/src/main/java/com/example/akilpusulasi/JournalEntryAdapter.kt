package com.example.akilpusulasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.Button



class JournalEntryAdapter(private val entries: List<JournalEntry>) :
    RecyclerView.Adapter<JournalEntryAdapter.JournalViewHolder>() {

    class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moodText: TextView = itemView.findViewById(R.id.moodTextView)
        val journalText: TextView = itemView.findViewById(R.id.journalTextView)
        val dateText: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_entry, parent, false)
        return JournalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        val entry = entries[position]
        holder.moodText.text = entry.mood
        holder.journalText.text = entry.text
        holder.dateText.text = entry.date
        holder.itemView.findViewById<Button>(R.id.backToJournalButton).setOnClickListener {
            val intent = Intent(holder.itemView.context, JournalActivity::class.java)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = entries.size
}
