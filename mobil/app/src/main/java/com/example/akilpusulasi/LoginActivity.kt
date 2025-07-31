package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.akilpusulasi.network.network.ApiClient
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var showPasswordCheckBox: CheckBox
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var registerTextView: TextView

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, CHECK for backend profile before proceeding
            Log.d("LoginActivity", "User already signed in. Checking backend profile.")
            checkBackendProfileAndNavigate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        // View bindings
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        registerTextView = findViewById(R.id.registerTextView)

        // Show/Hide Password
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            passwordEditText.transformationMethod = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Login Button Click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            loginButton.text = "Giriş Yapılıyor..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "signInWithEmail:success. Checking backend profile.")
                        // Instead of going to MainActivity, CHECK for profile first.
                        checkBackendProfileAndNavigate()
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        loginButton.isEnabled = true
                        loginButton.text = "Giriş Yap"
                    }
                }
        }

        // Other click listeners
        forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkBackendProfileAndNavigate() {
        // Use a coroutine to perform the network request
        lifecycleScope.launch {
            try {
                val currentUser = auth.currentUser
                    ?: throw IllegalStateException("User not found for profile check.")

                val token = currentUser.getIdToken(true).await().token
                    ?: throw IllegalStateException("Could not get auth token.")

                val authHeader = "Bearer $token"
                val response = ApiClient.instance.getMyProfile(authHeader)

                if (response.isSuccessful) {
                    // HTTP 200 OK: Profile exists. Go to MainActivity.
                    Log.d("LoginActivity", "Backend profile found. Navigating to MainActivity.")
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finishAffinity()
                } else if (response.code() == 404) {
                    // HTTP 404 Not Found: Profile does NOT exist. Force user to create one.
                    Log.d("LoginActivity", "Backend profile NOT found. Navigating to ProfileActivity.")
                    startActivity(Intent(this@LoginActivity, ProfileActivity::class.java))
                    finishAffinity()
                } else {
                    // Other server error
                    throw Exception("Server error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                // Handle exceptions (network error, etc.)
                Log.e("LoginActivity", "Error checking backend profile", e)
                Toast.makeText(this@LoginActivity, "Profil kontrol edilemedi: ${e.message}", Toast.LENGTH_LONG).show()
                // Reset button state if login was attempted
                loginButton.isEnabled = true
                loginButton.text = "Giriş Yap"
            }
        }
    }
}