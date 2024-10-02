package com.aditd5.mov.view.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aditd5.mov.R
import com.aditd5.mov.databinding.FragmentAccountBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage

    private lateinit var loading: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        storage = FirebaseStorage.getInstance()

        loading = LoadingDialog(requireActivity())

        setButtonListener()
        setData()
    }

    private fun setData() {
        binding.apply {
            val fullname = "${Prefs.firstName} ${Prefs.lastName}"
            val email = Prefs.email
            val uri = Prefs.imgProfileUri
            val verified = user.isEmailVerified

            tvUsername.text = fullname

            Picasso.get()
                .load(uri)
                .into(ivProfile)

            if (verified) {
                tvEmail.text = email
            } else {
                tvEmail.text = getString(R.string.verify_email)
                btnVerifyEmail.visibility = View.VISIBLE
            }
        }
    }

    private fun setButtonListener() {
        binding.apply {
            btnVerifyEmail.setOnClickListener {
                verifyEmail()
            }

            btnChangeProfile.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.nav_host_fragment_activity_home, ChangeProfileFragment())
                transaction.commit()
            }

            btnChangePassword.setOnClickListener {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.nav_host_fragment_activity_home, ChangePasswordFragment())
                transaction.commit()
            }

            btnChangeLanguage.setOnClickListener {

            }

            btnHelp.setOnClickListener {

            }
        }
    }

    private fun verifyEmail() {
        loading.showLoading()

        user.sendEmailVerification()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Email verifikasi telah dikirim",
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()
                } else {
                    val error = it.exception
                    Toast.makeText(
                        requireContext(),
                        error?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("account", "send email verification failure", error)
                    loading.dismissLoading()
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}