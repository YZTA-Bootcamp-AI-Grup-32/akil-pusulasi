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
import java.util.Random
import kotlin.math.sqrt

class PatternMemoryActivity : AppCompatActivity() {

    // --- EXISTING GAME CONSTANTS (No Changes Needed) ---
    companion object {
        const val MIN_ILLUMINATE_DELAY_MS: Long = 600L
        const val MIN_PAUSE_BETWEEN_CELLS_MS: Long = 250L
    }

    // --- MODIFIED & NEW GAME STATE VARIABLES ---
    private var currentLevel = 1
    private var gridSize = 3 // Will be updated by API
    private var cellsToIlluminate = 2 // Will be updated by API

    // UI elements
    private lateinit var gridLayout: GridLayout
    private lateinit var tvInstructions: TextView
    private lateinit var tvLevel: TextView
    private lateinit var btnStartGame: Button

    // Game logic data structures
    private var pattern = mutableListOf<Int>()
    private var userSelection = mutableListOf<Int>()

    // Game state flags
    private var isShowingPattern = false
    private var canUserClick = false

    private val gridCells = mutableListOf<Button>()

    // Color references
    private var defaultCellColor: Int = 0
    private var illuminatedCellColor: Int = 0
    private var selectedCellColor: Int = 0
    private var correctCellColor: Int = 0
    private var wrongCellColor: Int = 0

    private val handler = Handler(Looper.getMainLooper())

