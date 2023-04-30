package com.susuryo.berryme.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.susuryo.berryme.DetailActivity
import com.susuryo.berryme.MemberActivity
import com.susuryo.berryme.R
import com.susuryo.berryme.databinding.FragmentAllMemberBinding
import com.susuryo.berryme.databinding.GridItemBinding
import com.susuryo.berryme.databinding.ItemSearchBinding
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel

class AllMemberFragment: Fragment() {
    private lateinit var binding: FragmentAllMemberBinding
    private lateinit var adapter: MyPictureAdapter
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllMemberBinding.inflate(layoutInflater)

        adapter = MyPictureAdapter()
        binding.recyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                searchUser()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (binding.searchView.query.isEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.result.visibility = View.GONE
                }
                return true
            }
        })

        return binding.root
    }

    private fun searchUser() {
        val search = binding.searchView.query.toString()

        if (search.isNotEmpty()) {
            Firebase.database.getReference("users")
//                .orderByChild("username")
//                .startAt(search)
//                .endAt(search + "\uf8ff")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val result = mutableListOf<UserModel>()
                        for (item in snapshot.children) {
                            val tmp = item.getValue(UserModel::class.java)
                            if (tmp != null && tmp.username?.lowercase()?.contains(search) == true) {
                                result.add(tmp)
                            }
                        }

                        if (result.isNotEmpty()) {
                            binding.recyclerView.visibility = View.GONE
                            binding.result.visibility = View.VISIBLE

                            searchAdapter = SearchAdapter(result)
                            binding.result.layoutManager = LinearLayoutManager(layoutInflater.context)
                            binding.result.adapter = searchAdapter
                        } else {
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.result.visibility = View.GONE
                            Toast.makeText(requireContext(), resources.getString(R.string.no_result), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }

    private class SearchAdapter(var data: MutableList<UserModel>): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
        private class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                with(data[position]) {
                    binding.text.text = this.username
                    Glide.with(holder.itemView.context)
                        .load(this.profileImageUrl)
                        .circleCrop()
                        .into(binding.profile)

                    binding.constraint.setOnClickListener {
                        val intent = Intent(holder.itemView.context, MemberActivity::class.java)
                        intent.putExtra("Uid", this.uid)
                        intent.putExtra("profile", this.profileImageUrl)
                        intent.putExtra("info", this.info)
                        intent.putExtra("name", this.username)
                        val activityOptions = ActivityOptions.makeCustomAnimation(
                            holder.itemView.context,
                            R.anim.fromright,
                            R.anim.toleft
                        )
                        holder.itemView.context.startActivity(intent, activityOptions.toBundle())
                    }
                }
            }
        }

        override fun getItemCount() = data.size
    }

    private class MyPictureAdapter: RecyclerView.Adapter<MyPictureAdapter.ViewHolder>() {
        private var picture = mutableListOf<PictureModel>()
        private var pictureId = mutableListOf<String>()
        private class ViewHolder(val binding: GridItemBinding) : RecyclerView.ViewHolder(binding.root)

        init { getPictures() }

        fun getPictures() {
            Firebase.database.getReference("pictures")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        picture.clear()
                        pictureId.clear()
                        for (item in snapshot.children) {
                            item.getValue(PictureModel::class.java)?.let {
                                if (it.isPrivate == null || it.isPrivate == false) {
                                    picture.add(it)
                                    item.key?.let { it1 -> pictureId.add(it1) }
                                }
                            }
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

                    holder.binding.frameLayout.setOnClickListener {
                        val intent = Intent(holder.itemView.context, DetailActivity::class.java)
                        intent.putExtra("Uid", this.uid)
                        intent.putExtra("name", this.username)
                        intent.putExtra("profile", this.profileImageUrl)
                        intent.putExtra("picurl", this.pictureImageUrl)
                        intent.putExtra("picuid", pictureId[position])
                        val activityOptions = ActivityOptions.makeCustomAnimation(
                            holder.itemView.context,
                            R.anim.fromright,
                            R.anim.toleft
                        )
                        holder.itemView.context.startActivity(intent, activityOptions.toBundle())
                    }
                }
            }
        }

        override fun getItemCount() = picture.size
    }

}