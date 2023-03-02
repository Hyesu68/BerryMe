package com.susuryo.berryme

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.susuryo.berryme.databinding.ActivityChangeProfileBinding
import com.susuryo.berryme.model.UserModel
//import kotlinx.android.synthetic.main.activity_change_profile.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChangeProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChangeProfileBinding
    private var imageUri: Uri? = null
    private var isImageChanged = false
    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.changeprofileactivityEdittextEmail.setOnClickListener {
            Toast.makeText(applicationContext, "이메일은 변경이 불가합니다.", Toast.LENGTH_SHORT).show()
        }

        val circularProgressDrawable = CircularProgressDrawable(applicationContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(applicationContext)
            .load(UserObject.userModel?.profileImageUrl)
            .apply(RequestOptions().centerCrop())
            .placeholder(circularProgressDrawable)
            .into(binding.changeprofileactivityImageviewProfile)

        binding.changeprofileactivityEdittextEmail.text = UserObject.userModel?.email
        binding.changeprofileactivityEdittextName.setText(UserObject.userModel?.username)
        binding.changeprofileactivityEdittextInfo.setText(UserObject.userModel?.info)

//        changeprofileactivity_imageview_profile.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = MediaStore.Images.Media.CONTENT_TYPE
//            startActivityForResult(intent, PICK_FROM_ALBUM)
//        }

        var Pictures = HashMap<String, UserModel.Picture>()
        FirebaseDatabase.getInstance().reference.child("users").child(UserObject.userModel?.uid!!).child("Pictures")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Pictures?.clear()
                    for (item in dataSnapshot.children) {
                        val picture: UserModel.Picture? = item.getValue(UserModel.Picture::class.java)
                        Pictures?.put(picture?.picUid!!, picture!!)
                    }

                   binding.changeprofileactivityButtonSignup.setOnClickListener {
                        var isNameChanged = (binding.changeprofileactivityEdittextName.text.toString() != UserObject.userModel?.username.toString())
                        var isInfoChanged = (binding.changeprofileactivityEdittextInfo.text.toString() != UserObject.userModel?.info)

                        if (isNameChanged) {
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(UserObject.userModel?.uid!!).child("username")
                                .setValue(binding.changeprofileactivityEdittextName.text.toString())
                                .addOnSuccessListener {
                                    isNameChanged = false
                                    if (!isNameChanged && !isInfoChanged && !isImageChanged) {
                                        Toast.makeText(
                                            applicationContext,
                                            "변경되었습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                        if (isInfoChanged) {
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(UserObject.userModel?.uid!!).child("info")
                                .setValue(binding.changeprofileactivityEdittextInfo.text.toString())
                                .addOnSuccessListener {
                                    isInfoChanged = false
                                    if (!isNameChanged && !isInfoChanged && !isImageChanged) {
                                        Toast.makeText(
                                            applicationContext,
                                            "변경되었습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }

                        if (isImageChanged) {
                            FirebaseStorage.getInstance()
                                .reference.child("userImages").child(UserObject.userModel?.uid!!)
                                .putFile(imageUri!!)
                                .addOnCompleteListener { task -> //
                                    val result = task.result.storage.downloadUrl
                                    result.addOnSuccessListener { uri ->
                                        val imageUri = uri.toString()
                                        FirebaseDatabase.getInstance().reference.child("users")
                                            .child(UserObject.userModel?.uid!!).child("profileImageUrl")
                                            .setValue(imageUri)
                                            .addOnSuccessListener {
                                                isImageChanged = false
                                                if (!isNameChanged && !isInfoChanged && !isImageChanged) {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "변경되었습니다.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.changeprofileactivityImageviewProfile.setOnClickListener {
            dialog = Dialog(ChangeProfileActivity@this)
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (dialog?.isShowing == true) dialog?.dismiss()
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == PICK_FROM_ALBUM) {
//                changeprofileactivity_imageview_profile!!.setImageURI(data!!.data) // 가운데 뷰를 바꿈
                imageUri = data?.data //이미지 경로 원본
                isImageChanged = true

                val circularProgressDrawable = CircularProgressDrawable(applicationContext)
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 30f
                circularProgressDrawable.start()
                Glide.with(applicationContext)
                    .load(imageUri)
                    .apply(RequestOptions().fitCenter())
                    .placeholder(circularProgressDrawable)
                    .into(binding.changeprofileactivityImageviewProfile)
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
            .into(binding.changeprofileactivityImageviewProfile)
    }


    companion object {
        private const val PICK_FROM_ALBUM = 15
        private const val REQUEST_TAKE_PHOTO = 1
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

}