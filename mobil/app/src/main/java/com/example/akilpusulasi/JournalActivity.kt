package com.example.akilpusulasi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class JournalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)

        setupBottomNavigation(this, "journal")
    }
}
