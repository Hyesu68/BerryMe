package com.susuryo.berryme

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.susuryo.berryme.databinding.ActivitySignupBinding
import com.susuryo.berryme.model.UserModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupActivityImageviewProfile.setOnClickListener(View.OnClickListener {
            choosePictureDialog()
        })

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        binding.signupActivityButtonSignup.setOnClickListener { signUp() }
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
            .setNegativeButton("cancel") { dialog, which ->
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
                .into(binding.signupActivityImageviewProfile)
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
                .into(binding.signupActivityImageviewProfile)
        }
    }

    private fun signUp() {
        val email = binding.emailTextInput.editText?.text.toString()
        val name = binding.nameTextInput.editText?.text.toString()
        val password = binding.passwordTextInput.editText?.text.toString()
        val introduction = binding.infoTextInput.editText?.text.toString()

        if (email.isEmpty()) {
            binding.emailTextInput.error = "Email must not be empty"
        }
        if (name.isEmpty()) {
            binding.nameTextInput.error = "Name must not be empty"
        }
        if (password.isEmpty()) {
            binding.passwordTextInput.error = "Password must not be empty"
        }
        if (introduction.isEmpty()) {
            binding.infoTextInput.error = "Introduction must not be empty"
        }
        if (profileUri == null) {
            showErrorDialog("You must upload a profile picture")
        }

        if (email.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty() && introduction.isNotEmpty() && profileUri != null) {
            askFirebaseSignUp()
        }
    }

    private fun askFirebaseSignUp() {
        binding.signupactivityProgressbar.visibility = View.VISIBLE
        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(
                binding.emailTextInput.editText?.text.toString(),
                binding.passwordTextInput.editText?.text.toString())
            .addOnCompleteListener(this@SignupActivity) { task ->
                val uid = task.result.user!!.uid
                Firebase.storage.getReference("userImages").child(uid).putFile(profileUri!!)
                    .addOnCompleteListener { task ->
                        val result = task.result.storage.downloadUrl
                        result.addOnSuccessListener { uri ->
                            val imageUri = uri.toString()
                            val userModel = UserModel()
                            userModel.username = binding.nameTextInput.editText?.text.toString()
                            userModel.profileImageUrl = imageUri
                            userModel.uid = FirebaseAuth.getInstance().currentUser!!.uid
                            userModel.info = binding.infoTextInput.editText?.text.toString()
                            userModel.email = binding.emailTextInput.editText?.text.toString()

                            Firebase.database.getReference("users").child(uid)
                                .setValue(userModel)
                                .addOnSuccessListener {
                                    binding.signupactivityProgressbar.visibility = View.GONE
                                    finish()
                                }
                        }
                    }
            }
    }

    private fun showErrorDialog(str : String) {
        AlertDialog.Builder(this)
            .setTitle("Signup Failed")
            .setMessage(str)
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss()}
            .show()
    }

}