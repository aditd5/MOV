package com.aditd5.mov.view.account

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.aditd5.mov.R
import com.aditd5.mov.databinding.FragmentChangeProfileBinding
import com.aditd5.mov.databinding.SelectImageDialogBinding
import com.aditd5.mov.util.LoadingDialog
import com.aditd5.mov.util.Prefs
import com.aditd5.mov.view.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class ChangeProfileFragment : Fragment() {

    private var _binding: FragmentChangeProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var storage: FirebaseStorage

    private var imgUri: Uri? = null
    private var imgBitmap: Bitmap? = null

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private lateinit var imagePickerOldApi: ActivityResultLauncher<Intent>
    private lateinit var imagePickerNewApi: ActivityResultLauncher<PickVisualMediaRequest>

    private lateinit var loading: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater , container: ViewGroup? ,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangeProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View , savedInstanceState: Bundle?) {
        super.onViewCreated(view , savedInstanceState)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        storage = FirebaseStorage.getInstance()

        loading = LoadingDialog(requireActivity())

        setupPermissions()
        setUserData()
        setButtonListener()
        onBackPressedNewApi()
    }

    private fun setupPermissions() {
        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                takePicture()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Akses kamera ditolak",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result->
            if (result.resultCode == RESULT_OK) {
                result.data?.let {
//                val imgBitmap = it.extras?.get("data") as Bitmap
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        result.data?.getParcelableExtra("data", Bitmap::class.java)
                    } else {
                        result.data?.getParcelableExtra("data")
                    }
                    binding.ivProfile.setImageBitmap(bitmap)
                    imgBitmap = bitmap
                }
            }
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

    private fun setUserData() {
        val uri = user.photoUrl

        binding.apply {
            etFirstName.setText(Prefs.firstName)
            etLastName.setText(Prefs.lastName)

            if (uri != null) {
                Picasso.get()
                    .load(uri)
                    .into(ivProfile)
                btnDelete.visibility = View.VISIBLE
                imgUri = uri
            } else {
                btnAdd.visibility = View.VISIBLE
            }
        }
    }

    private fun setButtonListener() {
        binding.apply {
            val user = auth.currentUser

            btnBack.setOnClickListener {
                openAccountFragment()
            }

            btnSave.setOnClickListener {
                val firstName = etFirstName.text.toString().trimStart().trimEnd()
                val lastName = etLastName.text.toString().trimStart().trimEnd()

                if (user != null) {
                    if (imgBitmap != null) {
                        uploadImgToFirebase(imgBitmap!!, firstName, lastName)
                    } else {
                        updateUserProfile(firstName, lastName)
                    }
                }
            }

            btnSignOut.setOnClickListener {
                signOut()
            }

            ivProfile.setOnClickListener {
                showDialog()
            }

            btnAdd.setOnClickListener {
                showDialog()
            }

            btnDelete.setOnClickListener {
                deleteUserProfilePhoto()
            }
        }
    }

    private fun showDialog() {
        val dialogBinding = SelectImageDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(requireActivity())

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
                val isCameraPermissionGranted = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

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
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
        }
        binding.ivProfile.setImageBitmap(bitmap)
        imgBitmap = bitmap
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }

    private fun pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imagePickerNewApi.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerOldApi.launch(intent)
        }
    }

    private fun uploadImgToFirebase(imgBitmap: Bitmap , firstName: String , lastName: String) {
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

                        updateUserProfileWithProfilePhoto(uri, firstName, lastName)

                        binding.btnDelete.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Gagal mengupload photo",
                        Toast.LENGTH_SHORT
                    ).show()
                    loading.dismissLoading()
                    Log.e("account", "upload photo failure", task.exception)
                }
            }
    }

    private fun updateUserProfileWithProfilePhoto(uri: Uri , firstName: String , lastName: String) {
        UserProfileChangeRequest.Builder()
            .setDisplayName("$firstName $lastName")
            .setPhotoUri(uri)
            .build().also {
                user.updateProfile(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Prefs.firstName = firstName
                            Prefs.lastName = lastName
                            Prefs.imgProfileUri = uri.toString()
                            imgUri = uri

                            binding.etFirstName.clearFocus()
                            binding.etLastName.clearFocus()
                            Toast.makeText(
                                requireActivity() ,
                                "Profile Updated" ,
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnAdd.visibility = View.INVISIBLE
                            loading.dismissLoading()
                        } else {
                            Toast.makeText(
                                requireActivity() ,
                                "Gagal mengupdate profil" ,
                                Toast.LENGTH_SHORT
                            ).show()
                            loading.dismissLoading()
                            Log.e("account", "update profile and photo failure", task.exception)
                        }
                    }
            }
    }

    private fun updateUserProfile(firstName: String , lastName: String) {
        loading.showLoading()

        UserProfileChangeRequest.Builder()
            .setDisplayName("$firstName $lastName")
            .build().also {
                user.updateProfile(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Prefs.firstName = firstName
                            Prefs.lastName = lastName
                            binding.etFirstName.clearFocus()
                            binding.etLastName.clearFocus()
                            Toast.makeText(
                                requireActivity(),
                                "Profile Updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            loading.dismissLoading()
                        } else {
                            Toast.makeText(
                                requireActivity() ,
                                "Gagal mengupdate profil" ,
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("account", "update profile failure", task.exception)
                            loading.dismissLoading()
                        }
                    }
            }
    }

    private fun deleteUserProfilePhoto() {
        binding.loading.visibility = View.VISIBLE
        val imageRef = storage.getReferenceFromUrl(imgUri.toString())

        imageRef.delete()
            .addOnSuccessListener {
                binding.apply {
                    loading.visibility = View.INVISIBLE
                    btnDelete.visibility = View.INVISIBLE
                    ivProfile.setImageResource(R.drawable.ic_user_pic)

                    UserProfileChangeRequest.Builder()
                        .setPhotoUri(null)
                        .build().also {
                            user.updateProfile(it)
                                .addOnCompleteListener {task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            requireActivity(),
                                            "Foto profil dihapus",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        imgUri = null
                                        Prefs.imgProfileUri = null
                                        binding.btnAdd.visibility = View.VISIBLE
                                        binding.loading.visibility = View.INVISIBLE
                                    } else {
                                        binding.loading.visibility = View.INVISIBLE
                                    }
                                }
                        }
                }
            }
            .addOnFailureListener { e ->
                binding.loading.visibility = View.INVISIBLE
                Toast.makeText(
                    requireActivity(),
                    "Gagal menghapus foto profil",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("account", "delete image profile failure", e)
            }
    }

    private fun signOut() {
        loading.showLoading()
        auth.signOut()

        Prefs.isLogin = false
        Prefs.firstName = ""
        Prefs.lastName = ""
        Prefs.email = ""
        Prefs.imgProfileUri = null
        imgUri = null

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Prefs.isGuest = true
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                    requireActivity().finishAffinity()
                    loading.dismissLoading()
                } else {
                    Toast.makeText(requireActivity(), "Gagal Sign Out, silahkan ulangi kembali", Toast.LENGTH_SHORT).show()
                    Log.e("account", "sign out failure", task.exception)
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