package com.example.akilpusulasi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Random
import kotlin.math.min

class PatternMemoryActivity : AppCompatActivity() {

    // Oyun sabitleri
    companion object {
        const val INITIAL_ILLUMINATE_DELAY_MS: Long = 1200L // 1.2 saniye yanık kalma
        const val MIN_ILLUMINATE_DELAY_MS: Long = 600L // 0.6 saniye minimum yanma
        const val INITIAL_PAUSE_BETWEEN_CELLS_MS: Long = 500L // 0.5 saniye bekleme
        const val MIN_PAUSE_BETWEEN_CELLS_MS: Long = 250L // 0.25 saniye minimum bekleme
        const val DELAY_REDUCTION_PER_LEVEL_MS: Long = 35L // Her seviyede 35ms azalma (daha belirgin)
        const val PAUSE_REDUCTION_PER_LEVEL_MS: Long = 18L // Her seviyede 18ms azalma (daha belirgin)
        const val MAX_GAME_LEVEL: Int = 25 // Maksimum oyun seviyesi
    }

    // Oyun durumu değişkenleri
    private var currentLevel = 1 // Mevcut oyun seviyesi
    private var gridSize = 3 // Izgaranın boyutu (örneğin 3x3 için 3)
    private var cellsToIlluminate = 2 // Her seviyede yanacak hücre sayısı başlangıcı

    // UI elemanları
    private lateinit var gridLayout: GridLayout
    private lateinit var tvInstructions: TextView
    private lateinit var tvLevel: TextView
    private lateinit var btnStartGame: Button

    // Oyun mantığı için veri yapıları
    private var pattern = mutableListOf<Int>() // AI tarafından oluşturulan yanan hücrelerin indeksleri
    private var userSelection = mutableListOf<Int>() // Kullanıcının seçtiği hücrelerin indeksleri
    private var currentPatternIndex = 0 // Kullanıcının şu anda beklenen pattern indeksi

    // Oyunun çeşitli durumlarını kontrol eden flag'ler
    private var isGameActive = false // Oyunun aktif olup olmadığını belirtir
    private var isShowingPattern = false // Desenin gösterilip gösterilmediğini belirtir
    private var canUserClick = false // Kullanıcının hücrelere tıklayıp tıklayamayacağını belirtir
    private var isFirstGame = true // İlk oyun başlatma kontrolü

    // Izgaradaki tüm hücre butonlarını tutacak liste
    private val gridCells = mutableListOf<Button>()

    // Renk referansları
    private var defaultCellColor: Int = 0
    private var illuminatedCellColor: Int = 0
    private var selectedCellColor: Int = 0
    private var correctCellColor: Int = 0
    private var wrongCellColor: Int = 0

