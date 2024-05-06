package com.aditd5.mov.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aditd5.mov.databinding.ActivitySignInBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        mainButton()
    }

    private fun mainButton() {
        binding.apply {
            btnSignin.setOnClickListener {
                val email = etEmail.text.toString().trimEnd()
                val password = etPassword.text.toString().trimEnd()

                if (email.isEmpty()) {
                    etEmail.error = "Email tidak boleh kosong"
                    etEmail.requestFocus()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Email tidak valid"
                    etEmail.requestFocus()
                } else if (password.isEmpty()) {
                    etPassword.error = "Password tidak boleh kosong"
                    etPassword.requestFocus()
                } else {
                    signIn(email, password)
                }
            }

            btnSignup.setOnClickListener {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }
        }
    }

    private fun signIn(email: String, password: String) {
        val loading =  LoadingDialog(this@SignInActivity)
        loading.showLoading()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val name = auth.currentUser?.displayName
                    Toast.makeText(
                        this,
                        "Selamat Datang $name",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                    Prefs.isLogin = true
                    Prefs.name = name.toString()
                    loading.dismissLoading()
                } else {
                    Toast.makeText(
                        this,
                        "${it.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()

//                    if (it.exception == "FirebaseAuthInvalidUserException") {
//                        Toast.makeText(this, "Email tidak terdaftar", Toast.LENGTH_SHORT).show()
//                    } else {
//
//                    }
                }
            }
    }
}