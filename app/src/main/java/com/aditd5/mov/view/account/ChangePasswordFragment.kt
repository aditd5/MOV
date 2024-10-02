package com.aditd5.mov.view.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.aditd5.mov.databinding.FragmentChangePasswordBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    private lateinit var loading: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        loading = LoadingDialog(requireActivity())

        setButtonListener()
        onBackPressedNewApi()
    }

    private fun setButtonListener() {
        binding.apply {
            btnBack.setOnClickListener {
                openAccountFragment()
            }

            btnChangePassword.setOnClickListener {
                val oldPassword = binding.etOldPassword.text.toString().trimStart().trimEnd()
                val newPassword = binding.etNewPassword.text.toString().trimStart().trimEnd()
                val newPasswordConfirm = binding.etConfirmNewPassword.text.toString().trimStart().trimEnd()

                if (oldPassword.isEmpty()) {
                    etOldPassword.error = "Kata sandi lama tidak boleh kosong"
                    etOldPassword.requestFocus()
                } else if (newPassword.isEmpty()) {
                    etNewPassword.error = "Kata sandi baru tidak boleh kosong"
                    etNewPassword.requestFocus()
                } else if (newPassword.length < 8) {
                    etNewPassword.error = "Kata sandi tidak boleh kurang dari 8 karakter"
                    etNewPassword.requestFocus()
                } else if (newPasswordConfirm.isEmpty()) {
                    etConfirmNewPassword.error = "Konfirmasi kata sandi baru tidak boleh kosong"
                    etConfirmNewPassword.requestFocus()
                } else if (newPasswordConfirm != newPassword) {
                    etConfirmNewPassword.error = "Konfirmasi kata sandi baru harus sama dengan kata sandi baru"
                    etConfirmNewPassword.requestFocus()
                } else {
                    changePassword(oldPassword, newPassword)
                }
            }
        }
    }

    private fun changePassword(oldPassword: String , newPassword: String) {
        loading.showLoading()

        val email = Prefs.email
        val credential = EmailAuthProvider.getCredential(email, oldPassword)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(
                            requireActivity(),
                            "Kata sandi berhasil diganti",
                            Toast.LENGTH_SHORT
                        ).show()
                        loading.dismissLoading()
                    } else {
                        val error = updateTask.exception
                        Toast.makeText(
                            requireActivity(),
                            error?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("change password", "change password failure", error)
                        loading.dismissLoading()
                    }
                }
            } else {
                val error = reauthTask.exception
                Toast.makeText(
                    requireActivity(),
                    error?.message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("change password", "reauthenticate failure", error)
                loading.dismissLoading()
            }
        }
    }

    private fun onBackPressedNewApi() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                openAccountFragment()
            }
        })
    }

    private fun openAccountFragment() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.putExtra("openFragment", "account")
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}