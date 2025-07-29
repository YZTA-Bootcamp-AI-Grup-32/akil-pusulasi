package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupBottomNavigation(this, "game")

        // Alt navigasyon butonlarını tanımla
        val btnHome = findViewById<LinearLayout>(R.id.btnHome)
        val btnGame = findViewById<LinearLayout>(R.id.btnGame)
        val btnJournal = findViewById<LinearLayout>(R.id.btnJournal)

        // Ana sayfa butonu → MainActivity'e gider
        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Günlük butonu → JournalActivity'e gider
        btnJournal.setOnClickListener {
            startActivity(Intent(this, JournalActivity::class.java))
            finish()
        }

        // Oyun butonuna tıklanırsa → zaten bu sayfadasın, hiçbir şey yapma
        btnGame.setOnClickListener {
            // opsiyonel: kullanıcıya zaten buradasın diyebilirsin
        }
    }
}
