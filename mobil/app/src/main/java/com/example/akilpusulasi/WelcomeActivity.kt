package com.example.akilpusulasi

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.akilpusulasi.R


class WelcomeActivity : AppCompatActivity() {
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
    }
}
