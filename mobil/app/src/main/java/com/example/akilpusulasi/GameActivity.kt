package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Set up the bottom navigation to show "Game" as selected
        setupBottomNavigation(this, "game")

        // Find the card for the Pattern Memory game
        val cardPatternMemory = findViewById<CardView>(R.id.cardPatternMemory)

        // Set a click listener to start the PatternMemoryActivity
        cardPatternMemory.setOnClickListener {
            val intent = Intent(this, PatternMemoryActivity::class.java)
            startActivity(intent)
        }
    }
}