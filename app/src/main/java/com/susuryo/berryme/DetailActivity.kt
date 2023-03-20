package com.susuryo.berryme

import android.animation.Animator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.databinding.ActivityDetailBinding
import com.susuryo.berryme.databinding.ListCommentBinding
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailBinding
    private var destinationUid: String? = null
    private var picuid: String? = null
    var likesNum = 0
    lateinit var imm: InputMethodManager
    private lateinit var dUser : UserModel
    private lateinit var picValue: PictureModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.fromright, R.anim.none)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        destinationUid = intent.getStringExtra("Uid")
        Firebase.database.getReference("users").child(destinationUid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    dUser = snapshot as UserModel
                    dUser = snapshot.getValue(UserModel::class.java)!!
                    binding.listitemTextviewName.text = dUser.username

                    Glide.with(applicationContext)
                        .load(dUser.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(binding.listitemImageviewProfile)

                    binding.listitemTextviewName.text = dUser.username
                    binding.listitemTextviewValuename.text = dUser.username
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        picuid = intent.getStringExtra("picuid")
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        setDetail()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.horizon_exit)
    }

    private fun setDetail() {
        Firebase.database.getReference("pictures").child(picuid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    picValue = dataSnapshot.getValue(PictureModel::class.java)!!

                    picValue.pictureKey = dataSnapshot.key
                    binding.listitemTextviewValue.text = picValue.value
                    likesNum = if (picValue.Likes != null) {
                        picValue.Likes?.size!!
                    } else {
                        0
                    }

                    Glide.with(applicationContext)
                        .load(picValue.pictureImageUrl)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.listitemImageviewPicture)

                    val commentListAdapter = CommentListAdapter(applicationContext, picuid!!)
                    binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
                    binding.recyclerView.adapter = commentListAdapter

                    /*FirebaseDatabase.getInstance().reference.child("pictures").child(picuid!!).child("comment")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                binding.detailactivityCommentListview.adapter = null
                                val cmtArrayList = ArrayList<PictureModel.Comments>()
                                for (snapshot in dataSnapshot.children) {
                                    val cmtTmp = snapshot.getValue(PictureModel.Comments::class.java)
                                    cmtTmp?.key = snapshot.key
                                    cmtArrayList.add(cmtTmp!!)
                                }
                                val commentListAdapter = CommentListAdapter(applicationContext, cmtArrayList)
                                binding.detailactivityCommentListview.setOnItemLongClickListener { adapterView, view, i, l ->
                                    showCommentDialog(picuid, cmtArrayList[i].key, UserObject.userModel?.uid == cmtArrayList[i].uid)
                                    return@setOnItemLongClickListener(true)
                                }
                                binding.detailactivityCommentListview.adapter = commentListAdapter
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })*/
                    val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:ss")
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val unixTime = picValue?.timestamp as Long
                    val date = Date(unixTime)
                    binding.listitemTextviewTime.text = simpleDateFormat.format(date)

//                    val likesSize = picValue?.Likes?.size
                    binding.listitemTextviewLikenum.text = likesNum.toString()
                    binding.listitemImageviewMenu.setOnClickListener {
                        showDialog(picValue.pictureKey, picValue.uid == UserObject.userModel?.uid)
                    }

                    var isLiked = false
                    if (picValue.Likes != null) {
                        for (i in picValue.Likes!!) {
                            if (i.key == UserObject.userModel?.uid) {
                                isLiked = true
                                binding.listitemImageviewHeart.setImageDrawable(
                                    applicationContext.resources.getDrawable(
                                        R.drawable.icon_love_filled,
                                        applicationContext.theme
                                    )
                                )
                            }
                        }
                    }

                    val doubleClick = DoubleClick(object : DoubleClickListener {
                        override fun onSingleClickEvent(view: View?) { }

                        override fun onDoubleClickEvent(view: View?) {
                            if (isLiked) {
                                likesNum -= 1
                                if (likesNum < 0) {
                                    likesNum = 0
                                }
                                binding.listitemTextviewLikenum.text = likesNum.toString()
                                isLiked = false
                                binding.listitemImageviewHeart.setImageDrawable(
                                    applicationContext.resources.getDrawable(
                                        R.drawable.icon_love_blank,
                                        applicationContext.theme
                                    )
                                )

                                FirebaseDatabase.getInstance().reference.child("pictures").child(picValue?.pictureKey!!).child("Likes").child(UserObject.userModel?.uid!!)
                                    .removeValue()
                                    .addOnSuccessListener {
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            applicationContext,
                                            "문제가 발생하였습니다. 잠시 후 다시 시도하세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                likesNum += 1
                                binding.listitemTextviewLikenum.text = likesNum.toString()
                                isLiked = true
                                binding.listitemImageviewHeart.setImageDrawable(
                                    applicationContext.resources.getDrawable(
                                        R.drawable.icon_love_filled,
                                        applicationContext.theme
                                    )
                                )

                                binding.listitemAnimationLike.visibility = View.VISIBLE
                                binding.listitemAnimationLike.playAnimation()
                                binding.listitemAnimationLike.addAnimatorListener(object : Animator.AnimatorListener {

                                    override fun onAnimationStart(p0: Animator) {

                                    }

                                    override fun onAnimationEnd(p0: Animator) {
                                        binding.listitemAnimationLike.visibility = View.GONE
                                    }

                                    override fun onAnimationCancel(p0: Animator) {

                                    }

                                    override fun onAnimationRepeat(p0: Animator) {

                                    }
                                })

                                FirebaseDatabase.getInstance().reference.child("pictures")
                                    .child(picValue?.pictureKey!!)
                                    .child("Likes")
                                    .child(UserObject.userModel?.uid!!).setValue(true)
                            }
                        }
                    })
                    binding.listitemImageviewPicture.setOnClickListener(doubleClick)

                    binding.detailactivityCommentButton.setOnClickListener {
                        if (binding.detailactivityCommentEdittext.text != null) {
                            val pictureComment = PictureModel.Comments()
                            pictureComment.uid = UserObject.userModel?.uid
                            pictureComment.username = UserObject.userModel?.username
                            pictureComment.value = binding.detailactivityCommentEdittext.text.toString()
                            pictureComment.timestamp = ServerValue.TIMESTAMP

                            val dt = Date()
                            val date = SimpleDateFormat("yyyyMMddHHmmss")
                            val format = date.format(dt).toLong()
                            val time = 100000000000000 - format
                            val commentName = time.toString() + UUID.randomUUID().toString()
                            FirebaseDatabase.getInstance().reference.child("pictures")
                                .child(picValue?.pictureKey!!).child("comment").child(commentName)
                                .setValue(pictureComment)
                                .addOnSuccessListener {
                                    binding.detailactivityCommentEdittext.text = null
                                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                                    setDetail()
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showDialog(key: String?, isMe: Boolean) {
        var builder = AlertDialog.Builder(this)
        var menu = if (isMe) {
            R.array.list_me
        } else {
            R.array.list_other
        }
        builder.setItems(menu) { dialog, which ->
            if (key != null) {
                if (isMe) {
                    FirebaseDatabase.getInstance().reference.child("pictures").child(key).removeValue()
                        .addOnSuccessListener {
                            FirebaseDatabase.getInstance().reference.child("users").child(UserObject.userModel?.uid!!).child("pictures").child(key).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        applicationContext,
                                        "삭제되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        applicationContext,
                                        "문제가 발생하였습니다. 잠시 후 다시 시도하세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                "문제가 발생하였습니다. 잠시 후 다시 시도하세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    FirebaseDatabase.getInstance().reference.child("report").child("notice").child(key)
                        .child(UserObject.userModel?.uid!!)
                        .setValue(true)
                        .addOnSuccessListener{
                            Toast.makeText(
                                applicationContext,
                                "신고되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
        builder.show()
    }


    class CommentListAdapter(val context: Context, pictureId: String): RecyclerView.Adapter<CommentListAdapter.ViewHolder>() {
        val dataSet = mutableListOf<PictureModel.Comments>()

        inner class ViewHolder(val binding: ListCommentBinding) : RecyclerView.ViewHolder(binding.root)
        override fun getItemCount() = dataSet.size

        init {
            Firebase.database.getReference("pictures").child(pictureId).child("comment")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dataSet.clear()
                        for (item in snapshot.children) {
                            item.getValue(PictureModel.Comments::class.java)?.let { dataSet.add(it) }
                        }
                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val binding = ListCommentBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                with(dataSet[position]) {
                    binding.commentlistNicknameTextview.text = this.username
                    binding.commentlistNicknameTextview.setOnClickListener {
                        val intent = Intent(context, MemberActivity::class.java)
                        intent.putExtra("Uid", this.uid)
                        context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
                    }

                    binding.commentlistCommentTextview.text = this.value

                    val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val unixTime = this.timestamp as Long
                    val dateTime = Date(unixTime)
                    binding.commentlistDateTextview.text = simpleDateFormat.format(dateTime)
                }
            }
        }

        private fun showCommentDialog(uid: String?, key: String?, isMe: Boolean) {
            var builder = AlertDialog.Builder(context)
            var menu = if (isMe) {
                R.array.list_me
            } else {
                R.array.list_other
            }
            builder.setItems(menu) { dialog, which ->
                if (key != null) {
                    if (isMe) {
                        FirebaseDatabase.getInstance().reference.child("pictures").child(uid!!)
                            .child("comment").child(key).removeValue()
                            .addOnSuccessListener {
                                FirebaseDatabase.getInstance().reference.child("users").child(UserObject.userModel?.uid!!)
                                    .child("pictures").child(key).removeValue()
                                    .addOnSuccessListener {

                                    }
                                    .addOnFailureListener {

                                    }
                            }
                            .addOnFailureListener {

                            }
                    } else {
                        FirebaseDatabase.getInstance().reference.child("report").child("comment").child(key)
                            .child(UserObject.userModel?.uid!!)
                            .setValue(true)
                            .addOnSuccessListener{

                            }
                    }
                }
            }
            builder.show()
        }
    }
}