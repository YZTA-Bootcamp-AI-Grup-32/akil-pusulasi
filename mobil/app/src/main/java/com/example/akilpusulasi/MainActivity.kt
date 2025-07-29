package com.example.akilpusulasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.widget.LinearLayout


class MainActivity : AppCompatActivity() {
    private lateinit var btnHome:LinearLayout
    private lateinit var btnGame:LinearLayout
    private lateinit var btnJournal:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation(this, "home")

        btnHome = findViewById(R.id.btnHome)
        btnGame = findViewById(R.id.btnGame)
        btnJournal = findViewById(R.id.btnJournal)

        // Başlangıçta Ana Sayfa seçili
        setSelectedButton(btnHome)

        btnHome.setOnClickListener {
            setSelectedButton(btnHome)
            // Ana sayfa zaten burası
        }

        btnGame.setOnClickListener {
            setSelectedButton(btnGame)
            startActivity(Intent(this, GameActivity::class.java))
        }

        btnJournal.setOnClickListener {
            setSelectedButton(btnJournal)
            startActivity(Intent(this, JournalActivity::class.java))
        }
    }

    private fun setSelectedButton(selected: LinearLayout) {
        btnHome.isSelected = false
        btnGame.isSelected = false
        btnJournal.isSelected = false
        selected.isSelected = true
    }
}

