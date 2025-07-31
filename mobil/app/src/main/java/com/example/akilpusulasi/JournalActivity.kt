package com.example.akilpusulasi

import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class JournalActivity : AppCompatActivity() {

    private lateinit var journalEditText: EditText
    private lateinit var saveJournalButton: Button
    private lateinit var currentDateTextView: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var moodHappy: TextView
    private lateinit var moodSad: TextView
    private lateinit var moodAngry: TextView
    private lateinit var moodTired: TextView

    private var selectedMood: String? = null
    private val journalEntries = mutableListOf<JournalEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        // 🧭 Alt navigasyon
        setupBottomNavigation(this, "journal")

        // 🗓 Tarih
        currentDateTextView = findViewById(R.id.currentDateText)
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
        val todayDate = formatter.format(Date())
        currentDateTextView.text = "📅 $todayDate"

        // 📝 Görünümler
        journalEditText = findViewById(R.id.journalEditText)
        saveJournalButton = findViewById(R.id.saveJournalButton)
        recyclerView = findViewById(R.id.journalRecyclerView)

        moodHappy = findViewById(R.id.moodHappy)
        moodSad = findViewById(R.id.moodSad)
        moodAngry = findViewById(R.id.moodAngry)
        moodTired = findViewById(R.id.moodTired)

        setupMoodSelection()
        setupRecyclerView()

        // 💾 Kaydetme
        saveJournalButton.setOnClickListener {
            val journalText = journalEditText.text.toString()
            val currentDate = SimpleDateFormat("d MMMM yyyy", Locale("tr")).format(Date())

            if (journalText.isBlank() || selectedMood == null) {
                Toast.makeText(this, "Lütfen duygu ve günlük girin 💌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val entry = JournalEntry(
                text = journalText,
                mood = selectedMood!!,
                date = currentDate
            )

            journalEntries.add(0, entry) // en üste ekle
            recyclerView.adapter?.notifyItemInserted(0)

            Toast.makeText(this, "Günlük kaydedildi! 🎉", Toast.LENGTH_SHORT).show()

            journalEditText.text.clear()
            resetMoodSelection()
        }
    }

    private fun setupMoodSelection() {
        val moodViews = listOf(moodHappy, moodSad, moodAngry, moodTired)

        for (view in moodViews) {
            view.setOnClickListener {
                selectedMood = view.text.toString()
                highlightSelected(view, moodViews)
            }
        }
    }

    private fun highlightSelected(selectedView: TextView, allViews: List<TextView>) {
        for (view in allViews) {
            view.setTypeface(null, Typeface.NORMAL)
            view.setBackgroundColor(0x00000000)
        }
        selectedView.setTypeface(null, Typeface.BOLD)
        selectedView.setBackgroundResource(R.drawable.rounded_edittext)
    }

    private fun resetMoodSelection() {
        selectedMood = null
        val moodViews = listOf(moodHappy, moodSad, moodAngry, moodTired)
        for (view in moodViews) {
            view.setTypeface(null, Typeface.NORMAL)
            view.setBackgroundColor(0x00000000)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = JournalEntryAdapter(journalEntries)
    }
    fun updateJournalEntries(newEntries: List<JournalEntry>) {
        journalEntries.clear()
        journalEntries.addAll(newEntries)
        recyclerView.adapter?.notifyDataSetChanged()
    }

}
