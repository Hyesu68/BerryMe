package com.susuryo.berryme.fragment

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.susuryo.berryme.*
import com.susuryo.berryme.R
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment() {

    lateinit var picture : ImageView
    lateinit var text : TextView
    lateinit var button : Button
    lateinit var locationImage: ImageView
    private var imageUri: Uri? = null
    var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_camera, container, false)
        picture = view.findViewById(R.id.camerafragment_imageview_picture)
        text = view.findViewById(R.id.camerafragment_edittext_text)
        button = view.findViewById(R.id.camerafragment_button_register)
        locationImage = view.findViewById(R.id.camerafragment_location_imageview)
        locationImage.setOnClickListener {
            val intent = Intent(requireContext(), LocationActivity::class.java)
            requireContext().startActivity(intent)
        }

        picture.setOnClickListener(View.OnClickListener {
            dialog = Dialog(requireContext())
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

        button.setOnClickListener {
            if (imageUri != null && text.text.toString().isNotEmpty()) {
                button.isClickable = false
                camerafragment_progressbar.visibility = View.VISIBLE

                val uid = UserObject.userModel.uid!!
                FirebaseDatabase.getInstance().reference.child("users").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userModel: UserModel? = snapshot.getValue(UserModel::class.java)
                            var profileUrl = userModel?.profileImageUrl
                            var username = userModel?.username

                            val dt = Date()
                            val date = SimpleDateFormat("yyyyMMddHHmmss")
                            val format = date.format(dt).toLong()
                            val time = 100000000000000 - format
                            val picName = time.toString() + UUID.randomUUID().toString()
                            FirebaseStorage.getInstance()
                                .reference.child("pictureImages").child(picName).putFile(imageUri!!)
                                .addOnCompleteListener { task ->
                                    val result = task.result.storage.downloadUrl
//                                    val name = task.result.storage.name
                                    result.addOnSuccessListener { uri ->
                                        val imageUri = uri.toString()
                                        val pictureModel = PictureModel()
                                        pictureModel.uid = uid
//                                        pictureModel.username = username
                                        pictureModel.value = text.text.toString()
                                        pictureModel.pictureImageUrl = imageUri
//                                        pictureModel.profileImageUrl = profileUrl
                                        pictureModel.timestamp = ServerValue.TIMESTAMP

                                        FirebaseDatabase.getInstance().reference.child("pictures")
                                            .child(picName)
                                            .setValue(pictureModel)
                                            .addOnSuccessListener {
                                                val pictures = UserModel.Picture()
                                                pictures.picUrl = imageUri
                                                pictures.picUid = picName
                                                FirebaseDatabase.getInstance().reference.child("users")
                                                    .child(uid).child("Pictures").child(picName)
                                                    .setValue(pictures)
                                                    .addOnSuccessListener {
                                                        camerafragment_progressbar.visibility =
                                                            View.GONE
                                                        var mainActivity = activity as MainActivity
//                                                        mainActivity.setFragment(R.id.action_list)
                                                        mainActivity.bottomNavigationView.selectedItemId =
                                                            R.id.action_list
                                                    }
                                            }

                                    }
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            camerafragment_progressbar.visibility = View.GONE

                            button.isClickable = true
                            Toast.makeText(context, error.toString() + "", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })

            } else {
                Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (dialog?.isShowing == true) dialog?.dismiss()
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == PICK_FROM_ALBUM) {
//                camerafragment_imageview_picture.setImageURI(data!!.data) // 가운데 뷰를 바꿈
                imageUri = data?.data //이미지 경로 원본
//                picture.visibility = View.GONE
//                camerafragment_imageview_userpic.visibility = View.VISIBLE

//                picture.setImageURI(imageUri)

                val circularProgressDrawable = CircularProgressDrawable(requireContext())
                circularProgressDrawable.strokeWidth = 5f
                circularProgressDrawable.centerRadius = 30f
                circularProgressDrawable.start()
                Glide.with(requireContext())
                    .load(imageUri)
                    .apply(RequestOptions().fitCenter())
                    .placeholder(circularProgressDrawable)
                    .into(picture)
            } else if (requestCode == REQUEST_TAKE_PHOTO) {
                galleryAddPic()
            }
        } else {
            Toast.makeText(requireContext(), "Try Again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            imageUri = Uri.fromFile(f)
            requireContext().sendBroadcast(mediaScanIntent)
        }

//        BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
//            picture.setImageBitmap(bitmap)
//        }

        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(requireContext())
            .load(imageUri)
            .apply(RequestOptions().fitCenter())
            .placeholder(circularProgressDrawable)
            .into(picture)
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
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
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
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.susuryo.berryme.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

//                    val intent = Intent(Intent.ACTION_DIAL)
//                    intent.data = Uri.parse("tel:" + "phone_number")

                    if (Build.VERSION.SDK_INT > 23) {
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                    } else {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(requireContext(), "Permission Not Granted ", Toast.LENGTH_SHORT).show()
                        } else {
                            val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.CAMERA)
                            ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS_STORAGE,9)
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)

                        }
                    }
                }
            }
        }
    }
}