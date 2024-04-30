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
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.aditd5.mov.databinding.ActivitySignUpPhotoBinding
import com.aditd5.mov.databinding.ChooseImageDialogBinding
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class SignUpPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpPhotoBinding

    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var imgUri: String
    private lateinit var imgBitmap: Bitmap

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var imagePicker: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        setupPermissions()
        setUsername()
        mainButton()
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

        imagePicker = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uri = it.data?.data
            if (uri != null) {
                getBitmapFromUri(uri)
            }
        }
    }

    private fun setUsername() {
        val name = Prefs.name
        binding.tvName.text = name
    }

    private fun mainButton() {
        binding.apply {
            btnSelectPhoto.setOnClickListener {
                showChooseOpenDialog()
            }

            btnSkip.setOnClickListener {
                startActivity(
                    Intent(
                        this@SignUpPhotoActivity,
                        HomeActivity::class.java
                    )
                )
                finishAffinity()
            }
        }
    }

    private fun setBtnUpload() {
        binding.apply {
            btnUpload.visibility = View.VISIBLE
            btnUpload.setOnClickListener {
                uploadImgToFirebase(imgBitmap)
            }
        }
    }

    private fun showChooseOpenDialog() {
        val chooseImageDialogBinding = ChooseImageDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)

        if (chooseImageDialogBinding.root.parent != null) {
            (chooseImageDialogBinding.root.parent as ViewGroup).removeView(chooseImageDialogBinding.root)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(chooseImageDialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        chooseImageDialogBinding.apply {
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

    @Suppress("DEPRECATION")
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
                val imgBitmap = it.extras?.get("data") as Bitmap
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
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(
                Build.VERSION_CODES.R) >= 2) {
            Intent(MediaStore.ACTION_PICK_IMAGES)
        } else {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
        imagePicker.launch(intent)
    }

    private fun uploadImgToFirebase(imgBitmap: Bitmap) {
        binding.loading.visibility = View.VISIBLE

        val storageRef = storage.reference
        val fileName = auth.currentUser?.uid
        val imagesRef = storageRef.child("img_user/$fileName.jpg")

        val baos = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imagesRef.putBytes(data, storageMetadata { contentType = "image/jpeg" })
            .addOnCompleteListener {task->
                if (task.isSuccessful) {
                    imagesRef.downloadUrl.addOnSuccessListener { uri->
                        val uriString = uri.toString()
                        imgUri = uriString
                        Picasso.get()
                            .load(uriString)
                            .into(binding.ivProfile)
                        Prefs.imgProfileUri = imgUri
                        startActivity(
                            Intent(
                                this,
                                HomeActivity::class.java
                            )
                        )
                        binding.loading.visibility = View.INVISIBLE
                    }
                } else {
                    binding.loading.visibility = View.INVISIBLE
                    Toast.makeText(
                        this,
                        "Gagal mengupload photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}