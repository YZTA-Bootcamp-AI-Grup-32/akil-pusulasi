package com.example.akilpusulasi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.akilpusulasi.network.network.ApiClient
import com.example.akilpusulasi.network.response.GameScoreData
import com.example.akilpusulasi.network.response.UserStatsResponse
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
    private lateinit var scoreChartCard: CardView
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
        scoreChartCard = findViewById(R.id.scoreChartCard)
        lineChart = findViewById(R.id.scoreLineChart)

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
            scoreChartCard.visibility = View.GONE

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
                scoreChartCard.visibility = View.VISIBLE
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

        // Skor Grafiği
        if (stats.lastTenGameStats.isNotEmpty()) {
            setupScoreChart(stats.lastTenGameStats)
        } else {
            // No data, show empty message on chart
            lineChart.clear()
            lineChart.setNoDataText("Henüz skor verisi yok. İlk oyunla burası dolacak!")
            lineChart.setNoDataTextColor(Color.parseColor("#6366F1"))
            lineChart.setNoDataTextTypeface(null)
            lineChart.getPaint(Chart.PAINT_INFO).textSize = 14f * resources.displayMetrics.density
            lineChart.invalidate()
        }
    }

    private fun setupScoreChart(gameStats: List<GameScoreData>) {
        // Data for Score line
        val scoreEntries = gameStats.mapIndexed { index, gameData ->
            Entry(index.toFloat(), gameData.score.toFloat())
        }

        // Data for Level line
        val levelEntries = gameStats.mapIndexed { index, gameData ->
            Entry(index.toFloat(), gameData.level.toFloat())
        }

        // --- Dataset for Scores ---
        val scoreDataSet = LineDataSet(scoreEntries, "Skor").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.blue)
            valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.black)
            setCircleColor(color)
            circleHoleColor = color
            circleRadius = 4f
            lineWidth = 2.5f
            setDrawValues(false) // Don't draw numbers on data points for a cleaner look
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smoother lines
        }

        // --- Dataset for Levels ---
        val levelDataSet = LineDataSet(levelEntries, "Seviye").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.green)
            valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.black)
            setCircleColor(color)
            circleHoleColor = color
            circleRadius = 4f
            lineWidth = 2.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val dataSets = mutableListOf<ILineDataSet>(scoreDataSet, levelDataSet)
        val lineData = LineData(dataSets)
        lineChart.data = lineData

        // --- Chart Customization ---
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.legend.textColor = Color.DKGRAY

        // X-Axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = Color.DKGRAY
        xAxis.axisLineColor = Color.DKGRAY
        // Custom formatter to show "Oyun 1", "Oyun 2", ...
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                // The X-axis shows the index of the game in the list
                return "Oyun ${value.toInt() + 1}"
            }
        }

        // Y-Axis (Left)
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.LTGRAY
        leftAxis.axisMinimum = 0f // Start Y-axis from 0
        leftAxis.textColor = Color.DKGRAY

        // Y-Axis (Right)
        lineChart.axisRight.isEnabled = false

        // Refresh the chart with an animation
        lineChart.animateX(800)
    }
}