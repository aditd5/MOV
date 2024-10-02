package com.aditd5.mov.util

import com.chibatching.kotpref.KotprefModel

object Prefs: KotprefModel() {
    var isLogin by booleanPref(false)
    var isGuest by booleanPref(false)
    var firstName by stringPref()
    var lastName by nullableStringPref("")
    var email by stringPref()
    var imgProfileUri by nullableStringPref(null)
}