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
    private var isImageChanged = false

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
           editProfile()
       }

        binding.cameraImageView.setOnClickListener {
            choosePictureDialog()
        }
    }

    private fun editProfile() {
        val name = binding.nameTextInput.editText?.text.toString()
        val info = binding.infoTextInput.editText?.text.toString()

        binding.nameTextInput.isEnabled = false
        binding.infoTextInput.isEnabled = false
        binding.button.isEnabled = false

        val userModel = UserObject.userModel
        userModel?.username = name
        userModel?.info = info

        Firebase.database.getReference("users").child(userModel?.uid!!).setValue(userModel)
            .addOnSuccessListener {
                if (isImageChanged) {
                    Firebase.storage.getReference("userImages").child(UserObject.userModel?.uid!!)
                        .putFile(profileUri!!)
                        .addOnCompleteListener { task -> //
                            binding.nameTextInput.isEnabled = true
                            binding.infoTextInput.isEnabled = true
                            binding.button.isEnabled = true

                            val result = task.result.storage.downloadUrl
                            result.addOnSuccessListener { uri ->
                                val imageUri = uri.toString()
                                Firebase.database.getReference("users")
                                    .child(UserObject.userModel?.uid!!).child("profileImageUrl")
                                    .setValue(imageUri)
                                    .addOnSuccessListener {
                                        Toast.makeText(applicationContext, resources.getString(R.string.change_succeed), Toast.LENGTH_SHORT).show()
                                        userModel.profileImageUrl = imageUri
                                        UserObject.userModel = userModel
                                        Toast.makeText(
                                            applicationContext,
                                            resources.getString(R.string.change_succeed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                            }
                        }
                } else {
                    Toast.makeText(applicationContext, resources.getString(R.string.change_succeed), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnCompleteListener {
                binding.nameTextInput.isEnabled = true
                binding.infoTextInput.isEnabled = true
                binding.button.isEnabled = true
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
            isImageChanged = true
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
            isImageChanged = true
            Glide.with(applicationContext)
                .load(profileUri)
                .apply(RequestOptions().fitCenter())
                .into(binding.imageProfile)
        }
    }
}