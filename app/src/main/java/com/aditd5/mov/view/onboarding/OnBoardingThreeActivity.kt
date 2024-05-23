package com.aditd5.mov.view.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.databinding.ActivityOnBoardingThreeBinding
import com.aditd5.mov.view.auth.SignInActivity

class OnBoardingThreeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingThreeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingThreeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainButton()
    }

    private fun mainButton() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        }
    }
}