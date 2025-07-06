package com.example.akilpusulasi

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val emailEditText = findViewById<EditText>(R.id.editTextResetEmail)
        val resetButton = findViewById<Button>(R.id.buttonReset)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Şifre sıfırlama bağlantısı gönderildi 📧 (Firebase sonra)", Toast.LENGTH_SHORT).show()
                finish() // login ekranına geri dön
            } else {
                Toast.makeText(this, "Lütfen e-posta girin", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
