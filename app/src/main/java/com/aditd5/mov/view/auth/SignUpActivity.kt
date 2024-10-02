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
    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore

    private lateinit var loading: LoadingDialog

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
        db = FirebaseFirestore.getInstance()

        loading = LoadingDialog(this)

        setButtonListener()
    }

    private fun setButtonListener() {
        binding.apply {
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnSignup.setOnClickListener {
                val firstName = etFirstName.text.toString().trimStart().trimEnd()
                val lastName = etLastName.text.toString().trimStart().trimEnd()
                val email = etEmail.text.toString().trimStart().trimEnd()
                val password = etPassword.text.toString().trimStart().trimEnd()
                val confirmPassword = etConfirmPassword.text.toString().trimStart().trimEnd()

                checkFieldData(firstName , lastName , email , password , confirmPassword)
            }
        }
    }

    private fun checkFieldData(firstName: String , lastName: String , email: String , password: String , confirmPassword: String) {
        binding.apply {
            if (firstName.isEmpty()) {
                etFirstName.error = "Nama depan tidak boleh kosong"
                etFirstName.requestFocus()
            } else if (firstName.length < 4) {
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
                etPassword.error = "Kata sandi tidak boleh kosong"
                etPassword.requestFocus()
            } else if (password.length < 8) {
                etPassword.error = "Kata sandi tidak boleh kurang dari 8 karakter"
                etPassword.requestFocus()
            } else if (confirmPassword.isEmpty()) {
                etConfirmPassword.error = "Konfirmasi kata sandi tidak boleh kosong"
                etConfirmPassword.requestFocus()
            } else if (password != confirmPassword) {
                etConfirmPassword.error = "Konfirmasi kata sandi harus sama dengan kata sandi"
                etConfirmPassword.requestFocus()
            } else {
                signUp(firstName , lastName , email , password)
            }
        }
    }

    private fun signUp(firstName: String, lastName: String, email: String, password: String) {
        loading.showLoading()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser!!
                    updateUserProfile(firstName , lastName , email)
                } else {
                    val error = task.exception
                    Log.e("signup", "signup failure", error)
                    Toast.makeText(
                        this,
                        error?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()
                }
            }
    }

    private fun updateUserProfile(firstName: String , lastName: String , email: String) {
        UserProfileChangeRequest.Builder()
            .setDisplayName("$firstName $lastName")
            .build().also { userProfileChangeRequest ->
                user.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            addUserRole(firstName , lastName , email)
                        } else {
                            val error = task.exception
                            Toast.makeText(
                                this,
                                error?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            loading.dismissLoading()
                            Log.e("signup", "update user failure", error)
                        }
                    }
            }
    }

    private fun addUserRole(firstName: String , lastName: String , email: String) {
        val userData = hashMapOf(
            "admin" to false
        )

        val docRef = db.collection("users").document(user.uid)
        docRef.set(userData)
            .addOnSuccessListener {
                Prefs.isLogin = true
                Prefs.isGuest = false
                Prefs.firstName = firstName
                Prefs.lastName = lastName
                Prefs.email = email

                startActivity(Intent(this, SignUpPhotoActivity::class.java))
                finishAffinity()

                Toast.makeText(
                    this,
                    "Berhasil mendaftar, silahkan mengupload foto profil",
                    Toast.LENGTH_SHORT
                ).show()

                loading.dismissLoading()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
                loading.dismissLoading()
                Log.e("signup", "add user role failure", e)
            }
    }
}