package com.susuryo.berryme

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.databinding.ActivityMessageListBinding
import com.susuryo.berryme.databinding.ItemChatBinding
import com.susuryo.berryme.model.ChatModel
import com.susuryo.berryme.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class MessageListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.adapter = ChatRecyclerViewAdapter(this, applicationContext)
        binding.recyclerView.layoutManager = LinearLayoutManager(layoutInflater.context)

        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    internal class ChatRecyclerViewAdapter(_activity : Activity, _context: Context) : RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {
        private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd hh:ss")
        private val context = _context

        private val uid: String = FirebaseAuth.getInstance().currentUser!!.uid
        private val activity = _activity
        private val chatModels: MutableList<ChatModel?> = ArrayList<ChatModel?>()
        private val destinationUsers = ArrayList<String?>()

        inner class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                with(chatModels[position]) {
                    var destinationUid: String? = null

                    for (user in this?.users?.keys!!) {
                        if (user != uid) {
                            destinationUid = user
                            destinationUsers.add(destinationUid)
                        }
                    }

                    Firebase.database.getReference("users").child(destinationUid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userModel = snapshot.getValue(UserModel::class.java)
                                Glide.with(context)
                                    .load(userModel!!.profileImageUrl)
                                    .apply(RequestOptions().circleCrop())
                                    .into(binding.chatitemImageview)
                                binding.chatitemTextviewTitle.text = userModel.username
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })

                    val commentMap: MutableMap<String, ChatModel.Comment> =
                        TreeMap<String, ChatModel.Comment>(
                            Collections.reverseOrder<Any>()
                        )

                    this.comments.let { commentMap.putAll(it) }
                    val lastMessageKey: String = if (commentMap.keys.toTypedArray().isNotEmpty()) {
                        commentMap.keys.toTypedArray()[0]
                    } else {
                        ""
                    }

                    binding.chatitemTextviewLastmessage.setText(
                        chatModels[position]?.comments?.get(
                            lastMessageKey
                        )?.message
                    )

                    binding.constraintLayout.setOnClickListener { view ->
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
                    val unixTime = this.comments[lastMessageKey]?.timestamp as Long
                    val date = Date(unixTime)
                    binding.chatitemTextviewTimestamp.text = simpleDateFormat.format(date)
                }
            }
        }

        init {
            Firebase.database.getReference("chatrooms").orderByChild("users/$uid")
                .equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        chatModels.clear()
                        for (item in snapshot.children) {
                            chatModels.add(item.getValue(ChatModel::class.java))
                        }

                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }


        override fun getItemCount(): Int {
            return chatModels.size
        }

    }

}