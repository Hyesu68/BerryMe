package com.susuryo.berryme

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.susuryo.berryme.model.ChatModel
import com.susuryo.berryme.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity : AppCompatActivity() {
    private var destinationUid: String? = null
    private var button: Button? = null
    private var editText: EditText? = null
    private var uid: String? = null
    private var chatRoomUid: String? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        uid = FirebaseAuth.getInstance().currentUser!!.uid //채팅 요구하는 아이디
        destinationUid = intent.getStringExtra("destinationUid")
        button = findViewById(R.id.messageactivity_button)
        editText = findViewById(R.id.messageactivity_edittext)
        recyclerView = findViewById(R.id.messageactivity_recyclerview)
        button?.setOnClickListener {
//            val chatModel = ChatModel()
//            val chatUser = chatModel.users.toMutableMap()
//            chatUser[uid!!] = true
//            chatUser[destinationUid!!] = true
            val chat = mutableMapOf<String, Boolean>()
            chat[uid!!] = true
            chat[destinationUid!!] = true
            if (chatRoomUid == null) {
                button!!.setEnabled(false)
                FirebaseDatabase.getInstance().reference.child("chatrooms").push()
                    .child("users")
                    .setValue(chat).addOnSuccessListener {
                        checkChatRoom()
                    }
            } else {
                sendMsg()
            }
        }
        checkChatRoom()
    }

    fun sendMsg() {
        val comment: ChatModel.Comment = ChatModel.Comment()
        comment.uid = uid
        comment.message = editText?.text.toString()
        comment.timestamp = ServerValue.TIMESTAMP
        FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomUid!!)
            .child("comments").push().setValue(comment).addOnCompleteListener {
                editText?.setText("")
            }
    }

    fun checkChatRoom() {
        FirebaseDatabase.getInstance().reference.child("chatrooms").orderByChild("users/$uid")
            .equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (item in dataSnapshot.children) {
                        val chatModel: ChatModel? = item.getValue(ChatModel::class.java)
                        if (chatModel?.users?.containsKey(destinationUid) == true) {
                            chatRoomUid = item.key
                            button!!.isEnabled = true
                            recyclerView!!.layoutManager = LinearLayoutManager(this@MessageActivity)
                            recyclerView!!.adapter = RecyclerViewAdapter(chatRoomUid,
                                recyclerView!!, uid, destinationUid
                            )

                            if (editText?.text?.isNotEmpty() == true) {
                                sendMsg()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    internal class RecyclerViewAdapter(_chatRoomUid: String?, _recyclerView: RecyclerView, _uid: String?, _destinationUid: String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        var comments: MutableList<ChatModel.Comment?> = ArrayList<ChatModel.Comment?>()
        var uid = _uid
        var destinationUid = _destinationUid
        var userModel: UserModel? = null
        var chatRoomUid = _chatRoomUid
        var recyclerView = _recyclerView
        val messageList: Unit
            get() {
                chatRoomUid?.let {
                    FirebaseDatabase.getInstance().reference.child("chatrooms").child(it)
                        .child("comments").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                comments.clear()
                                for (item in dataSnapshot.children) {
                                    comments.add(item.getValue(ChatModel.Comment::class.java))
                                }
                                notifyDataSetChanged()
                                recyclerView.scrollToPosition(comments.size - 1)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val messageViewHolder = holder as MessageViewHolder

            //내가 보낸 메시지
            if (comments[position]?.uid.equals(uid)) {
                messageViewHolder.textview_message.setText(comments[position]?.message)
                messageViewHolder.textview_message.setBackgroundResource(R.drawable.rightbubble)
                messageViewHolder.linearlayout_destination.visibility = View.INVISIBLE
                messageViewHolder.linearlayout_main.gravity = Gravity.RIGHT
            } else {    //상대방이 보낸 메시지
                Glide.with(holder.itemView.context)
                    .load(userModel?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(messageViewHolder.imageview_profile)
                messageViewHolder.textview_name.setText(userModel?.username)
                messageViewHolder.linearlayout_destination.visibility = View.VISIBLE
                messageViewHolder.textview_message.setBackgroundResource(R.drawable.leftbubble)
                messageViewHolder.textview_message.setText(comments[position]?.message)
                messageViewHolder.textview_message.textSize = 25f
                messageViewHolder.linearlayout_main.gravity = Gravity.LEFT
            }
            val unixTime = comments[position]?.timestamp as Long
            val date = Date(unixTime)
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"))
            val time: String = simpleDateFormat.format(date)
            messageViewHolder.textview_timestamp.text = time
        }

        override fun getItemCount(): Int {
            return comments.size
        }

        init {
            FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        userModel = dataSnapshot.getValue(UserModel::class.java)
                        messageList
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textview_message: TextView
        var textview_name: TextView
        var imageview_profile: ImageView
        var linearlayout_destination: LinearLayout
        var linearlayout_main: LinearLayout
        var textview_timestamp: TextView

        init {
            textview_message = view.findViewById(R.id.messageitem_textview_message)
            textview_name = view.findViewById(R.id.messageitem_textview_name)
            imageview_profile = view.findViewById(R.id.messageitem_imageview_profile)
            linearlayout_destination = view.findViewById(R.id.messageitem_linearlayout_destination)
            linearlayout_main = view.findViewById(R.id.messageitem_lineatlayout_main)
            textview_timestamp = view.findViewById(R.id.messageitem_textview_timestamp)
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fromleft, R.anim.toright)
    }
}