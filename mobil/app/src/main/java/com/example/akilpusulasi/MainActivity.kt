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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import android.graphics.Color
import com.github.mikephil.charting.utils.Utils
import android.graphics.Paint
import com.github.mikephil.charting.charts.Chart




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
    private lateinit var lineChart: LineChart


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
        lineChart = findViewById(R.id.scoreLineChart)
        lineChart = findViewById(R.id.scoreLineChart)

        drawTestChart()
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
                    Toast.makeText(this@MainActivity, "Oturum bulunamadı.", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }

                val response = ApiClient.instance.getUserStats("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    updateStatsUI(response.body()!!)
                } else {
                    Log.e(
                        "MainActivity",
                        "Error fetching stats: ${response.code()} - ${response.message()}"
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "İstatistikler alınamadı.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Exception fetching stats", e)
                Toast.makeText(
                    this@MainActivity,
                    "Bir hata oluştu: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide progress bar and show the cards
                statsProgressBar.visibility = View.GONE
                gameScoresCard.visibility = View.VISIBLE
                aiAndStreakCard.visibility = View.VISIBLE
            }
        }
    }


    private fun updateStatsUI(stats: UserStatsResponse) {
        // Günlük Serisi metni
        val streakText = if (stats.journalStreak > 0) {
            "🔥 ${stats.journalStreak} günlük seri!"
        } else {
            "Bugün günlüğüne yazarak seriye başla!"
        }
        tvJournalStreak.text = streakText

        // Skorlar kutusu
        scoresLayout.removeAllViews()

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
                    background = ContextCompat.getDrawable(
                        this@MainActivity,
                        R.drawable.circle_blue_gradient
                    )
                    gravity = Gravity.CENTER
                    val size = (48 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins(0, 0, (8 * resources.displayMetrics.density).toInt(), 0)
                    }
                }
                scoresLayout.addView(scoreTextView)
            }
        }

        val entries = stats.lastFiveScores.mapIndexed { index, score ->
            Entry((index + 1).toFloat(), score.toFloat())
        }

        if (entries.isEmpty()) {
            val fakeEntries = listOf(Entry(0f, 0f)) // Sabit boş bir nokta

            val dataSet = LineDataSet(fakeEntries, "").apply {
                color = Color.TRANSPARENT // çizgi görünmesin
                setDrawCircles(false)
                setDrawValues(false)
                setDrawFilled(false)
            }

            val lineData = LineData(dataSet)
            lineChart.data = lineData

            // Ekseni göster ama veri çizme
            lineChart.axisLeft.isEnabled = true
            lineChart.axisRight.isEnabled = false
            lineChart.xAxis.isEnabled = true
            lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            lineChart.xAxis.setDrawGridLines(false)
            lineChart.axisLeft.setDrawGridLines(false)

            // Buraya bunu EKLE:
            lineChart.clear()
            lineChart.setNoDataText("Henüz skor verisi yok. İlk oyunla burası dolacak!")
            lineChart.setNoDataTextColor(Color.parseColor("#6366F1"))
            lineChart.setNoDataTextTypeface(null) // Opsiyonel, varsayılan font
            lineChart.getPaint(Chart.PAINT_INFO).textSize = 14f * resources.displayMetrics.density

            lineChart.invalidate()
        }


    }private fun drawTestChart() {
        val testEntries = listOf(
            Entry(1f, 10f),
            Entry(2f, 15f),
            Entry(3f, 8f),
            Entry(4f, 20f),
            Entry(5f, 17f)
        )

        val dataSet = LineDataSet(testEntries, "Test Skorları").apply {
            color = Color.BLUE
            valueTextSize = 10f
            setDrawFilled(true)
            fillAlpha = 100
            lineWidth = 2f
            setCircleColor(Color.DKGRAY)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.text = "Test Grafiği"
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.invalidate()



    }


}

