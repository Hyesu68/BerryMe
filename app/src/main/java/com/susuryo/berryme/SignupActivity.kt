package com.susuryo.berryme

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
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
    private var imageUri: Uri? = null
    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupActivityImageviewProfile.setOnClickListener(View.OnClickListener {
            val items = arrayOf("Gallery", "Camera")
            MaterialAlertDialogBuilder(this)
                .setTitle("CHOOSE")
                .setItems(items) { dialog, which ->
                    // Do something for item chosen
                    when (which) {
                        0 -> {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = MediaStore.Images.Media.CONTENT_TYPE
                            startActivityForResult(intent, PICK_FROM_ALBUM)
                        }
                        1 -> {
                            dispatchTakePictureIntent()
                        }
                    }
                }
                .setNegativeButton("cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        })

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }

        binding.signupActivityButtonSignup.setOnClickListener { signUp() }
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
        if (imageUri == null) {
            showErrorDialog("You must upload a profile picture")
        }

        if (email.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty() && introduction.isNotEmpty() && imageUri != null) {
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
                FirebaseStorage.getInstance()
                    .reference.child("userImages").child(uid).putFile(imageUri!!)
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

                            FirebaseDatabase.getInstance().reference.child("users").child(uid)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (dialog?.isShowing == true) dialog?.dismiss()
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_ALBUM) {
//                signupActivity_imageview_profile!!.setImageURI(data!!.data) // 가운데 뷰를 바꿈
                imageUri = data?.data //이미지 경로 원본

                val circularProgressDrawable = CircularProgressDrawable(applicationContext)
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 30f
                circularProgressDrawable.start()
                Glide.with(applicationContext)
                    .load(imageUri)
                    .apply(RequestOptions().fitCenter())
                    .placeholder(circularProgressDrawable)
                    .into(binding.signupActivityImageviewProfile)
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                galleryAddPic()
            }
        } else {
            Toast.makeText(applicationContext, "Try Again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            imageUri = Uri.fromFile(f)
            applicationContext.sendBroadcast(mediaScanIntent)
        }

        val circularProgressDrawable = CircularProgressDrawable(applicationContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(applicationContext)
            .load(imageUri)
            .apply(RequestOptions().fitCenter())
            .placeholder(circularProgressDrawable)
            .into(binding.signupActivityImageviewProfile)
    }

    companion object {
        private const val PICK_FROM_ALBUM = 15
        private const val REQUEST_TAKE_PHOTO = 1
    }

    private lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        val permissionlistener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
//                Toast.makeText(this@SignupActivity, "Permission Granted", Toast.LENGTH_SHORT).show()

                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    takePictureIntent.resolveActivity(applicationContext.packageManager)?.also {
                        val photoFile: File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            null
                        }

                        photoFile?.also {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                applicationContext,
                                "com.susuryo.berryme.fileprovider",
                                it
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                        }
                    }
                }
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@SignupActivity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.create()
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(android.Manifest.permission.CAMERA)
            .check()

    }
}