    // Handler'ları temizlemek için
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_memory)

        // UI elemanlarını bağla
        gridLayout = findViewById(R.id.grid_layout)
        tvInstructions = findViewById(R.id.tv_instructions)
        tvLevel = findViewById(R.id.tv_level)
        btnStartGame = findViewById(R.id.btn_start_game)

        // Renk referanslarını al (API uyumluluğu için ContextCompat kullan)
        defaultCellColor = ContextCompat.getColor(this, R.color.gray)
        illuminatedCellColor = ContextCompat.getColor(this, R.color.blue)
        selectedCellColor = ContextCompat.getColor(this, R.color.selected_purple)
        correctCellColor = ContextCompat.getColor(this, R.color.green)
        wrongCellColor = ContextCompat.getColor(this, R.color.red)

        // Başlangıçta seviye metnini gizle
        tvLevel.visibility = View.GONE

        // Başlat butonuna tıklama dinleyicisi ekle
        btnStartGame.setOnClickListener {
            startGame()
        }

        // Izgarayı kur
        setupGrid()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Tüm gecikmeli işlemleri temizle
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Izgarayı dinamik olarak oluşturur ve her hücreye tıklama dinleyicisi ekler
     */
    private fun setupGrid() {
        // Önceki hücreleri temizle
        gridLayout.removeAllViews()
        gridCells.clear()

        // Grid boyutlarını ayarla
        gridLayout.columnCount = gridSize
        gridLayout.rowCount = gridSize

        // Hücre boyutunu hesapla (minimum boyut garantisi ile)
        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val availableWidth = screenWidth - (64 * density).toInt() // Padding ve margin için alan bırak
        val minCellSize = (80 * density).toInt() // Minimum 80dp
        val cellSize = maxOf(availableWidth / gridSize, minCellSize)

        // Her bir hücre için buton oluştur
        for (i in 0 until gridSize * gridSize) {
            val button = Button(this)
            
            // Layout parametrelerini ayarla
            val params = GridLayout.LayoutParams()
            params.width = cellSize
            params.height = cellSize
            params.setMargins(4, 4, 4, 4)
            button.layoutParams = params

            // Butonun arka plan rengini ayarla
            button.setBackgroundColor(defaultCellColor)

            // Tıklama dinleyicisi ekle
            button.setOnClickListener {
                if (canUserClick && !isShowingPattern) {
                    onCellClicked(i)
                }
            }

            // Butonu ızgaraya ekle ve listeye kaydet
            gridLayout.addView(button)
            gridCells.add(button)
        }
    }

    /**
     * Oyunu başlatır
     */
    private fun startGame() {
        // Her oyun başlangıcında tüm parametreleri sıfırla
        currentLevel = 1
        gridSize = 3
        cellsToIlluminate = 2
        isFirstGame = false

        // Seviye metnini göster ve güncelle
        tvLevel.visibility = View.VISIBLE
        updateLevelText()
        
        // Zorluk parametrelerini güncelle (grid boyutu 3x3 olarak ayarlanacak)
        updateDifficultyParameters()
        
        // Izgarayı kesinlikle yeniden kur (3x3 olarak)
        setupGrid()
        
        // Oyun durumunu sıfırla
        resetGame()

        // Kısa bir hazırlık süresi sonra deseni göster
        handler.postDelayed({ generateAndShowPattern() }, 800)
    }

    /**
     * Oyun durumunu yeni bir tur veya seviye için sıfırlar
     */
    private fun resetGame() {
        isGameActive = true
        canUserClick = false
        userSelection.clear()
        pattern.clear()
        currentPatternIndex = 0

        tvInstructions.text = getString(R.string.remember_pattern)
        btnStartGame.visibility = View.GONE

        // Tüm hücrelerin rengini varsayılana döndür
        for (cell in gridCells) {
            cell.setBackgroundColor(defaultCellColor)
        }
    }

    /**
     * Seviye metnini günceller
     */
    private fun updateLevelText() {
        tvLevel.text = "Seviye: $currentLevel"
    }

    /**
     * Current level'a göre zorluk parametrelerini dinamik olarak ayarlar
     */
    private fun updateDifficultyParameters() {
        // Mevcut grid boyutunu kaydet
        val oldGridSize = gridSize
        
        when (currentLevel) {
            in 1..3 -> {
                gridSize = 3
                cellsToIlluminate = 2
            }
            in 4..6 -> {
                gridSize = 3
                cellsToIlluminate = 3
            }
            in 7..9 -> {
                gridSize = 4
                cellsToIlluminate = 3
            }
            in 10..12 -> {
                gridSize = 4
                cellsToIlluminate = 4
            }
            in 13..15 -> {
                gridSize = 4
                cellsToIlluminate = 5
            }
            in 16..18 -> {
                gridSize = 5
                cellsToIlluminate = 5
            }
            in 19..21 -> {
                gridSize = 5
                cellsToIlluminate = 6
            }
            in 22..MAX_GAME_LEVEL -> {
                gridSize = 5
                cellsToIlluminate = 7
            }
        }
        
        // Grid boyutu değiştiyse veya grid henüz oluşturulmadıysa ızgarayı yeniden oluştur
        if (oldGridSize != gridSize || gridLayout.columnCount != gridSize) {
            setupGrid()
        }
    }

    /**
     * Rastgele desen oluşturur ve görsel olarak gösterir
     */
    private fun generateAndShowPattern() {
        pattern.clear()
        isShowingPattern = true
        canUserClick = false

        // Dinamik süre hesaplaması - yaşlı kullanıcılar için yavaş azalma
        val reductionFactor = currentLevel - 1 // Seviye ilerledikçe artan faktör
        
        val currentIlluminateDelay = maxOf(
            INITIAL_ILLUMINATE_DELAY_MS - (reductionFactor * DELAY_REDUCTION_PER_LEVEL_MS),
            MIN_ILLUMINATE_DELAY_MS
        )
        val currentPauseBetweenCells = maxOf(
            INITIAL_PAUSE_BETWEEN_CELLS_MS - (reductionFactor * PAUSE_REDUCTION_PER_LEVEL_MS),
            MIN_PAUSE_BETWEEN_CELLS_MS
        )

        // Rastgele desen oluştur
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

        // Tane tane yanma efekti ile deseni göster
        showPatternWithDelay(currentIlluminateDelay, currentPauseBetweenCells)
    }

    /**
     * Deseni gecikmeli olarak gösterir - sırayla yanıp sönme akışı ile
     */
    private fun showPatternWithDelay(illuminateDelay: Long, pauseBetweenCells: Long) {
        // Her kutucuk için toplam döngü süresi: yanma + bekleme
        val totalCycleTime = illuminateDelay + pauseBetweenCells
        
        for ((index, cellIndex) in pattern.withIndex()) {
            // Array bounds kontrolü
            if (cellIndex < gridCells.size) {
                // Her kutucuğun başlangıç zamanı: önceki kutucukların toplam döngü süresi
                val startTime = index * totalCycleTime
                
                // Kutucuğu yak
                handler.postDelayed({
                    gridCells[cellIndex].setBackgroundColor(illuminatedCellColor)
                }, startTime)

                // Kutucuğu söndür (yanma süresi sonunda)
                handler.postDelayed({
                    gridCells[cellIndex].setBackgroundColor(defaultCellColor)
                }, startTime + illuminateDelay)
            }
        }

        // Desen gösterimi bittikten sonra kullanıcıya tıklama izni ver
        // Son kutucuğun döngüsü tamamlandıktan sonra ek bekleme süresi
        val totalPatternTime = pattern.size * totalCycleTime + 500L
        handler.postDelayed({
            isShowingPattern = false
            canUserClick = true
            currentPatternIndex = 0 // Sıra kontrolü için indeksi sıfırla
            tvInstructions.text = getString(R.string.now_click)
        }, totalPatternTime)
    }

    /**
     * Kullanıcının bir hücreye tıkladığında çağrılır
     */
    private fun onCellClicked(index: Int) {
        // Tıklama kontrolü
        if (!canUserClick || isShowingPattern || index >= gridCells.size) {
            return
        }

        // Sıralama kontrolü - kullanıcı doğru sırayla tıklamalı
        if (userSelection.size < pattern.size && index == pattern[userSelection.size]) {
            // Doğru sırada doğru hücreye tıklandı
            userSelection.add(index)
            gridCells[index].setBackgroundColor(selectedCellColor)

            // Kullanıcı tüm deseni doğru sırayla girdi mi kontrol et
            if (userSelection.size == pattern.size) {
                // Son tıklamanın görsel geri bildirimini göster
                handler.postDelayed({
                    checkPattern()
                }, 500)
            }
        } else {
            // Yanlış sıra veya yanlış hücre - oyun biter
            wrongSequence(index)
        }
    }

    /**
     * Yanlış sıra durumunda çağrılır
     */
    private fun wrongSequence(wrongIndex: Int) {
        canUserClick = false
        tvInstructions.text = "Yanlış! Oyun Bitti."
        isGameActive = false

        // Yanlış seçilen hücreyi kısa süreliğine vurgula
        gridCells[wrongIndex].setBackgroundColor(wrongCellColor)

        // Doğru deseni kısa süreliğine göster
        handler.postDelayed({
            for (index in pattern) {
                if (index < gridCells.size) {
                    gridCells[index].setBackgroundColor(correctCellColor)
                }
            }
        }, 500)

        handler.postDelayed({
            for (index in pattern) {
                if (index < gridCells.size) {
                    gridCells[index].setBackgroundColor(defaultCellColor)
                }
            }
            tvLevel.visibility = View.GONE
            btnStartGame.text = getString(R.string.restart_game)
            btnStartGame.visibility = View.VISIBLE
        }, 2000)
    }

    /**
     * Kullanıcının seçimini kontrol eder
     */
    private fun checkPattern() {
        canUserClick = false

        // Kullanıcının seçimi ile orijinal deseni karşılaştır
        val isCorrect = userSelection == pattern // Sıra da önemli olduğu için == kullanıyoruz

        if (isCorrect) {
            // Doğru cevap
            tvInstructions.text = "Doğru! Sonraki seviye..."
            
            // Seviyeyi artır
            currentLevel++
            
            // Maksimum seviye kontrolü
            if (currentLevel > MAX_GAME_LEVEL) {
                tvInstructions.text = "Tebrikler! Oyunu Tamamladınız!"
                tvLevel.visibility = View.GONE
                btnStartGame.text = getString(R.string.restart_game)
                btnStartGame.visibility = View.VISIBLE
                isGameActive = false
                return
            }

            // Doğru cevapta yeşil vurgu
            for (index in pattern) {
                if (index < gridCells.size) {
                    gridCells[index].setBackgroundColor(correctCellColor)
                }
            }

            // 1000ms sonra yeni seviyeye geç
            handler.postDelayed({
                // Tüm hücreleri varsayılan renge döndür
                for (index in pattern) {
                    if (index < gridCells.size) {
                        gridCells[index].setBackgroundColor(defaultCellColor)
                    }
                }
                
                // Zorluk parametrelerini güncelle
                updateDifficultyParameters()
                updateLevelText()
                
                // Oyun durumunu sıfırla
                resetGame()
                
                // Yeni deseni göster
                handler.postDelayed({ generateAndShowPattern() }, 800)
            }, 1000)

        } else {
            // Yanlış cevap
            tvInstructions.text = getString(R.string.wrong_answer)
            isGameActive = false

            // Yanlış seçilen hücreleri kısa süreliğine vurgula
            for (index in userSelection) {
                if (index < gridCells.size) {
                    gridCells[index].setBackgroundColor(wrongCellColor)
                }
            }

            // Doğru deseni kısa süreliğine göster
            handler.postDelayed({
                for (index in pattern) {
                    if (index < gridCells.size) {
                        gridCells[index].setBackgroundColor(correctCellColor)
                    }
                }
            }, 500)

            handler.postDelayed({
                for (index in pattern) {
                    if (index < gridCells.size) {
                        gridCells[index].setBackgroundColor(defaultCellColor)
                    }
                }
                tvLevel.visibility = View.GONE
                btnStartGame.text = getString(R.string.restart_game)
                btnStartGame.visibility = View.VISIBLE
            }, 2000)
        }
    }
} 