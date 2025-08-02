package com.example.akilpusulasi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.akilpusulasi.network.network.ApiClient
import com.example.akilpusulasi.network.request.GameSessionCreateRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.util.Random
import kotlin.math.sqrt

class PatternMemoryActivity : AppCompatActivity() {

    companion object {
        const val MIN_ILLUMINATE_DELAY_MS: Long = 600L
        const val MIN_PAUSE_BETWEEN_CELLS_MS: Long = 250L
    }

    // --- OTURUM DURUM DEĞİŞKENLERİ ---
    private var sessionStartTime: Long = 0L
    private var highestLevelCompletedInSession: Int = 0
    private var isSessionActive = false

    // --- SEVİYE DURUM DEĞİŞKENLERİ ---
    private var currentLevel = 1
    private var gridSize = 3
    private var cellsToIlluminate = 3
    private var patternDisplayTime: Long = 1200L

    // UI elements
    private lateinit var gridLayout: GridLayout
    private lateinit var tvInstructions: TextView
    private lateinit var tvLevel: TextView
    private lateinit var btnStartGame: Button

    // Game logic
    private var pattern = mutableListOf<Int>()
    private var userSelection = mutableListOf<Int>()
    private var isShowingPattern = false
    private var canUserClick = false
    private val gridCells = mutableListOf<Button>()

