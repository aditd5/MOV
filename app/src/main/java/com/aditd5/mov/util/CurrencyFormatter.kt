package com.aditd5.mov.util

import java.text.NumberFormat
import java.util.Locale

class CurrencyFormatter {
    fun formatRupiah(currency: Int): String {
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id" , "ID"))
        formatRupiah.maximumFractionDigits = 0
        return formatRupiah.format(currency)
    }
}