package com.susuryo.berryme.fragment

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.susuryo.berryme.MessageActivity
import com.susuryo.berryme.R
import com.susuryo.berryme.model.ChatModel
import com.susuryo.berryme.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class MessageFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.peoplefragment_recyclerview)
        recyclerView.adapter = ChatRecyclerViewAdapter(requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        return view
    }

    override fun onResume() {
        super.onResume()
    }

    internal class ChatRecyclerViewAdapter(_activity : Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd hh:ss")

        private val uid: String = FirebaseAuth.getInstance().currentUser!!.uid
        private val activity = _activity
        private val chatModels: MutableList<ChatModel?> = ArrayList<ChatModel?>()
        private val destinationUsers = ArrayList<String?>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val customViewHolder = holder as CustomViewHolder
            var destinationUid: String? = null

            //챗방에 있는 유저 체크
            for (user in chatModels[position]?.users?.keys!!) {
                if (user != uid) {
                    destinationUid = user
                    destinationUsers.add(destinationUid)
                }
            }
            FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userModel = snapshot.getValue(UserModel::class.java)
                        Glide.with(customViewHolder.itemView.context)
                            .load(userModel!!.profileImageUrl)
                            .apply(RequestOptions().circleCrop())
                            .into(customViewHolder.imageView)
                        customViewHolder.textview_title.text = userModel.username
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            //메시지를 내림 차순으로 정렬 후 마지막 메시지의 키값을 가져옴
            val commentMap: MutableMap<String, ChatModel.Comment> =
                TreeMap<String, ChatModel.Comment>(
                    Collections.reverseOrder<Any>()
                )
            chatModels[position]?.comments?.let { commentMap.putAll(it) }
            val lastMessageKey : String = if (commentMap.keys.toTypedArray().isNotEmpty()) {
                commentMap.keys.toTypedArray()[0]
            } else {
                ""
            }
            customViewHolder.textview_last_message.setText(
                chatModels[position]?.comments?.get(
                    lastMessageKey
                )?.message
            )
            customViewHolder.itemView.setOnClickListener { view ->
                val intent = Intent(view.context, MessageActivity::class.java)
                intent.putExtra("destinationUid", destinationUsers[position])
                val activityOptions =
                    ActivityOptions.makeCustomAnimation(
                        view.context,
                        R.anim.fromright,
                        R.anim.toleft
                    )
                activity.startActivity(intent, activityOptions.toBundle())
            }

            //TimeStamp
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val unixTime = chatModels[position]?.comments?.get(lastMessageKey)?.timestamp as Long
            val date = Date(unixTime)
            customViewHolder.textview_timestamp.setText(simpleDateFormat.format(date))
        }

        override fun getItemCount(): Int {
            return chatModels.size
        }

        private inner class CustomViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            var imageView: ImageView
            var textview_title: TextView
            var textview_last_message: TextView
            var textview_timestamp: TextView

            init {
                imageView = view.findViewById(R.id.chatitem_imageview)
                textview_title = view.findViewById(R.id.chatitem_textview_title)
                textview_last_message = view.findViewById(R.id.chatitem_textview_lastmessage)
                textview_timestamp = view.findViewById(R.id.chatitem_textview_timestamp)
            }
        }

        init {
            FirebaseDatabase.getInstance().reference.child("chatrooms").orderByChild("users/$uid")
                .equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        chatModels.clear()
                        for (item in snapshot.children) {
                            chatModels.add(item.getValue(ChatModel::class.java))
                        }
//                        chatModels.reverse()
/*
                        val sortedChatrooms = chatModels.sortedByDescending {
                            val comments = it?.comments
                            if (comments != null) {
                                val lastComment = comments.values.maxBy { it.timestamp as Long }
                                lastComment?.timestamp ?: 0
                            } else {
                                0
                            }
                        }*/
                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

}