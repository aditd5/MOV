package com.aditd5.mov.view.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivityOnBoardingBinding
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding

    private val image1 = R.drawable.ic_now_playing
    private val image2 = R.drawable.ic_pre_sale
    private val image3 = R.drawable.ic_cashless

    private var currentImage = image1

    private val imageDescription = mapOf(
        image1 to Pair(
            R.string.now_playing_en,
            R.string.on_boarding_one
        ),
        image2 to Pair(
            R.string.pre_sale,
            R.string.on_boarding_two
        ),
        image3 to Pair(
            R.string.cashless,
            R.string.on_boarding_three
        )
    )

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Prefs.isGuest || Prefs.isLogin) {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }

        auth = FirebaseAuth.getInstance()

        binding.imageView.setImageResource(image1)

        updateDots()
        setButtonListener()
    }

    private fun setButtonListener() {
        binding.apply {
            btnNext.setOnClickListener {
                currentImage = when(currentImage) {
                    image1 -> image2
                    image2 -> image3
                    else -> image1
                }

                if (currentImage == image1) {
                    initGuestUser()
                    startActivity(Intent(this@OnBoardingActivity , HomeActivity::class.java))
                    finishAffinity()
                } else {
                    imageView.setImageResource(currentImage)
                    val description = imageDescription[currentImage] ?: Pair(0, 0)
                    tvTitle.setText(description.first)
                    tvDesc.setText(description.second)
                    updateDots()
                }
            }

            btnSkip.setOnClickListener {
                initGuestUser()
                startActivity(Intent(this@OnBoardingActivity , HomeActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun updateDots() {
        val inactiveColor = getColor(R.color.colorGrey)
        val activeColor = getColor(R.color.colorPink)

        binding.apply {
            dot1.setTextColor(if (currentImage == image1) activeColor else inactiveColor)
            dot2.setTextColor(if (currentImage == image2) activeColor else inactiveColor)
            dot3.setTextColor(if (currentImage == image3) activeColor else inactiveColor)
        }
    }

    private fun initGuestUser() {
        auth.signInAnonymously()
        Prefs.isGuest = true
    }
}