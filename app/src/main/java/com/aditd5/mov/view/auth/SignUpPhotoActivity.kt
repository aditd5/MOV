package com.aditd5.mov.view.auth

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aditd5.mov.databinding.ActivitySignUpPhotoBinding
import com.aditd5.mov.databinding.SelectImageDialogBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class SignUpPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpPhotoBinding

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var imgBitmap: Bitmap

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var imagePickerOldApi: ActivityResultLauncher<Intent>
    private lateinit var imagePickerNewApi: ActivityResultLauncher<PickVisualMediaRequest>

    private lateinit var loading: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPhotoBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        loading = LoadingDialog(this)

        setupPermissions()
        setUsername()
        setButtonListener()
    }

    private fun setupPermissions() {
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result->
            handleTakePictureResult(result)
        }

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleCameraPermissionResult(isGranted)
        }

        imagePickerOldApi = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uri = it.data?.data
            if (uri != null) {
                getBitmapFromUri(uri)
            }
        }

        imagePickerNewApi = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                getBitmapFromUri(uri)
            }
        }
    }

    private fun setUsername() {
        val fullname = "${Prefs.firstName} ${Prefs.lastName}"
        binding.tvName.text = fullname
    }

    private fun setButtonListener() {
        binding.apply {
            btnSelectPhoto.setOnClickListener {
                showDialog()
            }

            btnSkip.setOnClickListener {
                startActivity(Intent(this@SignUpPhotoActivity, HomeActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun setBtnUpload() {
        binding.apply {
            btnUpload.isEnabled = true

            btnUpload.setOnClickListener {
                uploadImgToFirebase(imgBitmap)
            }
        }
    }

    private fun showDialog() {
        val dialogBinding = SelectImageDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)

        if (dialogBinding.root.parent != null) {
            (dialogBinding.root.parent as ViewGroup).removeView(dialogBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.apply {
            btnGallery.setOnClickListener {
                pickImage()
                dialog.dismiss()
            }

            btnCamera.setOnClickListener {
                val isCameraPermissionGranted = ContextCompat.checkSelfPermission(this@SignUpPhotoActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

                if (isCameraPermissionGranted) {
                    takePicture()
                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun getBitmapFromUri(uri: Uri)  {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        imgBitmap = bitmap
        binding.ivProfile.setImageBitmap(imgBitmap)
        setBtnUpload()
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun handleTakePictureResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
//                val imgBitmap = it.extras?.get("data") as Bitmap
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra("data", Bitmap::class.java)
                } else {
                    result.data?.getParcelableExtra("data")
                }
                imgBitmap = bitmap!!
                binding.ivProfile.setImageBitmap(imgBitmap)
                setBtnUpload()
            }
        } else {
            Toast.makeText(this,
                "Gagal mengambil gambar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            takePicture()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imagePickerNewApi.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerOldApi.launch(intent)
        }
    }

    private fun uploadImgToFirebase(imgBitmap: Bitmap) {
        loading.showLoading()

        val storageRef = storage.reference
        val fileName = auth.currentUser?.uid
        val imagesRef = storageRef.child("img_user/$fileName.jpg")

        val baos = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imagesRef.putBytes(data, storageMetadata { contentType = "image/jpeg" })
            .addOnCompleteListener { task->
                if (task.isSuccessful) {
                    imagesRef.downloadUrl.addOnSuccessListener { uri->
                        setUserProfilePhoto(uri)
                    }
                } else {
                    val error = task.exception
                    Toast.makeText(
                        this,
                        error?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()
                    Log.e("signup photo", "upload photo failure", error)
                }
            }
    }

    private fun setUserProfilePhoto(uri: Uri) {
        val user = auth.currentUser

        if (user != null) {
            UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build().also {
                    user.updateProfile(it)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Prefs.imgProfileUri = uri.toString()
                                Toast.makeText(
                                    this@SignUpPhotoActivity,
                                    "Selamat Datang ${user.displayName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finishAffinity()
                                loading.dismissLoading()
                            } else {
                                val error = task.exception
                                Toast.makeText(
                                    this,
                                    error?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                                loading.dismissLoading()
                                Log.e("signup photo","update user profile failure", error)
                            }
                        }
                }
        }
    }
}