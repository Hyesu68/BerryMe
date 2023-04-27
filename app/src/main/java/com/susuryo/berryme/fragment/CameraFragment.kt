package com.susuryo.berryme.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.susuryo.berryme.*
import com.susuryo.berryme.R
import com.susuryo.berryme.databinding.FragmentCameraBinding
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
       binding.camerafragmentLocationRelativelayout.setOnClickListener {
//            val intent = Intent(requireContext(), LocationActivity::class.java)
//            requireContext().startActivity(intent)
        }

        binding.camerafragmentImageviewPicture.setOnClickListener { choosePictureDialog() }
        binding.shareButton.setOnClickListener { shareEvent() }

        return binding.root
    }

    private fun shareEvent() {
        val caption = binding.captionInput.editText?.text.toString()
        if (caption.isEmpty()) binding.captionInput.error = resources.getString(R.string.caption_not_empty)
        if (profileUri == null) Toast.makeText(requireContext(), resources.getString(R.string.profile_not_empty), Toast.LENGTH_SHORT).show()

        if (caption.isNotEmpty() && profileUri != null) { share(caption) }
    }

    private fun share(caption: String) {
        val uid = UserObject.userModel?.uid!!

        val dt = Date()
        val date = SimpleDateFormat("yyyyMMddHHmmss")
        val format = date.format(dt).toLong()
        val time = 100000000000000 - format
        val picName = time.toString() + UUID.randomUUID().toString()
        Firebase.storage.getReference("pictureImages").child(picName).putFile(profileUri!!)
            .addOnCompleteListener { task ->
                val result = task.result.storage.downloadUrl
                result.addOnSuccessListener { uri ->
                    val imageUri = uri.toString()
                    val pictureModel = PictureModel()
                    pictureModel.uid = uid
                    pictureModel.value = caption
                    pictureModel.pictureImageUrl = imageUri
                    pictureModel.timestamp = ServerValue.TIMESTAMP

                    FirebaseDatabase.getInstance().reference.child("pictures")
                        .child(picName)
                        .setValue(pictureModel)
                        .addOnSuccessListener {
                            val pictures = UserModel.Picture()
                            pictures.picUrl = imageUri
                            pictures.picUid = picName
                            FirebaseDatabase.getInstance().reference.child("users")
                                .child(uid).child("pictures").child(picName)
                                .setValue(pictures)
                                .addOnSuccessListener {
                                    binding.camerafragmentProgressbar.visibility =
                                        View.GONE
                                    val mainActivity = activity as MainActivity
                                    mainActivity.binding.mainactivityBottomnavigationview.selectedItemId =
                                        R.id.action_list
                                }
                        }

                }
            }

    }

    private fun choosePictureDialog() {
        val items = arrayOf(resources.getString(R.string.gallery), resources.getString(R.string.camera))
        MaterialAlertDialogBuilder(requireContext())
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

    private var profileUri: Uri? = null
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            profileUri = uri
            Glide.with(requireContext())
                .load(profileUri)
                .apply(RequestOptions().fitCenter())
                .into(binding.camerafragmentImageviewPicture)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            profileUri = data?.data
            Glide.with(requireContext())
                .load(profileUri)
                .apply(RequestOptions().fitCenter())
                .into(binding.camerafragmentImageviewPicture)
        }
    }

    private fun openSomeActivityForResult() {
        val intent = Intent(requireContext(), CameraActivity::class.java)
        resultLauncher.launch(intent)
    }

}