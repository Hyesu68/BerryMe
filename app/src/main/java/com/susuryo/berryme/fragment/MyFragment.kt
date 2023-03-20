package com.susuryo.berryme.fragment

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.*
import com.susuryo.berryme.databinding.ActivityMemberBinding
import com.susuryo.berryme.databinding.GridItemBinding
import com.susuryo.berryme.model.PictureModel
//import com.susuryo.berryme.databinding.FragmentCameraBinding
import com.susuryo.berryme.model.UserModel
//import kotlinx.android.synthetic.main.activity_member.*

class MyFragment : Fragment() {
    private lateinit var binding: ActivityMemberBinding
    lateinit var user: UserModel
    private lateinit var adapter: MyPicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMemberBinding.inflate(inflater, container, false)
        user = UserObject.userModel!!

        binding.profileButton.setOnClickListener { editProfile() }

        Glide.with(requireContext())
            .load(UserObject.userModel?.profileImageUrl)
            .apply(RequestOptions().circleCrop())
            .into(binding.profile)
        binding.name.text = UserObject.userModel?.username
        binding.info.text = UserObject.userModel?.info

        adapter = MyPicAdapter(requireActivity())
        binding.gridView.adapter = adapter

        return binding.root
    }

    private fun editProfile() {
        val intent = Intent(requireContext(), ChangeProfileActivity::class.java)
        val activityOptions = ActivityOptions.makeCustomAnimation(
            requireContext(),
            R.anim.fromright,
            R.anim.toleft
        )
        startActivity(intent, activityOptions.toBundle())
    }

    private class MyPicAdapter(_activity: Activity): RecyclerView.Adapter<MyPicAdapter.ViewHolder>() {
        private val activity = _activity
        private var picture = mutableListOf<UserModel.Picture>()
        private class ViewHolder(val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root)

        init { getPictures() }

        fun getPictures() {
            Firebase.database.getReference("users").child(UserObject.userModel?.uid!!).child("pictures")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        picture.clear()

                        for (item in snapshot.children) {
                            item.getValue(UserModel.Picture::class.java)?.let { picture.add(it) }
                        }
                        picture.reverse()

                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val binding = GridItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                with(picture[position]) {
                    binding.progressBar.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(this.picUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.progressBar.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.progressBar.visibility = View.GONE
                                return false
                            }
                        })
                        .apply(RequestOptions().fitCenter())
                        .centerCrop()
                        .into(binding.gridImageView)

                    binding.gridImageView.setOnClickListener {
                        val intent = Intent(binding.root.context, DetailActivity::class.java)
                        intent.putExtra("Uid", UserObject.userModel?.uid)
                        intent.putExtra("picuid", this.picUid)
                        val activityOptions = ActivityOptions.makeCustomAnimation(
                            binding.root.context,
                            R.anim.fromright,
                            R.anim.toleft
                        )
                        activity.startActivity(intent, activityOptions.toBundle())
                    }
                }
            }
        }

        override fun getItemCount() = picture.size
    }

}