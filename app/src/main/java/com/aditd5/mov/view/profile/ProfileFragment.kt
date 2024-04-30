package com.aditd5.mov.view.profile

import android.Manifest
import android.app.Activity.RESULT_OK
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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aditd5.mov.R
import com.aditd5.mov.databinding.ChooseImageDialogBinding
import com.aditd5.mov.databinding.FragmentProfileBinding
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.auth.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var imgUri: String

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var imagePicker: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        setupPermissions()
        setUserData()
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

    private fun setUserData() {
        val user = auth.currentUser
        val uri = Prefs.imgProfileUri

        if (user != null) {
            binding.apply {
                etName.setText(user.displayName)
                etEmail.setText(user.email)
            }
        }

        if (uri != null) {
            Picasso.get()
                .load(uri)
                .into(binding.ivProfile)
            binding.btnDelete.visibility = View.VISIBLE
        }
    }

    private fun mainButton() {
        binding.apply {
            btnSave.setOnClickListener {
                val user = auth.currentUser
                val name = etName.text.toString()
                val email = etEmail.text.toString()

                if (user != null) {
                    val profileUpdate =  UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user.updateProfile(profileUpdate)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Prefs.name = name
                                etName.clearFocus()
                            } else {
                                Toast.makeText(requireContext(), "Gagal mengupdate profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

            btnSignOut.setOnClickListener {
               signOut()
            }

            ivProfile.setOnClickListener {
                showChooseOpenDialog()
            }

            btnDelete.setOnClickListener {
                deleteImgFromFirebase()
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        Prefs.isLogin = false
        Prefs.name = ""
        Prefs.imgProfileUri = null
        startActivity(Intent(activity, SignInActivity::class.java))
        activity?.finishAffinity()
    }

    private fun showChooseOpenDialog() {
        val chooseImageDialogBinding = ChooseImageDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireActivity())

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
                val isCameraPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

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
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        }
        uploadImgToFirebase(bitmap)
    }

    private fun handleTakePictureResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                val imgBitmap = it.extras?.get("data") as Bitmap
                uploadImgToFirebase(imgBitmap)
            }
        } else {
            Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            takePicture()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
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
                        binding.loading.visibility = View.INVISIBLE
                        val uriString = uri.toString()
                        imgUri = uriString
                        Picasso.get()
                            .load(uriString)
                            .into(binding.ivProfile)
                        Prefs.imgProfileUri = imgUri
                        binding.btnDelete.visibility = View.VISIBLE
                    }
                } else {
                    binding.loading.visibility = View.INVISIBLE
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengupload photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun deleteImgFromFirebase() {
        binding.loading.visibility = View.VISIBLE
        val uri = Prefs.imgProfileUri
        val imageRef = storage.getReferenceFromUrl(uri!!)

        imageRef.delete()
            .addOnSuccessListener {
                binding.apply {
                    loading.visibility = View.INVISIBLE
                    btnDelete.visibility = View.INVISIBLE
                    ivProfile.setImageResource(R.drawable.ic_user_pic)
                    Prefs.imgProfileUri = null
                }
            }
            .addOnFailureListener {
                binding.loading.visibility = View.INVISIBLE
                Log.e("ProfileFragment", it.message.toString())
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}