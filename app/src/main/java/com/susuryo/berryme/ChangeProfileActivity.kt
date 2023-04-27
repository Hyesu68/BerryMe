package com.susuryo.berryme

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.susuryo.berryme.databinding.ActivityChangeProfileBinding

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChangeProfileBinding
    private var imageUri: Uri? = null
    private var isImageChanged = false
    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        Glide.with(applicationContext)
            .load(UserObject.userModel?.profileImageUrl)
            .apply(RequestOptions().circleCrop())
            .into(binding.imageProfile)

        binding.emailTextInput.editText?.setText(UserObject.userModel?.email)
        binding.nameTextInput.editText?.setText(UserObject.userModel?.username)
        binding.infoTextInput.editText?.setText(UserObject.userModel?.info)
        binding.emailTextInput.isEnabled = false

       binding.button.setOnClickListener {
           val name = binding.nameTextInput.editText?.text.toString()
           val info = binding.infoTextInput.editText?.text.toString()
           val userModel = UserObject.userModel
           userModel?.username = name
           userModel?.info = info

           Firebase.database.getReference("users").child(userModel?.uid!!).setValue(userModel)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext,resources.getString(R.string.change_succeed),Toast.LENGTH_SHORT).show()
                    if (isImageChanged) {
                        Firebase.storage.getReference("userImages").child(UserObject.userModel?.uid!!).putFile(imageUri!!)
                            .addOnCompleteListener { task -> //
                                val result = task.result.storage.downloadUrl
                                result.addOnSuccessListener { uri ->
                                    val imageUri = uri.toString()
                                    Firebase.database.getReference("users").child(UserObject.userModel?.uid!!).child("profileImageUrl")
                                        .setValue(imageUri)
                                        .addOnSuccessListener {
                                            userModel.profileImageUrl = imageUri
                                            UserObject.userModel = userModel
                                            Toast.makeText(applicationContext,resources.getString(R.string.change_succeed),Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                }
                            }
                    } else {
                        finish()
                    }
                }
        }

        binding.cameraImageView.setOnClickListener {
            choosePictureDialog()
        }
    }

    private fun choosePictureDialog() {
        val items = arrayOf(resources.getString(R.string.gallery), resources.getString(R.string.camera))
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.choose))
            .setItems(items) { dialog, which ->
                // Do something for item chosen
                when (which) {
                    0 -> {
                        bringPictureFromGallery()
                    }
                    1 -> {
                        openSomeActivityForResult()
                    }
                }
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


    private fun bringPictureFromGallery() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun openSomeActivityForResult() {
        val intent = Intent(this, CameraActivity::class.java)
        resultLauncher.launch(intent)
    }

    private var profileUri: Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            profileUri = uri
            Glide.with(applicationContext)
                .load(profileUri)
                .apply(RequestOptions().fitCenter())
                .into(binding.imageProfile)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            profileUri = data?.data
            Glide.with(applicationContext)
                .load(profileUri)
                .apply(RequestOptions().fitCenter())
                .into(binding.imageProfile)
        }
    }
}