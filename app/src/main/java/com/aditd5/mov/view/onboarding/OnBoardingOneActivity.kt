package com.aditd5.mov.view.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.databinding.ActivityOnBoardingOneBinding
import com.aditd5.mov.view.auth.SignInActivity

class OnBoardingOneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingOneBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingOneBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainButton()
    }

    private fun mainButton() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this , OnBoardingTwoActivity::class.java))
            finishAffinity()
        }

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this , SignInActivity::class.java))
            finishAffinity()
        }
    }
}