    // --- NEW VARIABLES FOR BACKEND INTEGRATION ---
    private lateinit var auth: FirebaseAuth
    private var currentDifficultyLevel: Int = 1
    private var sessionStartTime: Long = 0L // To calculate duration
    private var patternDisplayTime: Long = 1200L // Will be updated by API


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_memory)

        // --- NEW ---
        auth = Firebase.auth

        // UI element binding (No changes)
        gridLayout = findViewById(R.id.grid_layout)
        tvInstructions = findViewById(R.id.tv_instructions)
        tvLevel = findViewById(R.id.tv_level)
        btnStartGame = findViewById(R.id.btn_start_game)

        // Color references (No changes)
        defaultCellColor = ContextCompat.getColor(this, R.color.gray)
        illuminatedCellColor = ContextCompat.getColor(this, R.color.blue)
        selectedCellColor = ContextCompat.getColor(this, R.color.selected_purple)
        correctCellColor = ContextCompat.getColor(this, R.color.green)
        wrongCellColor = ContextCompat.getColor(this, R.color.red)

        tvLevel.visibility = View.GONE
        btnStartGame.setOnClickListener {
            // startGame() is now replaced with fetch and start
            fetchGameParametersAndStart()
        }
        setupGrid() // Initial setup for display
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    // --- MODIFIED: This is now the main entry point for starting a game ---
    private fun fetchGameParametersAndStart() {
        btnStartGame.isEnabled = false
        btnStartGame.text = "Hazırlanıyor..."

        lifecycleScope.launch {
            try {
                val currentUser = auth.currentUser
                    ?: throw IllegalStateException("User not logged in.")
                val token = currentUser.getIdToken(true).await().token
                    ?: throw IllegalStateException("Could not get auth token.")

                val authHeader = "Bearer $token"
                val response = ApiClient.instance.getNewGameParameters(authHeader)

                if (response.isSuccessful && response.body() != null) {
                    val params = response.body()!!
                    Log.d("PatternMemory", "Received params: $params")

                    // Update game state with parameters from the backend
                    currentDifficultyLevel = params.difficultyLevel
                    currentLevel = params.difficultyLevel // Sync local level with backend difficulty
                    cellsToIlluminate = params.patternSize
                    patternDisplayTime = params.displayTimeMs.toLong()

                    // The backend sends total cells (9, 16), we need the side length (3, 4)
                    val side = sqrt(params.gridSize.toDouble()).toInt()
                    if (gridSize != side) {
                        gridSize = side
                        setupGrid() // Re-create the grid if size changed
                    }

                    // Now start the actual game round
                    startRound()

                } else {
                    Log.e("PatternMemory", "API Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@PatternMemoryActivity, "Oyun başlatılamadı. Sunucu hatası.", Toast.LENGTH_LONG).show()
                    resetUIForNewGame()
                }

            } catch (e: Exception) {
                Log.e("PatternMemory", "Exception fetching game params", e)
                Toast.makeText(this@PatternMemoryActivity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                resetUIForNewGame()
            }
        }
    }

    // --- NEW: This function handles starting a single round/level ---
    private fun startRound() {
        resetRoundState()
        updateLevelText()
        tvLevel.visibility = View.VISIBLE
        // Short delay before showing the pattern
        handler.postDelayed({ generateAndShowPattern() }, 800)
    }

    // Renamed from resetGame to resetRoundState for clarity
    private fun resetRoundState() {
        canUserClick = false
        userSelection.clear()
        pattern.clear()

        tvInstructions.text = getString(R.string.remember_pattern)
        btnStartGame.visibility = View.GONE

        for (cell in gridCells) {
            cell.setBackgroundColor(defaultCellColor)
        }
    }

    // --- NEW: Sends the game result to the backend ---
    private fun sendGameResult(score: Int) {
        val durationSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()

        val request = GameSessionCreateRequest(
            gameName = "Pattern Memory",
            score = score,
            durationSeconds = durationSeconds,
            difficultyLevel = currentDifficultyLevel
        )

        // Launch a background coroutine to send data. We don't need to wait for the result.
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = auth.currentUser?.getIdToken(false)?.await()?.token ?: return@launch
                val authHeader = "Bearer $token"
                val response = ApiClient.instance.createGameSession(authHeader, request)
                if (response.isSuccessful) {
                    Log.d("PatternMemory", "Game session logged successfully for level $currentDifficultyLevel")
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

        // --- NEW: Record the start time for this level ---
        sessionStartTime = System.currentTimeMillis()

        // Dynamic pause calculation (can be simplified as display time is now from backend)
        val currentPauseBetweenCells = maxOf(
            patternDisplayTime / 4, // A reasonable fraction of the display time
            MIN_PAUSE_BETWEEN_CELLS_MS
        )

        // Generate random pattern (No changes)
        val random = Random()
        val availableIndices = (0 until gridSize * gridSize).toMutableList()
        for (i in 0 until cellsToIlluminate) {
            if (availableIndices.isNotEmpty()) {
                val randomIndex = random.nextInt(availableIndices.size)
                val selectedIndex = availableIndices[randomIndex]
                pattern.add(selectedIndex)
                availableIndices.removeAt(randomIndex)
            }
        }

        // Show pattern with delay (using backend display time)
        showPatternWithDelay(patternDisplayTime, currentPauseBetweenCells)
    }

    private fun checkPattern() {
        canUserClick = false
        val isCorrect = userSelection == pattern

        if (isCorrect) {
            // --- NEW: Log successful result to backend ---
            // Score can be the level number completed.
            sendGameResult(score = currentLevel)

            tvInstructions.text = "Doğru! Sonraki seviye..."
            for (index in pattern) {
                if (index < gridCells.size) {
                    gridCells[index].setBackgroundColor(correctCellColor)
                }
            }

            // Fetch parameters for the NEXT level
            handler.postDelayed({ fetchGameParametersAndStart() }, 1500)

        } else {
            // This part of the original code was unreachable due to early exit in onCellClicked.
            // The logic is now handled in wrongSequence.
        }
    }

    private fun wrongSequence(wrongIndex: Int) {
        canUserClick = false
        isShowingPattern = false

        // --- NEW: Log failed result to backend. We can use a score of 0 for failure.
        sendGameResult(score = 0)

        tvInstructions.text = "Yanlış! Oyun Bitti."
        gridCells[wrongIndex].setBackgroundColor(wrongCellColor)

        // Show correct pattern
        handler.postDelayed({
            for (index in pattern) {
                if (index < gridCells.size) {
                    gridCells[index].setBackgroundColor(correctCellColor)
                }
            }
        }, 500)

        // Reset UI to allow starting a new game
        handler.postDelayed({
            resetUIForNewGame()
        }, 2000)
    }

    private fun resetUIForNewGame() {
        for (cell in gridCells) {
            cell.setBackgroundColor(defaultCellColor)
        }
        tvLevel.visibility = View.GONE
        tvInstructions.text = "Tekrar denemeye hazır mısın?"
        btnStartGame.text = getString(R.string.restart_game)
        btnStartGame.visibility = View.VISIBLE
        btnStartGame.isEnabled = true
    }

    // --- UNCHANGED HELPER FUNCTIONS ---
    private fun updateLevelText() {
        tvLevel.text = "Seviye: $currentLevel"
    }

    private fun onCellClicked(index: Int) {
        if (!canUserClick || isShowingPattern || index >= gridCells.size) return

        if (userSelection.size < pattern.size && index == pattern[userSelection.size]) {
            userSelection.add(index)
            gridCells[index].setBackgroundColor(selectedCellColor)

            if (userSelection.size == pattern.size) {
                handler.postDelayed({ checkPattern() }, 300)
            }
        } else {
            wrongSequence(index)
        }
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
                setOnClickListener { if (canUserClick && !isShowingPattern) onCellClicked(i) }
            }
            gridLayout.addView(button)
            gridCells.add(button)
        }
    }

    private fun showPatternWithDelay(illuminateDelay: Long, pauseBetweenCells: Long) {
        val totalCycleTime = illuminateDelay + pauseBetweenCells
        for ((index, cellIndex) in pattern.withIndex()) {
            if (cellIndex < gridCells.size) {
                val startTime = index * totalCycleTime
                handler.postDelayed({ gridCells[cellIndex].setBackgroundColor(illuminatedCellColor) }, startTime)
                handler.postDelayed({ gridCells[cellIndex].setBackgroundColor(defaultCellColor) }, startTime + illuminateDelay)
            }
        }
        val totalPatternTime = pattern.size * totalCycleTime + 500L
        handler.postDelayed({
            isShowingPattern = false
            canUserClick = true
            tvInstructions.text = getString(R.string.now_click)
        }, totalPatternTime)
    }
}