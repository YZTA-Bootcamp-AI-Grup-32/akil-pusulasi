package com.example.akilpusulasi
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

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
            // User is already logged in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        // Viewleri bağla
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        showPasswordCheckBox = findViewById(R.id.showPasswordCheckBox)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        registerTextView = findViewById(R.id.registerTextView)

        // Şifre Göster/Gizle checkbox listener
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Şifre görünür olsun
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // Şifre gizlensin
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            // İmleci sonuna al
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Giriş Butonu tıklama listener
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "signInWithEmail:success")
                        Toast.makeText(this, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }

        }

        // Şifremi unuttum tıklama
        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Şifremi unuttum tıklandı", Toast.LENGTH_SHORT).show()
            // TODO: Şifre sıfırlama ekranına yönlendirme
        }

        // Kayıt ol tıklama
        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        // Şifremi unuttum tıklama
        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }


    }
}
