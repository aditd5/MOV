package com.aditd5.mov.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.aditd5.mov.view.onboarding.OnBoardingOneActivity

class RoutingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Prefs.isLogin) {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        } else {
            startActivity(Intent(this, OnBoardingOneActivity::class.java))
            finishAffinity()
        }
    }
}