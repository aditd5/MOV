package com.aditd5.mov.view.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ActivityHomeBinding
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.account.AccountFragment
import com.aditd5.mov.view.auth.SignInActivity
import com.aditd5.mov.view.ticket.TicketFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , v.paddingBottom)    // Jika menggunakan system bar bottom akan merubah height bottom navbar
            insets
        }

        val intent = intent.getStringExtra("openFragment")

        when(intent) {
            "ticket" -> {
                loadFragment(TicketFragment())
                binding.navView.selectedItemId = R.id.navigation_ticket
            }
            "account" -> {
                loadFragment(AccountFragment())
                binding.navView.selectedItemId = R.id.navigation_account
            }
            else -> {
                loadFragment(HomeFragment())
            }
        }

        if (Prefs.isGuest) {
            binding.navView.menu.findItem(R.id.navigation_account).title = "Sign In"
        }

        binding.navView.apply {
            itemIconTintList = null
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_home -> {
                        loadFragment(HomeFragment())
                    }

                    R.id.navigation_account -> {
                        if (Prefs.isLogin) {
                            loadFragment(AccountFragment())
                        } else {
                            startActivity(Intent(this@HomeActivity, SignInActivity::class.java))
                        }
                    }

                    else -> {
                        R.id.navigation_ticket
                        loadFragment(TicketFragment())
                    }
                }
                true
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 101)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment_activity_home, fragment)
        transaction.commit()
    }
}