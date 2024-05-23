package com.aditd5.mov.view.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.databinding.ActivityOnBoardingTwoBinding
import com.aditd5.mov.view.auth.SignInActivity

class OnBoardingTwoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingTwoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingTwoBinding.inflate(layoutInflater)
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
            startActivity(Intent(this, OnBoardingThreeActivity::class.java))
            finishAffinity()
        }

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        }
    }
}