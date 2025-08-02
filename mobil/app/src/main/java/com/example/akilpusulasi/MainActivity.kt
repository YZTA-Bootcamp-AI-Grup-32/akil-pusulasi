package com.example.akilpusulasi

import android.R.attr.textStyle
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button // Import Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.akilpusulasi.network.network.ApiClient
import com.example.akilpusulasi.network.response.UserStatsResponse
import com.google.firebase.auth.ktx.auth // Import auth
import com.google.firebase.ktx.Firebase // Import Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private lateinit var btnHome: LinearLayout
    private lateinit var btnGame: LinearLayout
    private lateinit var btnJournal: LinearLayout
    private lateinit var btnLogout: Button

    // --- UI-element variables ---
    private lateinit var statsProgressBar: ProgressBar
    private lateinit var tvJournalStreak: TextView
    private lateinit var scoresLayout: LinearLayout
    // --- New visibility controls ---
    private lateinit var gameScoresCard: CardView
    private lateinit var aiAndStreakCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation(this, "home")

        // --- Bind UI elements ---
        btnHome = findViewById(R.id.btnHome)
        btnGame = findViewById(R.id.btnGame)
        btnJournal = findViewById(R.id.btnJournal)
        btnLogout = findViewById(R.id.btnLogout)
        statsProgressBar = findViewById(R.id.statsProgressBar)
        tvJournalStreak = findViewById(R.id.tvJournalStreak)
        scoresLayout = findViewById(R.id.scoresLayout)
        gameScoresCard = findViewById(R.id.gameScoresCard)
        aiAndStreakCard = findViewById(R.id.aiAndStreakCard)

        setSelectedButton(btnHome)

        btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }

        btnHome.setOnClickListener {
            setSelectedButton(btnHome)
        }

        btnGame.setOnClickListener {
            setSelectedButton(btnGame)
            startActivity(Intent(this, GameActivity::class.java))
        }

        btnJournal.setOnClickListener {
            setSelectedButton(btnJournal)
            startActivity(Intent(this, JournalActivity::class.java))
        }

        fetchUserStats()
    }

    private fun setSelectedButton(selected: LinearLayout) {
        btnHome.isSelected = false
        btnGame.isSelected = false
        btnJournal.isSelected = false
        selected.isSelected = true
    }

    private fun fetchUserStats() {
        lifecycleScope.launch {
            // Show progress bar and hide the cards
            statsProgressBar.visibility = View.VISIBLE
            gameScoresCard.visibility = View.GONE
            aiAndStreakCard.visibility = View.GONE

            try {
                val token = Firebase.auth.currentUser?.getIdToken(true)?.await()?.token
                if (token == null) {
                    Toast.makeText(this@MainActivity, "Oturum bulunamadı.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = ApiClient.instance.getUserStats("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    updateStatsUI(response.body()!!)
                } else {
                    Log.e("MainActivity", "Error fetching stats: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@MainActivity, "İstatistikler alınamadı.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Exception fetching stats", e)
                Toast.makeText(this@MainActivity, "Bir hata oluştu: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Hide progress bar and show the cards
                statsProgressBar.visibility = View.GONE
                gameScoresCard.visibility = View.VISIBLE
                aiAndStreakCard.visibility = View.VISIBLE
            }
        }
    }

    private fun updateStatsUI(stats: UserStatsResponse) {
        // Update Journal Streak (in the bottom card)
        val streakText = if (stats.journalStreak > 0) {
            "🔥 ${stats.journalStreak} günlük seri!"
        } else {
            "Bugün günlüğüne yazarak seriye başla!"
        }
        tvJournalStreak.text = streakText

        // Update Last 5 Scores (in the top card)
        scoresLayout.removeAllViews() // Clear previous scores if any

        if (stats.lastFiveScores.isEmpty()) {
            val noDataTextView = TextView(this).apply {
                text = "Henüz oyun oynamadın."
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.gray))
                textSize = 14f
            }
            scoresLayout.addView(noDataTextView)
        } else {
            stats.lastFiveScores.forEach { score ->
                val scoreTextView = TextView(this).apply {
                    text = score.toString()
                    textSize = 18f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                    background = ContextCompat.getDrawable(this@MainActivity, R.drawable.circle_blue_gradient)
                    gravity = Gravity.CENTER
                    val size = (48 * resources.displayMetrics.density).toInt() // 48dp
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(0, 0, (8 * resources.displayMetrics.density).toInt(), 0) // 8dp margin right
                    }
                }
                scoresLayout.addView(scoreTextView)
            }
        }
    }
}