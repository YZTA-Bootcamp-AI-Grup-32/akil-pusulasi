package com.example.akilpusulasi

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var goToLoginTextView: TextView
    private lateinit var checkBoxShowPassword: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        // XML view tanımları
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.buttonRegister)
        goToLoginTextView = findViewById(R.id.textViewGoToLogin)
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword)

        // Şifreyi göster/gizle özelliği
        checkBoxShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
        }

        // Kayıt butonu tıklama
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, user is created
                        Log.d("RegisterActivity", "createUserWithEmail:success")
                        Toast.makeText(this, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()

                        // PROFIL ekranına yönlendir
                        val intent = Intent(this, ProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity() // Finish this and any parent activities (like Login)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }

        }

        // Giriş sayfasına geçiş
        goToLoginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
