package com.susuryo.berryme

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.databinding.ActivityMemberBinding
import com.susuryo.berryme.databinding.GridItemBinding
import com.susuryo.berryme.fragment.MyFragment
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel
//import kotlinx.android.synthetic.main.activity_member.*
import java.util.*

class MemberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemberBinding
    private var destinationUid: String? = null
    private var name: String? = null
    private var profile: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.fromright, R.anim.none)

        destinationUid = intent.getStringExtra("Uid")
        binding.top.visibility = View.VISIBLE

        binding.messageButton.visibility = View.VISIBLE
        binding.profileButton.visibility = View.GONE
        binding.messageButton.setOnClickListener {
            val intent = Intent(applicationContext, MessageActivity::class.java)
            intent.putExtra("destinationUid", destinationUid)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    name = user?.username
                    binding.name.text = name

                    profile = user?.profileImageUrl
                    Glide.with(applicationContext)
                        .load(profile)
                        .apply(RequestOptions().circleCrop())
                        .into(binding.profile)

                    binding.info.text = user?.info
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        val pictures: MutableList<UserModel.Picture?> = ArrayList<UserModel.Picture?>()
        FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!).child("Pictures")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    pictures.clear()
                    for (item in dataSnapshot.children) {
                        val picture: UserModel.Picture? = item.getValue(UserModel.Picture::class.java)
                        pictures.add(picture)
                    }

                    binding.gridView.adapter = MyPictureAdapter(this@MemberActivity, destinationUid)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.horizon_exit)
    }

    private class MyPictureAdapter(_activity: Activity, _dUid: String?): RecyclerView.Adapter<MyPictureAdapter.ViewHolder>() {
        private val activity = _activity
        private val dUid = _dUid
        private var picture = mutableListOf<UserModel.Picture>()
        private class ViewHolder(val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root)

        init { getPictures() }

        fun getPictures() {
            Firebase.database.getReference("users").child(dUid!!).child("Pictures")
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
                        intent.putExtra("Uid", dUid)
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