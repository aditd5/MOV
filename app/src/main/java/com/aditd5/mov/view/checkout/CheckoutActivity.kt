package com.aditd5.mov.view.checkout

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aditd5.mov.R.style
import com.aditd5.mov.databinding.ActivityCheckoutBinding
import com.aditd5.mov.databinding.TicketSuccessDialogBinding
import com.aditd5.mov.model.Checkout
import com.aditd5.mov.model.Movie
import com.aditd5.mov.util.CurrencyFormatter
import com.aditd5.mov.util.MidtransSdkConfig
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.uikit.api.model.CustomerDetails
import com.midtrans.sdk.uikit.api.model.ItemDetails
import com.midtrans.sdk.uikit.api.model.SnapTransactionDetail
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_CANCELED
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_FAILED
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_INVALID
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_PENDING
import com.midtrans.sdk.uikit.internal.util.UiKitConstants.STATUS_SUCCESS
import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Suppress("DEPRECATION")
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var db: FirebaseFirestore

    private var movie: Movie? = null
    private var totalPrice: Int? = null
    private var dateTimeEpoch: Long = 0L
    private var orderId: String? = null

    private lateinit var seats: ArrayList<String>

    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        db = FirebaseFirestore.getInstance()

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {    }

        initMidtransSDK()
        setButtonListener()
        setData()
    }

    private fun initMidtransSDK() {
        UiKitApi.Builder()
            .withContext(this)
            .withMerchantUrl(MidtransSdkConfig.MERCHANT_BASE_CHECKOUT_URL)
            .withMerchantClientKey(MidtransSdkConfig.MERCHANT_CLIENT_KEY)
            .enableLog(true)
            .build()
        uiKitCustomSetting()
    }

    private fun uiKitCustomSetting() {
        val uIKitCustomSetting = UiKitApi.getDefaultInstance().uiKitSetting
        uIKitCustomSetting.saveCardChecked = true
    }

    private fun setData() {
        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("movie", Movie::class.java)
        } else {
            intent.getParcelableExtra("movie")
        }
        seats = intent.getStringArrayListExtra("seats")!!
        dateTimeEpoch = intent.getLongExtra("dateTime", 0L)
        val price = movie!!.price

        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale("id", "ID")).format(dateTimeEpoch)
        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(dateTimeEpoch)
        binding.tvDate.text = dateFormat
        binding.tvTime.text = timeFormat

        val checkoutList: List<Checkout> = seats.map { Checkout(it, price) }
        val checkoutAdapter = CheckoutAdapter(checkoutList)

        binding.rvCheckout.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = checkoutAdapter
        }

        totalPrice = seats.size * price!!
        binding.tvTotalprice.text = CurrencyFormatter().formatRupiah(totalPrice!!)
    }

    private fun setButtonListener() {
        binding.apply {
            btnPay.setOnClickListener {
                goToPayment()
            }

            btnCancel.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun goToPayment() {
        val movieId = movie!!.movieId
        val movieName = movie!!.title
        val qty = seats.size
        val price = movie!!.price!!.toDouble()
        val amount = totalPrice!!.toDouble()
        val firstName = Prefs.firstName
        val lastName = Prefs.lastName
        val email = Prefs.email
        orderId = "MOV-" + System.currentTimeMillis().toString()

        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            activity = this ,
            launcher = launcher ,
            transactionDetails = SnapTransactionDetail(
                orderId = orderId!! ,     // id order
                grossAmount = amount
            ) ,
            customerDetails = CustomerDetails(
                firstName = firstName ,
                lastName =  lastName ,
                customerIdentifier = email ,
                email = email ,
//                phone = "085310102020"
            ) ,
            itemDetails = listOf(
                ItemDetails(
                    id = movieId ,     // id barang
                    price = price ,
                    quantity = qty ,
                    name = movieName
                )
            )
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent?) {
        if (resultCode == RESULT_OK) {
            val transactionResult =
                data?.getParcelableExtra<TransactionResult>(UiKitConstants.KEY_TRANSACTION_RESULT)
            if (transactionResult != null) {
                val transactionId = transactionResult.transactionId
                val paymentMethod = transactionResult.paymentType

                when (transactionResult.status) {
                    STATUS_SUCCESS -> {
                        saveTransactionToFirestore(transactionId!! , paymentMethod , "success")
                        Toast.makeText(
                            this ,
                            "Transaction Finished" ,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.apply {
                            btnPay.isVisible = false
                            btnCancel.isVisible = false
                        }
                    }

                    STATUS_PENDING -> {
                        Toast.makeText(
                            this ,
                            "Transaction Pending" ,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    STATUS_FAILED -> {
                        Toast.makeText(
                            this ,
                            "Transaction Failed" ,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    STATUS_CANCELED -> {
                        Toast.makeText(
                            this ,
                            "Transaction Cancelled" ,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    STATUS_INVALID -> {
                        Toast.makeText(
                            this ,
                            "Transaction Invalid" ,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this ,
                            transactionResult.status ,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this ,
                    "Transaction Invalid" ,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onActivityResult(requestCode , resultCode , data)
    }

    private fun successDialog() {
        val dialogBinding = TicketSuccessDialogBinding.inflate(this.layoutInflater)
        val dialog = Dialog(this, style.DialogTheme)

        if (dialogBinding.root.parent != null) {
            (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()

        dialogBinding.btnTicket.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("openFragment","ticket")
            startActivity(intent)
            finishAffinity()
            dialog.dismiss()
        }

        dialogBinding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
            dialog.dismiss()
        }
    }

    private fun saveTransactionToFirestore(
        transactionId: String ,
        paymentMethod: String ,
        status: String
    ) {
        val transactionData = hashMapOf(
            "userId" to user.uid ,
            "movieId" to movie!!.movieId ,
            "orderId" to orderId ,
            "seats" to seats ,
            "price" to totalPrice ,
            "passcode" to generatePasscode(transactionId) ,
            "status" to status ,
            "paymentMethod" to paymentMethod ,
            "showTime" to Timestamp(dateTimeEpoch) ,
            "createdAt" to serverTimestamp()
        )

        val docRef = db.collection("transactions").document(transactionId)
        docRef.set(transactionData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (status == "success") {
                        successDialog()
                    }
                } else {
                    saveTransactionToFirestore(transactionId , paymentMethod , status)
                    Log.e("checkout", "save transaction" , it.exception)
                }
            }
    }

    private fun generatePasscode(transactionId: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(transactionId.toByteArray())
        val hash = bytes.joinToString("") { "%02x".format(it) }
        return hash.take(6).uppercase()
    }
}