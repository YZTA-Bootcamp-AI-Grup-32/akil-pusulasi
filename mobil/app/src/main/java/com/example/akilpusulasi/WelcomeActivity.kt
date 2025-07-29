package com.example.akilpusulasi

import android.content.Intent // Import Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    // Define the duration of the welcome screen in milliseconds
    private val WELCOME_SCREEN_DURATION: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // TextView'e referans alıyoruz
        val welcomeText = findViewById<TextView>(R.id.textViewWelcome)

        // Intent'ten gelen verileri al
        val name = intent.getStringExtra("name") ?: "Kullanıcı"
        val interest = intent.getStringExtra("interest") ?: "Zihin Gücü"

        // Hoş geldin mesajı
        val message = """
            🌟 Hoş geldin, $name! 🌟

            💫 İlgi alanın: $interest
            🧠 Zihnini keşfetmeye hazır mısın?
        """.trimIndent()

        // Mesajı ekranda göster
        welcomeText.text = message


        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)

        }, WELCOME_SCREEN_DURATION)
    }
}