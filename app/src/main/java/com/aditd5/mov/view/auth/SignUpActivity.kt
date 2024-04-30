package com.aditd5.mov.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns.EMAIL_ADDRESS
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aditd5.mov.databinding.ActivitySignUpBinding
import com.aditd5.mov.model.User
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        mainButton()
    }

    private fun mainButton() {
        binding.apply {
            btnBack.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                finishAffinity()
            }

            btnSignup.setOnClickListener {
                val name = binding.etName.text.toString()
//                val username = binding.etUsername.text.toString()
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                if (name.isEmpty()) {
                    etName.error = "Nama tidak boleh kosong"
                    etName.requestFocus()
                } else if (name.length < 4) {
                    Toast.makeText(this@SignUpActivity,
                        "Nama tidak boleh kurang dari 4 karakter",
                        Toast.LENGTH_SHORT
                    ).show()
//                } else if (username.isEmpty()) {
//                    etUsername.error = "Username tidak boleh kosong"
//                    etUsername.requestFocus()
//                } else if (username.length < 4) {
//                    Toast.makeText(this@SignUpActivity,
//                        "Username tidak boleh kurang dari 4 karakter",
//                        Toast.LENGTH_SHORT
//                    ).show()
                } else if (email.isEmpty()) {
                    etEmail.error = "Email tidak boleh kosong"
                    etEmail.requestFocus()
                } else if (!EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.error = "Email tidak valid"
                    etEmail.requestFocus()
                } else if (password.isEmpty()) {
                    etPassword.error = "Password tidak boleh kosong"
                    etPassword.requestFocus()
                } else if (password.length < 8) {
                    Toast.makeText(this@SignUpActivity,
                        "Password tidak boleh kurang dari 8 karakter",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    signUp(name, email, password)
                }
            }
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        val loading = LoadingDialog(this)
        loading.showLoading()

        val userData = User()
        userData.name = name
//        userData.username = username
        userData.email = email
        userData.password = password

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this,
                                    "Berhasil mendaftar",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Prefs.isLogin = true
                                Prefs.name = name
                                startActivity(Intent(this, SignUpPhotoActivity::class.java))
                                loading.dismissLoading()
                            } else {
                                val errorMessage = it.exception?.message ?: "Failed"
                                Toast.makeText(this,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                                loading.dismissLoading()
                            }
                        }
                } else {
                    val errorMessage = task.exception?.message ?: "Failed"
                    Toast.makeText(this,
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()
                }
            }
    }
}