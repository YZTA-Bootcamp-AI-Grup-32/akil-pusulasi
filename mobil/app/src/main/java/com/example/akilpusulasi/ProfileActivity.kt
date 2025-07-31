// In C:\...\mobil\app\src\main\java\com\example\akilpusulasi\ProfileActivity.kt

package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akilpusulasi.network.network.ApiClient
import com.example.akilpusulasi.network.request.CreateUserRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Make sure you have the 'kotlinx-coroutines-play-services' dependency
import kotlinx.coroutines.withContext
import java.util.Calendar // Import Calendar for calculating birth year

class ProfileActivity : AppCompatActivity() {

    // Declare views at the class level
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var continueButton: Button
    private lateinit var checkbox1: CheckBox
    private lateinit var checkbox2: CheckBox
    private lateinit var checkbox3: CheckBox
    private lateinit var checkbox4: CheckBox
    private lateinit var checkbox5: CheckBox

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Bind views
        nameInput = findViewById(R.id.editTextName)
        ageInput = findViewById(R.id.editTextAge)
        continueButton = findViewById(R.id.buttonContinue)
        checkbox1 = findViewById(R.id.checkbox1)
        checkbox2 = findViewById(R.id.checkbox2)
        checkbox3 = findViewById(R.id.checkbox3)
        checkbox4 = findViewById(R.id.checkbox4)
        checkbox5 = findViewById(R.id.checkbox5)

        continueButton.setOnClickListener {
            handleContinueClick()
        }
    }

    private fun handleContinueClick() {
        // 1. Validate Input on the Main Thread
        val name = nameInput.text.toString().trim()
        val ageString = ageInput.text.toString().trim()

        if (name.isEmpty() || ageString.isEmpty()) {
            Toast.makeText(this, "Lütfen ad ve yaş alanlarını doldurun ✍️", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageString.toIntOrNull()
        if (age == null || age <= 0) {
            Toast.makeText(this, "Lütfen geçerli bir yaş girin.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedInterests = getSelectedInterests()
        if (selectedInterests.isEmpty()) {
            Toast.makeText(this, "Lütfen en az bir ilgi alanı seç 💖", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Disable the button to prevent multiple clicks
        continueButton.isEnabled = false
        continueButton.text = "İşleniyor..."

        // 3. Launch a coroutine for background work
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 4. Get Firebase ID Token
                val currentUser = auth.currentUser
                    ?: throw IllegalStateException("Kullanıcı bulunamadı. Lütfen tekrar giriş yapın.")

                val idToken = currentUser.getIdToken(true).await().token
                    ?: throw IllegalStateException("Kimlik doğrulama başarısız. Lütfen tekrar deneyin.")

                // 5. Call your Backend API
                val birthYear = Calendar.getInstance().get(Calendar.YEAR) - age
                val request = CreateUserRequest(
                    fullName = name,
                    birthYear = birthYear,
                    interests = selectedInterests
                )
                val authHeader = "Bearer $idToken"
                val response = ApiClient.instance.createUser(authHeader, request)

                // 6. Handle the Result on the Main Thread
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("ProfileActivity", "Backend profile created successfully.")
                        navigateToWelcomeScreen(name, selectedInterests)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ProfileActivity", "Backend error: ${response.code()} - $errorBody")
                        showError("Profil oluşturulamadı: Sunucu hatası (${response.code()})")
                    }
                }

            } catch (e: Exception) {
                // Handle any exceptions (network, Firebase, etc.)
                Log.e("ProfileActivity", "An exception occurred", e)
                showError("Bir hata oluştu: ${e.localizedMessage}")
            } finally {
                // 7. ALWAYS re-enable the button on the Main Thread
                withContext(Dispatchers.Main) {
                    continueButton.isEnabled = true
                    continueButton.text = getString(R.string.button_continue)
                }
            }
        }
    }

    private fun getSelectedInterests(): List<String> {
        // Using the text from the checkboxes directly is more maintainable
        return listOf(checkbox1, checkbox2, checkbox3, checkbox4, checkbox5)
            .filter { it.isChecked }
            .map { it.text.toString() }
    }

    private fun navigateToWelcomeScreen(name: String, interests: List<String>) {
        val intent = Intent(this, WelcomeActivity::class.java).apply {
            putExtra("name", name)
            putExtra("interest", interests.joinToString(", "))
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun showError(message: String) {
        Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_LONG).show()
    }
}