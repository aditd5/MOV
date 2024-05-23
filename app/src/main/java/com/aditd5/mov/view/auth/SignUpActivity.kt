package com.aditd5.mov.view.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.databinding.ActivitySignUpBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        db = FirebaseFirestore.getInstance()

        loadingDialog = LoadingDialog(this)

        mainButton()
    }

    private fun mainButton() {
        binding.apply {
            btnBack.setOnClickListener {
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                finishAffinity()
            }

            btnSignup.setOnClickListener {
                val name = etName.text.toString().trimEnd()
                val email = etEmail.text.toString().trimEnd()
                val password = etPassword.text.toString().trimEnd()
                val confirmPassword = etConfirmPassword.text.toString().trimEnd()

                checkFieldData(name, email, password, confirmPassword)
            }
        }
    }

    private fun checkFieldData(name: String, email: String, password: String, confirmPassword: String) {
        binding.apply {
            if (name.isEmpty()) {
                etName.error = "Nama tidak boleh kosong"
                etName.requestFocus()
            } else if (name.length < 4) {
                Toast.makeText(this@SignUpActivity,
                    "Nama tidak boleh kurang dari 4 karakter",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (email.isEmpty()) {
                etEmail.error = "Email tidak boleh kosong"
                etEmail.requestFocus()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Email tidak valid"
                etEmail.requestFocus()
            } else if (password.isEmpty()) {
                etPassword.error = "Password tidak boleh kosong"
                etPassword.requestFocus()
            } else if (password.length < 8) {
                etPassword.error = "Password tidak boleh kurang dari 8 karakter"
                etPassword.requestFocus()
            } else if (confirmPassword.isEmpty()) {
                etConfirmPassword.error = "Konfirmasi Password tidak boleh kosong"
                etConfirmPassword.requestFocus()
            } else if (password != confirmPassword) {
                etConfirmPassword.error = "Konfirmasi Password Harus Sesuai Dengan Password"
                etConfirmPassword.requestFocus()
            } else {
                signUp(name , email , password)
            }
        }
    }

    private fun signUp(name: String, email: String, password: String) {
        loadingDialog.showLoading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (user != null) {
                        updateUserProfile(user!!, name)     //user?.let { updateUserProfile(it , name) }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Failed"
                    Log.e("signup", errorMessage)
                    Toast.makeText(this,
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingDialog.dismissLoading()
                }
            }
    }

    private fun updateUserProfile(user: FirebaseUser, name: String) {
        UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build().also { userProfileChangeRequest ->
                user.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            addUserDetailInformation(name)
                        } else {
                            val errorMessage = task.exception?.message ?: "Failed"
                            Log.e("signup_profile", errorMessage)
                            Toast.makeText(this,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                            loadingDialog.dismissLoading()
                        }
                    }
            }
    }

    private fun addUserDetailInformation(name: String) {
        val userData = hashMapOf(
            "admin" to false,
            "wallet" to false,
            "balance" to 0
        )

        val docRef = db.collection("users").document(user!!.uid)
        docRef.set(userData)
            .addOnSuccessListener {
                Prefs.isLogin = true
                Prefs.name = name

                startActivity(Intent(this, SignUpPhotoActivity::class.java))
                finishAffinity()

                Toast.makeText(this,
                    "Berhasil mendaftar",
                    Toast.LENGTH_SHORT
                ).show()

                loadingDialog.dismissLoading()
            }
            .addOnFailureListener {
                val errorMessage = it.message ?: "Failed"
                Log.e("signup_firestore", errorMessage)
                Toast.makeText(this,
                    errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
                loadingDialog.dismissLoading()
            }
    }
}