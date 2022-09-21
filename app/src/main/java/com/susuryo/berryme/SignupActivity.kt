package com.susuryo.berryme

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.susuryo.berryme.model.UserModel
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var imageUri: Uri? = null
    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val splash_backgrond = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_color))
        signupActivity_imageview_profile.setOnClickListener(View.OnClickListener {
            dialog = Dialog(SignupActivity@this)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setContentView(R.layout.dialog_camera)

            val gallery = dialog?.findViewById<TextView>(R.id.cameradialog_gallery_textview)
            gallery?.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                startActivityForResult(intent, PICK_FROM_ALBUM)
            }

            val camera = dialog?.findViewById<TextView>(R.id.cameradialog_camera_textview)
            camera?.setOnClickListener {
                dispatchTakePictureIntent()
            }

            dialog?.show()
        })

        back_button.setOnClickListener {
            onBackPressed()
        }

        signupActivity_button_signup.setBackgroundColor(Color.parseColor(splash_backgrond))
        signupActivity_button_signup.setOnClickListener(View.OnClickListener {
            if (signupActivity_edittext_email.getText()
                    .toString() == null || signupActivity_edittext_name.getText()
                    .toString() == null || signupActivity_edittext_password.getText()
                    .toString() == null || imageUri == null
            ) {
                return@OnClickListener
            }

            signupactivity_progressbar.visibility = View.VISIBLE
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(
                    signupActivity_edittext_email.getText().toString(),
            signupActivity_edittext_password.getText().toString()
            )
                .addOnCompleteListener(this@SignupActivity) { task ->
                    //                            String uid = task.getResult().getUser().getUid();
                    val uid = task.result.user!!.uid
                    FirebaseStorage.getInstance()
                        .reference.child("userImages").child(uid).putFile(imageUri!!)
                        .addOnCompleteListener { task -> //                                                @SuppressWarnings("VisibleForTests")
//                                                String imageUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            val result = task.result.storage.downloadUrl
                            result.addOnSuccessListener { uri ->
                                val imageUri = uri.toString()

                                val userModel = UserModel()
                                userModel.username = signupActivity_edittext_name.getText().toString()
                                userModel.profileImageUrl = imageUri
                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser()!!.uid
                                userModel.info = signupActivity_edittext_info.text.toString()
                                userModel.email = signupActivity_edittext_email.text.toString()

                                FirebaseDatabase.getInstance().reference.child("users").child(uid)
                                    .setValue(userModel)
                                    .addOnSuccessListener {
                                        signupactivity_progressbar.visibility = View.GONE
                                        finish()
                                    }
                            }
                        }
                }
        })
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
                    .into(signupActivity_imageview_profile)
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

//        BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
//            signupActivity_imageview_profile.setImageBitmap(bitmap)
//        }

        val circularProgressDrawable = CircularProgressDrawable(applicationContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(applicationContext)
            .load(imageUri)
            .apply(RequestOptions().fitCenter())
            .placeholder(circularProgressDrawable)
            .into(signupActivity_imageview_profile)
    }

    companion object {
        private const val PICK_FROM_ALBUM = 15
        private const val REQUEST_TAKE_PHOTO = 1
    }

    lateinit var currentPhotoPath: String

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
}