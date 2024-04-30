package com.aditd5.mov.util

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import com.aditd5.mov.databinding.ViewLoadingBinding

class LoadingDialog(private val activity: Activity) {
    private lateinit var loadingBinding: ViewLoadingBinding
    private lateinit var dialog: Dialog

    fun showLoading() {
        loadingBinding = ViewLoadingBinding.inflate(activity.layoutInflater)
        dialog = Dialog(activity)

        if (loadingBinding.root.parent != null) {
            (loadingBinding.root.parent as ViewGroup).removeView(loadingBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(loadingBinding.root)
        dialog.setCancelable(false)
        dialog.show()
    }

    fun dismissLoading() {
        dialog.dismiss()
    }
}