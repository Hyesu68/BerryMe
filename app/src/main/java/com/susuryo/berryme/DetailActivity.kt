package com.susuryo.berryme

import android.animation.Animator
import android.app.ActivityOptions
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
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.google.firebase.database.*
import com.susuryo.berryme.model.PictureModel
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailActivity : AppCompatActivity() {
    private var destinationUid: String? = null
    private var name: String? = null
    private var profile: String? = null
    private var picuid: String? = null
    var picValue: PictureModel? = null
    var likesNum = 0
    lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        overridePendingTransition(R.anim.fromright, R.anim.none)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        destinationUid = intent.getStringExtra("Uid")
        name = intent.getStringExtra("name")
        listitem_textview_name.text = name
        profile = intent.getStringExtra("profile")
        var picurl = intent.getStringExtra("picurl")
        picuid = intent.getStringExtra("picuid")

        val circularProgressDrawable = CircularProgressDrawable(applicationContext)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        Glide.with(applicationContext)
            .load(profile)
            .apply(RequestOptions().circleCrop())
            .placeholder(circularProgressDrawable)
            .into(listitem_imageview_profile)

        listitem_textview_name.text = name
        listitem_textview_valuename.text = name

        Glide.with(applicationContext)
            .load(picurl)
            .apply(RequestOptions().centerCrop())
            .placeholder(circularProgressDrawable)
            .into(listitem_imageview_picture)

        back_button.setOnClickListener {
            onBackPressed()
        }

        setDetail()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.horizon_exit)
    }

    private fun setDetail() {
        FirebaseDatabase.getInstance().reference.child("pictures").child(picuid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    picValue = dataSnapshot.getValue(PictureModel::class.java)
                    picValue?.pictureKey = dataSnapshot.key
                    listitem_textview_value.text = picValue?.value
                    likesNum = if (picValue?.Likes != null) {
                        picValue?.Likes?.size!!
                    } else {
                        0
                    }

                    FirebaseDatabase.getInstance().reference.child("pictures").child(picuid!!).child("comment")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                detailactivity_comment_listview.adapter = null
                                val cmtArrayList = ArrayList<PictureModel.Comments>()
                                for (snapshot in dataSnapshot.children) {
                                    val cmtTmp = snapshot.getValue(PictureModel.Comments::class.java)
                                    cmtTmp?.key = snapshot.key
                                    cmtArrayList.add(cmtTmp!!)
                                }
                                val commentListAdapter = CommentListAdapter(applicationContext, cmtArrayList)
                                detailactivity_comment_listview.setOnItemLongClickListener { adapterView, view, i, l ->
                                    showCommentDialog(picuid, cmtArrayList[i].key, UserObject.userModel.uid == cmtArrayList[i].uid)
                                    return@setOnItemLongClickListener(true)
                                }
                                detailactivity_comment_listview.adapter = commentListAdapter
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:ss")
                    simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
                    val unixTime = picValue?.timestamp as Long
                    val date = Date(unixTime)
                    listitem_textview_time.text = simpleDateFormat.format(date)

//                    val likesSize = picValue?.Likes?.size
                    listitem_textview_likenum.text = likesNum.toString()
                    listitem_imageview_menu.setOnClickListener {
                        showDialog(picValue?.pictureKey, picValue?.uid == UserObject.userModel.uid)
                    }

                    var isLiked = false
                    if (picValue?.Likes != null) {
                        for (i in picValue?.Likes!!) {
                            if (i.key == UserObject.userModel.uid) {
                                isLiked = true
                                listitem_imageview_heart.setImageDrawable(
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
                                listitem_textview_likenum.text = likesNum.toString()
                                isLiked = false
                                listitem_imageview_heart.setImageDrawable(
                                    applicationContext.resources.getDrawable(
                                        R.drawable.icon_love_blank,
                                        applicationContext.theme
                                    )
                                )

                                FirebaseDatabase.getInstance().reference.child("pictures").child(picValue?.pictureKey!!).child("Likes").child(UserObject.userModel.uid!!)
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
                                listitem_textview_likenum.text = likesNum.toString()
                                isLiked = true
                                listitem_imageview_heart.setImageDrawable(
                                    applicationContext.resources.getDrawable(
                                        R.drawable.icon_love_filled,
                                        applicationContext.theme
                                    )
                                )

                                listitem_animation_like.visibility = View.VISIBLE
                                listitem_animation_like.playAnimation()
                                listitem_animation_like.addAnimatorListener(object : Animator.AnimatorListener {
                                    override fun onAnimationStart(p0: Animator?) {
                                    }

                                    override fun onAnimationEnd(p0: Animator?) {
                                        listitem_animation_like.visibility = View.GONE
                                    }

                                    override fun onAnimationCancel(p0: Animator?) {
                                    }

                                    override fun onAnimationRepeat(p0: Animator?) {
                                    }
                                })
                                FirebaseDatabase.getInstance().reference.child("pictures")
                                    .child(picValue?.pictureKey!!)
                                    .child("Likes")
                                    .child(UserObject.userModel.uid!!).setValue(true)
                                    .addOnSuccessListener {
                                    }
                            }
                        }
                    })
                    listitem_imageview_picture.setOnClickListener(doubleClick)

                    detailactivity_comment_button.setOnClickListener {
                        if (detailactivity_comment_edittext.text != null) {
                            val pictureComment = PictureModel.Comments()
                            pictureComment.uid = UserObject.userModel.uid
                            pictureComment.username = UserObject.userModel.username
                            pictureComment.value = detailactivity_comment_edittext.text.toString()
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
                                    detailactivity_comment_edittext.text = null
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
                            FirebaseDatabase.getInstance().reference.child("users").child(UserObject.userModel.uid!!).child("Pictures").child(key).removeValue()
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
                        .child(UserObject.userModel.uid!!)
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

    private fun showCommentDialog(uid: String?, key: String?, isMe: Boolean) {
        var builder = AlertDialog.Builder(this)
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
                            FirebaseDatabase.getInstance().reference.child("users").child(UserObject.userModel.uid!!)
                                .child("Pictures").child(key).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        applicationContext,
                                        "삭제되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    setDetail()
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
                    FirebaseDatabase.getInstance().reference.child("report").child("comment").child(key)
                        .child(UserObject.userModel.uid!!)
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

    class CommentListAdapter(val context: Context, val commentList: ArrayList<PictureModel.Comments>): BaseAdapter() {
        private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:ss")

        override fun getCount(): Int {
            return commentList.size
        }

        override fun getItem(p0: Int): Any {
            return commentList[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val view: View = LayoutInflater.from(context).inflate(R.layout.list_comment, null)
            val nickname = view.findViewById<TextView>(R.id.commentlist_nickname_textview)
            val comment = view.findViewById<TextView>(R.id.commentlist_comment_textview)
            val date = view.findViewById<TextView>(R.id.commentlist_date_textview)

            val cmtList = commentList[p0]
            nickname.text = cmtList.username
            nickname.setOnClickListener {
                val intent = Intent(view.context, MemberActivity::class.java)
                intent.putExtra("Uid", cmtList?.uid)
//                val activityOptions = ActivityOptions.makeCustomAnimation(
//                    view.context,
//                    R.anim.fromright,
//                    R.anim.toleft
//                )
                context.startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK))
            }

            comment.text = cmtList.value

            val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val unixTime = cmtList?.timestamp as Long
            val dateTime = Date(unixTime)
            date.text = simpleDateFormat.format(dateTime)

            return view
        }
    }
}