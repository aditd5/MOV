package com.aditd5.mov.view.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aditd5.mov.databinding.ActivityOnBoardingTwoBinding
import com.aditd5.mov.view.auth.SignInActivity

class OnBoardingTwoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingTwoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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