package com.aditd5.mov.util

import com.chibatching.kotpref.KotprefModel

object Prefs: KotprefModel() {
    var isLogin by booleanPref(false)
    var name by stringPref()
    var imgProfileUri by nullableStringPref(null)
}