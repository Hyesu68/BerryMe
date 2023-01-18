package com.susuryo.berryme.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.susuryo.berryme.MainActivity
import com.susuryo.berryme.R
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel

class MessageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_list, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.peoplefragment_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        var mainActivity = activity as MainActivity
        recyclerView.adapter = MessageFragmentRecyclerViewAdapter(mainActivity)
        return view
    }

    internal class MessageFragmentRecyclerViewAdapter(_activity: Activity) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var activity: Activity = _activity
        private lateinit var context: Context

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.message_item_list,
                parent,
                false
            )
            context = parent.context
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            var profileImageView: ImageView = view.findViewById(R.id.message_itemlist_profile)
            var nameTextView: ImageView = view.findViewById(R.id.message_textview_name)
            var textTextView: ImageView = view.findViewById(R.id.message_textview_text)
            var newImageView: ImageView = view.findViewById(R.id.message_imageview_new)
        }

        init {
            FirebaseDatabase.getInstance().reference.child("messages")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        pictureModels.clear()
                        for (snapshot in dataSnapshot.children) {
                            var picValue: PictureModel? = snapshot.getValue(PictureModel::class.java)
                            picValue?.pictureKey = snapshot.key
                            pictureModels.add(picValue)
                        }

                        for (i: Int in 0 until pictureModels.size) {
                            FirebaseDatabase.getInstance().reference.child("users").child(pictureModels[i]?.uid!!)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val userTmp: UserModel? = dataSnapshot.getValue(UserModel::class.java)
                                        pictureModels[i]?.profileImageUrl = userTmp?.profileImageUrl
                                        pictureModels[i]?.username = userTmp?.username
                                        if (i == pictureModels.size - 1) {
                                            notifyDataSetChanged()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }
    }
}