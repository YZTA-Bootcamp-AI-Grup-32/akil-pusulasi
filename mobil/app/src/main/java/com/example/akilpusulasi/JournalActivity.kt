package com.example.akilpusulasi

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akilpusulasi.network.network.ApiClient
import com.example.akilpusulasi.network.request.DailyJournalCreateRequest
import com.example.akilpusulasi.network.response.DailyJournalResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class JournalActivity : AppCompatActivity() {
    private lateinit var journalEditText: EditText
    private lateinit var saveJournalButton: Button
    private lateinit var currentDateTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    // Mood selection UI (remains client-side for now)
    private lateinit var moodHappy: TextView
    private lateinit var moodSad: TextView
    private lateinit var moodAngry: TextView
    private lateinit var moodTired: TextView

    private var selectedMood: String? = null
    private val journalEntries = mutableListOf<JournalEntry>()
    private lateinit var journalAdapter: JournalEntryAdapter

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        auth = Firebase.auth

        // UI Binding
        setupBottomNavigation(this, "journal")
        currentDateTextView = findViewById(R.id.currentDateText)
        journalEditText = findViewById(R.id.journalEditText)
        saveJournalButton = findViewById(R.id.saveJournalButton)
        recyclerView = findViewById(R.id.journalRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        moodHappy = findViewById(R.id.moodHappy)
        moodSad = findViewById(R.id.moodSad)
        moodAngry = findViewById(R.id.moodAngry)
        moodTired = findViewById(R.id.moodTired)

        // Setup UI
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
        val todayDate = formatter.format(Date())
        currentDateTextView.text = "📅 $todayDate"

        setupMoodSelection()
        setupRecyclerView()

        // Fetch journals from backend
        fetchJournals()

        // Save Button Logic
        saveJournalButton.setOnClickListener {
            val journalText = journalEditText.text.toString().trim()
            val mood = selectedMood ?: "😐"
            val fullEntryText = "$mood $journalText"

            if (journalText.isBlank()) {
                Toast.makeText(this, "Lütfen günlüğüne bir şeyler yaz 💌", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createJournalEntry(fullEntryText)
        }

    }

    private fun fetchJournals() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val token = auth.currentUser?.getIdToken(true)?.await()?.token
                if (token == null) {
                    Toast.makeText(this@JournalActivity, "Oturum açılamadı, lütfen tekrar giriş yapın.", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    return@launch
                }

                val response = ApiClient.instance.getMyJournals("Bearer $token")

                if (response.isSuccessful) {
                    val backendJournals = response.body() ?: emptyList()
                    updateJournalList(backendJournals)
                } else {
                    Log.e("JournalActivity", "Error fetching journals: ${response.code()}")
                    Toast.makeText(this@JournalActivity, "Günlükler alınamadı.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("JournalActivity", "Exception fetching journals", e)
                Toast.makeText(this@JournalActivity, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun createJournalEntry(content: String) {
        // Bu fonksiyon artık iki aşamalı çalışacak
        lifecycleScope.launch {
            // Aşama 1: Anında Kayıt
            saveJournalButton.isEnabled = false
            saveJournalButton.text = "Kaydediliyor..."
            progressBar.visibility = View.VISIBLE

            try {
                val token = auth.currentUser?.getIdToken(true)?.await()?.token
                if (token == null) {
                    Toast.makeText(this@JournalActivity, "Oturum açılamadı.", Toast.LENGTH_LONG).show()
                    resetSaveButton()
                    return@launch
                }

                val request = DailyJournalCreateRequest(journalContent = content)
                // 1. ANLIK KAYIT İSTEĞİ
                val response = ApiClient.instance.createJournal("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    val newJournal = response.body()!!
                    Toast.makeText(this@JournalActivity, "Günlük kaydedildi! 🎉", Toast.LENGTH_SHORT).show()

                    // UI'ı anında güncelle
                    journalEditText.text.clear()
                    resetMoodSelection()
                    addJournalToTop(newJournal) // Yeni günlüğü listeye ekle

                    // 2. ARKA PLAN AI İSTEĞİ
                    launch {
                        generateAiResponseInBackground(token, newJournal.id)
                    }

                } else {
                    Log.e("JournalActivity", "Error creating journal: ${response.code()}")
                    Toast.makeText(this@JournalActivity, "Günlük kaydedilemedi.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("JournalActivity", "Exception creating journal", e)
                Toast.makeText(this@JournalActivity, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Kaydet butonu hemen serbest bırakılır
                resetSaveButton()
            }
        }
    }

    private suspend fun generateAiResponseInBackground(token: String, journalId: String) {
        try {
            Log.d("JournalActivity", "Arka planda AI yanıtı isteniyor: $journalId")
            val response = ApiClient.instance.generateAiResponseForJournal("Bearer $token", journalId)

            if (response.isSuccessful && response.body() != null) {
                val updatedJournal = response.body()!!
                Log.d("JournalActivity", "AI yanıtı alındı: ${updatedJournal.aiResponse}")
                // UI'ı güncellenmiş veriyle yenile
                updateJournalInList(updatedJournal)
            } else {
                Log.e("JournalActivity", "AI yanıtı alınamadı: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("JournalActivity", "AI yanıtı alınırken hata oluştu", e)
        }
    }

    private fun addJournalToTop(newJournal: DailyJournalResponse) {
        val entry = mapResponseToEntry(newJournal)
        journalEntries.add(0, entry)
        journalAdapter.notifyItemInserted(0)
        recyclerView.scrollToPosition(0)
    }

    private fun updateJournalInList(updatedJournal: DailyJournalResponse) {
        val index = journalEntries.indexOfFirst { it.id == updatedJournal.id }
        if (index != -1) {
            journalEntries[index] = mapResponseToEntry(updatedJournal)
            journalAdapter.notifyItemChanged(index)
        }
    }

    private fun updateJournalList(backendJournals: List<DailyJournalResponse>) {
        journalEntries.clear()
        val mappedEntries = backendJournals.map { backendEntry ->
            mapResponseToEntry(backendEntry)
        }
        journalEntries.addAll(mappedEntries)
        journalAdapter.notifyDataSetChanged()
    }

    private fun mapResponseToEntry(backendEntry: DailyJournalResponse): JournalEntry {
        // Tarih formatlama
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr"))
        val date = try {
            inputFormat.parse(backendEntry.createdAt)?.let { outputFormat.format(it) } ?: backendEntry.createdAt
        } catch (e: Exception) {
            backendEntry.createdAt
        }

        return JournalEntry(
            id = backendEntry.id,
            content = backendEntry.journalContent,
            date = date,
            aiResponse = backendEntry.aiResponse
        )
    }

    private fun resetSaveButton() {
        saveJournalButton.isEnabled = true
        saveJournalButton.text = "GÜNLÜĞÜ KAYDET"
        progressBar.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        journalAdapter = JournalEntryAdapter(journalEntries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = journalAdapter
    }

    // --- Mood selection functions remain the same ---
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
}