    // Colors
    private var defaultCellColor: Int = 0
    private var illuminatedCellColor: Int = 0
    private var selectedCellColor: Int = 0
    private var correctCellColor: Int = 0
    private var wrongCellColor: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_memory)

        auth = Firebase.auth

        gridLayout = findViewById(R.id.grid_layout)
        tvInstructions = findViewById(R.id.tv_instructions)
        tvLevel = findViewById(R.id.tv_level)
        btnStartGame = findViewById(R.id.btn_start_game)

        defaultCellColor = ContextCompat.getColor(this, R.color.gray)
        illuminatedCellColor = ContextCompat.getColor(this, R.color.blue)
        selectedCellColor = ContextCompat.getColor(this, R.color.selected_purple)
        correctCellColor = ContextCompat.getColor(this, R.color.green)
        wrongCellColor = ContextCompat.getColor(this, R.color.red)

        tvLevel.visibility = View.GONE
        btnStartGame.setOnClickListener {
            // Bu buton artık her zaman yeni bir tam oyun oturumu başlatır
            fetchAndStartNewSession()
        }
        setupGrid()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    // Bütün bir oyun oturumunu başlatan ana fonksiyon
    private fun fetchAndStartNewSession() {
        isSessionActive = true
        sessionStartTime = System.currentTimeMillis()
        // Başlangıçta en yüksek tamamlanan seviye 0'dır (veya başlangıç seviyesinden 1 eksik)
        highestLevelCompletedInSession = 0

        // Backend'den en uygun başlangıç seviyesini al
        fetchRoundParameters(fromCompletedLevel = null)
    }

    // Sadece bir sonraki seviyenin parametrelerini getiren fonksiyon
    private fun fetchRoundParameters(fromCompletedLevel: Int?) {
        btnStartGame.isEnabled = false
        btnStartGame.text = "Hazırlanıyor..."
        btnStartGame.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val token = auth.currentUser?.getIdToken(true)?.await()?.token ?: throw IllegalStateException("Token alınamadı.")
                val authHeader = "Bearer $token"

                val response = ApiClient.instance.getNewGameParameters(authHeader, fromCompletedLevel)

                if (response.isSuccessful && response.body() != null) {
                    val params = response.body()!!
                    Log.d("PatternMemory", "Seviye ${params.difficultyLevel} için parametreler alındı: $params")

                    // Oturumun başlangıç seviyesini ayarla (eğer ilk seviye ise)
                    if (fromCompletedLevel == null) {
                        highestLevelCompletedInSession = params.difficultyLevel - 1
                    }

                    // Mevcut seviye durumunu güncelle
                    currentLevel = params.difficultyLevel
                    cellsToIlluminate = params.patternSize
                    patternDisplayTime = params.displayTimeMs.toLong()

                    val side = sqrt(params.gridSize.toDouble()).toInt()
                    if (gridSize != side) {
                        gridSize = side
                        setupGrid()
                    }
                    startRound()
                } else if (response.code() == 404) {
                    // Oyunu tamamen kazandı!
                    handleGameWon()
                } else {
                    Log.e("PatternMemory", "API Hatası: ${response.code()}")
                    Toast.makeText(this@PatternMemoryActivity, "Oyun başlatılamadı.", Toast.LENGTH_LONG).show()
                    resetUIForNewGame()
                }
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 404) {
                    handleGameWon()
                } else {
                    Log.e("PatternMemory", "Parametre alınırken hata oluştu", e)
                    Toast.makeText(this@PatternMemoryActivity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                    resetUIForNewGame()
                }
            }
        }
    }


    private fun startRound() {
        resetRoundState()
        updateLevelText()
        tvLevel.visibility = View.VISIBLE
        btnStartGame.visibility = View.GONE
        handler.postDelayed({ generateAndShowPattern() }, 800)
    }

    private fun resetRoundState() {
        canUserClick = false
        userSelection.clear()
        pattern.clear()
        tvInstructions.text = getString(R.string.remember_pattern)
        gridCells.forEach { it.setBackgroundColor(defaultCellColor) }
    }

    // *** OTURUM SONUNDA ÇAĞIRILACAK TEK KAYIT FONKSİYONU ***
    private fun logFinalGameSession(finalScore: Int, levelFailedAt: Int) {
        if (!isSessionActive) return // Zaten loglandıysa tekrar loglama
        isSessionActive = false

        val totalDurationSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()

        val request = GameSessionCreateRequest(
            gameName = "Pattern Memory",
            score = finalScore, // Oturumdaki en yüksek başarılı seviye
            durationSeconds = totalDurationSeconds, // Toplam süre
            difficultyLevel = levelFailedAt // Hata yapılan seviye
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val authHeader = "Bearer $token"
                ApiClient.instance.createGameSession(authHeader, request)
                Log.d("PatternMemory", "Oturum sonucu loglandı: Skor=$finalScore, Hata Seviyesi=$levelFailedAt")
            } catch (e: Exception) {
                Log.e("PatternMemory", "Oturum loglanırken hata oluştu", e)
            }
        }
    }

    private fun generateAndShowPattern() {
        isShowingPattern = true
        canUserClick = false
        pattern.clear()

        val random = Random()
        val availableIndices = (0 until gridSize * gridSize).toMutableList()
        repeat(cellsToIlluminate) {
            if (availableIndices.isNotEmpty()) {
                val randomIndex = random.nextInt(availableIndices.size)
                pattern.add(availableIndices.removeAt(randomIndex))
            }
        }
        showPatternWithDelay()
    }

    private fun onCellClicked(index: Int) {
        if (!canUserClick || isShowingPattern) return

        userSelection.add(index)
        gridCells[index].setBackgroundColor(selectedCellColor)

        val currentUserSubsequence = userSelection
        val correctSubsequence = pattern.subList(0, userSelection.size)

        if (currentUserSubsequence.sorted() != correctSubsequence.sorted()) {
            wrongSequence(index)
            return
        }

        if (userSelection.size == pattern.size) {
            levelSuccess()
        }
    }

    // Bir seviye başarıyla tamamlandığında çağrılır
    private fun levelSuccess() {
        canUserClick = false

        // Backend'e istek GÖNDERİLMEZ. Sadece client state güncellenir.
        highestLevelCompletedInSession = currentLevel

        tvInstructions.text = "Doğru! Sonraki seviye..."
        pattern.forEach { index -> gridCells.getOrNull(index)?.setBackgroundColor(correctCellColor) }

        // Bir sonraki seviye için parametreleri iste
        handler.postDelayed({
            fetchRoundParameters(fromCompletedLevel = currentLevel)
        }, 1500)
    }

    // Kullanıcı hata yaptığında çağrılır
    private fun wrongSequence(wrongIndex: Int) {
        canUserClick = false
        isShowingPattern = false

        // *** OTURUM BİTTİ, FİNAL SONUCU LOGLA ***
        logFinalGameSession(finalScore = highestLevelCompletedInSession, levelFailedAt = currentLevel)

        tvInstructions.text = "Yanlış! Oyun Bitti."
        gridCells.getOrNull(wrongIndex)?.setBackgroundColor(wrongCellColor)

        handler.postDelayed({
            pattern.forEach { correctIndex ->
                if (correctIndex != wrongIndex) {
                    gridCells.getOrNull(correctIndex)?.setBackgroundColor(correctCellColor)
                }
            }
        }, 500)

        handler.postDelayed({ resetUIForNewGame() }, 2500)
    }

    // Kullanıcı oyunu tamamen kazandığında çağrılır
    private fun handleGameWon() {
        // *** OTURUM BİTTİ, FİNAL SONUCU LOGLA ***
        logFinalGameSession(finalScore = currentLevel, levelFailedAt = currentLevel) // Kazandığı seviye hem skor hem de son seviyedir

        tvInstructions.text = "Tebrikler! Tüm seviyeleri tamamladın!"
        tvLevel.visibility = View.VISIBLE
        resetUIForNewGame()
    }

    // Yeni bir oyuna başlamak için UI'yı sıfırlar
    private fun resetUIForNewGame() {
        isSessionActive = false
        gridCells.forEach { it.setBackgroundColor(defaultCellColor) }
        tvInstructions.text = "Tekrar denemeye hazır mısın?"
        btnStartGame.text = getString(R.string.restart_game)
        btnStartGame.visibility = View.VISIBLE
        btnStartGame.isEnabled = true
    }

    private fun updateLevelText() {
        tvLevel.text = "Seviye: $currentLevel"
    }

    private fun setupGrid() {
        gridLayout.removeAllViews()
        gridCells.clear()
        gridLayout.columnCount = gridSize
        gridLayout.rowCount = gridSize
        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val availableWidth = screenWidth - (64 * density).toInt()
        val minCellSize = (80 * density).toInt()
        val cellSize = maxOf(availableWidth / gridSize, minCellSize)
        for (i in 0 until gridSize * gridSize) {
            val button = Button(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(4, 4, 4, 4)
                }
                setBackgroundColor(defaultCellColor)
                setOnClickListener { onCellClicked(i) }
            }
            gridLayout.addView(button)
            gridCells.add(button)
        }
    }

    private fun showPatternWithDelay() {
        val currentPauseBetweenCells = maxOf(
            patternDisplayTime / (cellsToIlluminate.takeIf { it > 0 } ?: 1),
            MIN_PAUSE_BETWEEN_CELLS_MS
        )
        val illuminateDuration = currentPauseBetweenCells.coerceAtMost(MIN_ILLUMINATE_DELAY_MS)

        pattern.forEachIndexed { index, cellIndex ->
            val startTime = index * (illuminateDuration + 50L)
            handler.postDelayed({ gridCells.getOrNull(cellIndex)?.setBackgroundColor(illuminatedCellColor) }, startTime)
            handler.postDelayed({ gridCells.getOrNull(cellIndex)?.setBackgroundColor(defaultCellColor) }, startTime + illuminateDuration)
        }

        val totalPatternTime = pattern.size * (illuminateDuration + 50L) + 500L
        handler.postDelayed({
            isShowingPattern = false
            canUserClick = true
            tvInstructions.text = getString(R.string.now_click)
        }, totalPatternTime)
    }
}