package com.aditd5.mov.util

import android.app.Application
import com.chibatching.kotpref.Kotpref

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
    }
}