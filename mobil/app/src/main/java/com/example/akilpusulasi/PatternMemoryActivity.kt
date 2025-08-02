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
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Random
import kotlin.math.sqrt

class PatternMemoryActivity : AppCompatActivity() {

    // --- EXISTING GAME CONSTANTS (No Changes Needed) ---
    companion object {
        const val MIN_ILLUMINATE_DELAY_MS: Long = 600L
        const val MIN_PAUSE_BETWEEN_CELLS_MS: Long = 250L
    }

    // --- GAME STATE VARIABLES ---
    private var currentLevel = 1
    private var gridSize = 3
    private var cellsToIlluminate = 2
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

    // --- BACKEND INTEGRATION VARIABLES ---
    private lateinit var auth: FirebaseAuth
    private var sessionStartTime: Long = 0L

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
            // This button now only starts the very first round of a session.
            fetchAndStartRound(isNewGame = true)
        }
        setupGrid()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun fetchAndStartRound(isNewGame: Boolean) {
        btnStartGame.isEnabled = false
        btnStartGame.text = "Hazırlanıyor..."
        btnStartGame.visibility = if (isNewGame) View.VISIBLE else View.GONE

        lifecycleScope.launch {
            try {
                val currentUser = auth.currentUser ?: throw IllegalStateException("User not logged in.")
                val token = currentUser.getIdToken(true).await().token ?: throw IllegalStateException("Could not get auth token.")
                val authHeader = "Bearer $token"

                val levelToRequestFrom = if (isNewGame) null else currentLevel

                val response = ApiClient.instance.getNewGameParameters(authHeader, levelToRequestFrom)

                if (response.isSuccessful && response.body() != null) {
                    val params = response.body()!!
                    Log.d("PatternMemory", "Received params for level ${params.difficultyLevel}: $params")

                    // Update game state with parameters from the backend
                    currentLevel = params.difficultyLevel
                    cellsToIlluminate = params.patternSize
                    patternDisplayTime = params.displayTimeMs.toLong()

                    val side = sqrt(params.gridSize.toDouble()).toInt()
                    if (gridSize != side) {
                        gridSize = side
                        setupGrid() // Re-create the grid if size changed
                    }

                    startRound()
                } else {
                    if (response.code() == 404) {
                        handleGameWon()
                    } else {
                        Log.e("PatternMemory", "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@PatternMemoryActivity, "Oyun başlatılamadı. Sunucu hatası.", Toast.LENGTH_LONG).show()
                        resetUIForNewGame()
                    }
                }
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 404) {
                    handleGameWon()
                } else {
                    Log.e("PatternMemory", "Exception fetching game params", e)
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
        handler.postDelayed({ generateAndShowPattern() }, 800)
    }

    private fun resetRoundState() {
        canUserClick = false
        userSelection.clear()
        pattern.clear()
        tvInstructions.text = getString(R.string.remember_pattern)
        btnStartGame.visibility = View.GONE
        gridCells.forEach { it.setBackgroundColor(defaultCellColor) }
    }

    private fun sendGameResult(isSuccess: Boolean) {
        val durationSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
        // Score is the completed level number on success, 0 on failure.
        val score = if (isSuccess) currentLevel else 0

        val request = GameSessionCreateRequest(
            gameName = "Pattern Memory",
            score = score,
            durationSeconds = durationSeconds,
            difficultyLevel = currentLevel // Always log the level that was attempted
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val authHeader = "Bearer $token"
                val response = ApiClient.instance.createGameSession(authHeader, request)
                if (response.isSuccessful) {
                    Log.d("PatternMemory", "Game session for level $currentLevel logged successfully with score $score.")
                } else {
                    Log.e("PatternMemory", "Failed to log game session: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PatternMemory", "Exception logging game session", e)
            }
        }
    }

    private fun generateAndShowPattern() {
        isShowingPattern = true
        canUserClick = false
        pattern.clear()
        sessionStartTime = System.currentTimeMillis() // Start timer for this level attempt

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

    private fun checkPattern() {
        canUserClick = false
        val isCorrect = userSelection.sorted() == pattern.sorted()

        if (isCorrect) {
            // --- LOGIC FOR SUCCESS ---
            sendGameResult(isSuccess = true) // Log success to backend
            tvInstructions.text = "Doğru! Sonraki seviye..."
            pattern.forEach { index -> gridCells.getOrNull(index)?.setBackgroundColor(correctCellColor) }

            // --- MODIFIED: Fetch the NEXT level instead of ending the game ---
            handler.postDelayed({
                fetchAndStartRound(isNewGame = false)
            }, 1500)

        } else {
            // This path is now handled by onCellClicked's else block, but as a fallback:
            wrongSequence(userSelection.lastOrNull() ?: 0)
        }
    }

    // This is called when the user clicks the wrong square
    private fun wrongSequence(wrongIndex: Int) {
        canUserClick = false
        isShowingPattern = false

        sendGameResult(isSuccess = false) // Log failure to backend

        tvInstructions.text = "Yanlış! Oyun Bitti."
        gridCells.getOrNull(wrongIndex)?.setBackgroundColor(wrongCellColor)

        // Briefly show the correct pattern
        handler.postDelayed({
            pattern.forEach { index -> gridCells.getOrNull(index)?.setBackgroundColor(correctCellColor) }
        }, 500)

        handler.postDelayed({ resetUIForNewGame() }, 2500)
    }

    // --- NEW: Handle winning the entire game ---
    private fun handleGameWon() {
        tvInstructions.text = "Tebrikler! Tüm seviyeleri tamamladın!"
        tvLevel.visibility = View.VISIBLE // Keep the last level visible
        resetUIForNewGame()
    }

    private fun resetUIForNewGame() {
        gridCells.forEach { it.setBackgroundColor(defaultCellColor) }
        tvInstructions.text = "Tekrar denemeye hazır mısın?"
        btnStartGame.text = getString(R.string.restart_game)
        btnStartGame.visibility = View.VISIBLE
        btnStartGame.isEnabled = true
    }

    // --- UNCHANGED HELPER FUNCTIONS (with minor fixes for safety) ---
    private fun updateLevelText() {
        tvLevel.text = "Seviye: $currentLevel"
    }

    private fun onCellClicked(index: Int) {
        if (!canUserClick || isShowingPattern || index >= gridCells.size) return

        // The core sequence check: Is this the correct next button in the pattern?
        if (index == pattern.getOrNull(userSelection.size)) {
            // Correct tap in the sequence
            userSelection.add(index)
            gridCells[index].setBackgroundColor(selectedCellColor)

            // Did the user just complete the full pattern?
            if (userSelection.size == pattern.size) {
                // Yes, they finished the sequence correctly.
                canUserClick = false // Prevent more clicks
                handler.postDelayed({ levelSuccess() }, 300)
            }
        } else {
            // Wrong tap, the sequence is broken. Game over.
            wrongSequence(index)
        }
    }
    private fun levelSuccess() {
        sendGameResult(isSuccess = true) // Log success
        tvInstructions.text = "Doğru! Sonraki seviye..."
        pattern.forEach { index -> gridCells.getOrNull(index)?.setBackgroundColor(correctCellColor) }

        // Fetch the NEXT level
        handler.postDelayed({
            fetchAndStartRound(isNewGame = false)
        }, 1500)
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
            val startTime = index * (illuminateDuration + 50L) // Small pause
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