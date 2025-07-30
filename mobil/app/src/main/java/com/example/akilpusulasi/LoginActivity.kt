package com.example.akilpusulasi
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var showPasswordCheckBox: CheckBox
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var registerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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

        // Giriş Butonu tıklama listener - Direkt oyuna yönlendir
        loginButton.setOnClickListener {
            // Direkt Pattern Memory oyununa yönlendir
            val intent = Intent(this, PatternMemoryActivity::class.java)
            startActivity(intent)
            finish() // LoginActivity'yi kapat
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
