package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val nameInput = findViewById<EditText>(R.id.editTextName)
        val ageInput = findViewById<EditText>(R.id.editTextAge)
        val continueButton = findViewById<Button>(R.id.buttonContinue)

        // CheckBox referansları
        val checkbox1 = findViewById<CheckBox>(R.id.checkbox1)
        val checkbox2 = findViewById<CheckBox>(R.id.checkbox2)
        val checkbox3 = findViewById<CheckBox>(R.id.checkbox3)
        val checkbox4 = findViewById<CheckBox>(R.id.checkbox4)
        val checkbox5 = findViewById<CheckBox>(R.id.checkbox5)

        // Butona tıklanınca:
        continueButton.setOnClickListener {
            val name = nameInput.text.toString()
            val age = ageInput.text.toString()

            if (name.isNotEmpty() && age.isNotEmpty()) {
                val selectedInterests = mutableListOf<String>()

                if (checkbox1.isChecked) selectedInterests.add("Zihin Egzersizleri")
                if (checkbox2.isChecked) selectedInterests.add("Hafıza Kartları")
                if (checkbox3.isChecked) selectedInterests.add("Odaklanma Oyunları")
                if (checkbox4.isChecked) selectedInterests.add("Dikkat Testleri")
                if (checkbox5.isChecked) selectedInterests.add("Günlük Tutma")

                if (selectedInterests.isNotEmpty()) {
                    val interestsString = selectedInterests.joinToString(", ")

                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("interest", interestsString)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Lütfen en az bir ilgi alanı seç 💖", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Lütfen ad ve yaş alanlarını doldur ✍️", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
