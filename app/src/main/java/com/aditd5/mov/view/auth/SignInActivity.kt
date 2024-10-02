package com.aditd5.mov.view.auth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivitySignInBinding
import com.aditd5.mov.databinding.ResetPasswordDialogBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var loading: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        loading = LoadingDialog(this)

        setButtonListener()
    }

    private fun setButtonListener() {
        binding.apply {
            btnSignin.setOnClickListener {
                val email = etEmail.text.toString().trimStart().trimEnd()
                val password = etPassword.text.toString().trimStart().trimEnd()

                if (email.isEmpty()) {
                    etEmail.error = "Email tidak boleh kosong"
                    etEmail.requestFocus()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Email tidak valid"
                    etEmail.requestFocus()
                } else if (password.isEmpty()) {
                    etPassword.error = "Kata sandi tidak boleh kosong"
                    etPassword.requestFocus()
                } else {
                    signIn(email, password)
                }
            }

            btnSignup.setOnClickListener {
                startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
            }

            btnForgotPassword.setOnClickListener {
                resetPasswordDialog()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        loading.showLoading()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    val fullname = user!!.displayName
                    val uri = user.photoUrl
                    Toast.makeText(
                        this,
                        "Selamat Datang $fullname",
                        Toast.LENGTH_SHORT
                    ).show()

                    Prefs.isLogin = true
                    Prefs.isGuest = false
                    Prefs.firstName = fullname!!.split(" ")[0]
                    Prefs.lastName = fullname.split(" ").getOrNull(1) ?: ""
                    Prefs.email = email
                    Prefs.imgProfileUri = uri.toString()

                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()

                    loading.dismissLoading()
                } else {
                    val error = it.exception
                    Toast.makeText(
                        this,
                        error?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()
                    Log.e("signin", "signin failure", error)
                }
            }
    }

    private fun resetPasswordDialog() {
        val dialogBinding = ResetPasswordDialogBinding.inflate(this.layoutInflater)
        val dialog = Dialog(this, R.style.DialogTheme)

        if (dialogBinding.root.parent != null) {
            (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.show()

        dialogBinding.apply {
            btnResetPassword.setOnClickListener {
                val email = etEmail.text.toString().trimStart().trimEnd()

                if (email.isEmpty()) {
                    etEmail.error = "Email tidak boleh kosong"
                    etEmail.requestFocus()
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Email tidak valid"
                    etEmail.requestFocus()
                } else {
                    loading.showLoading()

                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this@SignInActivity,
                                    "Email reset password telah dikirim",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loading.dismissLoading()
                                dialog.dismiss()
                            } else {
                                val error = it.exception
                                Toast.makeText(
                                    this@SignInActivity,
                                    error?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                loading.dismissLoading()
                                dialog.dismiss()
                                Log.e("signin", "reset password failure", error)
                            }
                        }
                }
            }
        }
    }
}