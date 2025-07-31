package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Import Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth // Import auth
import com.google.firebase.ktx.Firebase // Import Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var btnHome: LinearLayout
    private lateinit var btnGame: LinearLayout
    private lateinit var btnJournal: LinearLayout
    private lateinit var btnLogout: Button // Declare the logout button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation(this, "home")

        btnHome = findViewById(R.id.btnHome)
        btnGame = findViewById(R.id.btnGame)
        btnJournal = findViewById(R.id.btnJournal)
        btnLogout = findViewById(R.id.btnLogout) // Bind the logout button

        // Başlangıçta Ana Sayfa seçili
        setSelectedButton(btnHome)

        // ****** ADD THIS LOGOUT LOGIC ******
        btnLogout.setOnClickListener {
            // Sign out from Firebase
            Firebase.auth.signOut()

            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Finish all activities in the stack to prevent the user from
            // pressing the back button and returning to MainActivity
            finishAffinity()
        }
        // ************************************

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

