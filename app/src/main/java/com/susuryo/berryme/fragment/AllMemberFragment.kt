package com.susuryo.berryme.fragment

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
import com.susuryo.berryme.databinding.FragmentAllMemberBinding
import com.susuryo.berryme.databinding.GridItemBinding
import com.susuryo.berryme.model.PictureModel

class AllMemberFragment: Fragment() {
    private lateinit var binding: FragmentAllMemberBinding
    private lateinit var adapter: MyPictureAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllMemberBinding.inflate(layoutInflater)

        adapter = MyPictureAdapter()
        binding.recyclerView.adapter = adapter

        return binding.root
    }


    private class MyPictureAdapter: RecyclerView.Adapter<MyPictureAdapter.ViewHolder>() {
        private var picture = mutableListOf<PictureModel>()
        private class ViewHolder(val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root)

        init { getPictures() }

        fun getPictures() {
            Firebase.database.getReference("pictures")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        picture.clear()

                        for (item in snapshot.children) {
                            item.getValue(PictureModel::class.java)?.let { picture.add(it) }
                        }
//                        picture.reverse()

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
                        .load(this.pictureImageUrl)
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
                }
            }
        }

        override fun getItemCount() = picture.size
    }

}