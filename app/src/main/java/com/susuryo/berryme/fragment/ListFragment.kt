package com.susuryo.berryme.fragment

import android.animation.Animator
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.*
import com.susuryo.berryme.R
import com.susuryo.berryme.model.PictureModel
import com.susuryo.berryme.model.UserModel
import java.text.SimpleDateFormat
import java.util.*

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_list, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.peoplefragment_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(inflater.context)
        var mainActivity = activity as MainActivity
        recyclerView.adapter = ListFragmentRecyclerViewAdapter(mainActivity)
        return view
    }

    internal class ListFragmentRecyclerViewAdapter(_activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var activity: Activity = _activity
        private val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:ss")
        private lateinit var context: Context

        var pictureModels: MutableList<PictureModel?> = ArrayList<PictureModel?>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_list,
                parent,
                false
            )
            context = parent.context
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            Glide.with(holder.itemView.context)
                .load(pictureModels[position]?.profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .placeholder(circularProgressDrawable)
                .into((holder as CustomViewHolder).profileImageView)

            Glide.with(holder.itemView.context)
                .load(pictureModels[position]?.pictureImageUrl)
                .apply(RequestOptions().centerCrop())
                .placeholder(circularProgressDrawable)
                .into((holder as CustomViewHolder).pictureImageView)

            if (pictureModels[position]?.Likes?.containsKey(UserObject.userModel?.uid) == true) {
                val drawable = context.resources.getDrawable(R.drawable.berry_full, context.theme)
                drawable.setColorFilter(ContextCompat.getColor(context, R.color.berrymeGradiant1), PorterDuff.Mode.SRC_IN)
                holder.heartImageView.setImageDrawable(drawable)
                /*holder.heartImageView.setImageDrawable(
                    context.resources.getDrawable(
                        R.drawable.berry_full,
                        context.theme
                    )
                )*/
            }

            val doubleClick = DoubleClick(object : DoubleClickListener {
                override fun onSingleClickEvent(view: View?) {
                    // DO STUFF SINGLE CLICK
                }

                override fun onDoubleClickEvent(view: View?) {
                    if (pictureModels[position]?.Likes?.containsKey(UserObject.userModel?.uid) == true) {
                        var likeNum = pictureModels[position]?.Likes?.size?.minus(1)
                        if (likeNum == null || likeNum < 0) {
                            likeNum = 0
                        }
                        holder.likeNumTextView.text = likeNum.toString()
                        pictureModels[position]?.Likes?.remove(UserObject.userModel?.uid)
                        val drawable = context.resources.getDrawable(R.drawable.berry_empty, context.theme)
                        drawable.setColorFilter(ContextCompat.getColor(context, R.color.berrymeGradiant1), PorterDuff.Mode.SRC_IN)
                        holder.heartImageView.setImageDrawable(drawable)
                        /*holder.heartImageView.setImageDrawable(
                            context.resources.getDrawable(
                                R.drawable.berry_empty,
                                context.theme
                            )
                        )*/

                        Firebase.database.getReference("pictures")
                            .child(pictureModels[position]?.pictureKey!!).child("Likes").child(UserObject.userModel?.uid!!)
                            .removeValue()
                    } else {
                        var likeNum = pictureModels[position]?.Likes?.size?.plus(1)
                        if (likeNum == null || likeNum < 0) {
                            likeNum = 0
                        }
                        holder.likeNumTextView.text = likeNum.toString()
                        pictureModels[position]?.Likes?.put(UserObject.userModel?.uid!!, true)

                        val drawable = context.resources.getDrawable(R.drawable.berry_full, context.theme)
                        drawable.setColorFilter(ContextCompat.getColor(context, R.color.berrymeGradiant1), PorterDuff.Mode.SRC_IN)
                        holder.heartImageView.setImageDrawable(drawable)
                        /*holder.heartImageView.setImageDrawable(
                            context.resources.getDrawable(
                                R.drawable.berry_full,
                                context.theme
                            )
                        )*/

                        holder.likeAnimation.visibility = View.VISIBLE
                        holder.likeAnimation.playAnimation()
                        holder.likeAnimation.addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(p0: Animator) { }

                            override fun onAnimationEnd(p0: Animator) {
                                holder.likeAnimation.visibility = View.GONE
                            }

                            override fun onAnimationCancel(p0: Animator) { }

                            override fun onAnimationRepeat(p0: Animator) { }
                        })

                        Firebase.database.getReference("pictures")
                            .child(pictureModels[position]?.pictureKey!!).child("Likes")
                            .child(UserObject.userModel?.uid!!).setValue(true)
                    }
                }
            })
            holder.pictureImageView.setOnClickListener(doubleClick)

            holder.nameTextView.text = pictureModels[position]?.username
            holder.valueTextView.text = pictureModels[position]?.value
            holder.valueTextView.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("Uid", pictureModels[position]?.uid)
                intent.putExtra("name", pictureModels[position]?.username)
                intent.putExtra("profile", pictureModels[position]?.profileImageUrl)
                intent.putExtra("picurl", pictureModels[position]?.pictureImageUrl)
                intent.putExtra("picuid", pictureModels[position]?.pictureKey)
                val activityOptions = ActivityOptions.makeCustomAnimation(
                    context,
                    R.anim.fromright,
                    R.anim.toleft
                )
                context.startActivity(intent, activityOptions.toBundle())
            }

            holder.valueNameTextView.text = pictureModels[position]?.username
            holder.valueNameTextView.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("Uid", pictureModels[position]?.uid)
                intent.putExtra("name", pictureModels[position]?.username)
                intent.putExtra("profile", pictureModels[position]?.profileImageUrl)
                intent.putExtra("picurl", pictureModels[position]?.pictureImageUrl)
                intent.putExtra("picuid", pictureModels[position]?.pictureKey)
                val activityOptions = ActivityOptions.makeCustomAnimation(
                    context,
                    R.anim.fromright,
                    R.anim.toleft
                )
                context.startActivity(intent, activityOptions.toBundle())
            }

            val likesSize = pictureModels[position]?.Likes?.size
            if (likesSize == null) {
                holder.likeNumTextView.text = "0"
            } else {
                holder.likeNumTextView.text = pictureModels[position]?.Likes?.size.toString()
            }

            //TimeStamp
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val unixTime = pictureModels[position]?.timestamp as Long
            val date = Date(unixTime)
            holder.timeTextView.text = simpleDateFormat.format(date)

            holder.profileImageView.setOnClickListener { view ->
                Firebase.database.getReference("users")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val userModel: UserModel? =
                                    snapshot.getValue(UserModel::class.java)

                                if (userModel!!.uid.equals(pictureModels[position]?.uid)) {
                                    val intent = Intent(view.context, MemberActivity::class.java)
                                    intent.putExtra("Uid", userModel?.uid)
                                    intent.putExtra("profile", userModel?.profileImageUrl)
                                    intent.putExtra("info", userModel?.info)
                                    intent.putExtra("name", userModel?.username)
                                    val activityOptions = ActivityOptions.makeCustomAnimation(
                                        view.context,
                                        R.anim.fromright,
                                        R.anim.toleft
                                    )
                                    context.startActivity(intent, activityOptions.toBundle())
                                    break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

//            var uid = FirebaseAuth.getInstance().currentUser!!.uid //채팅 요구하는 아이디
            holder.menuImageView.visibility = View.VISIBLE
            holder.menuImageView.setOnClickListener {
                showDialog(pictureModels[position]?.pictureKey, pictureModels[position]?.uid == UserObject.userModel?.uid)
            }
        }

        override fun getItemCount(): Int {
            return pictureModels.size
        }

        private inner class CustomViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            var profileImageView: ImageView = view.findViewById(R.id.listitem_imageview_profile)
            var pictureImageView: ImageView = view.findViewById(R.id.listitem_imageview_picture)
            var nameTextView: TextView = view.findViewById(R.id.listitem_textview_name)
            var timeTextView: TextView = view.findViewById(R.id.listitem_textview_time)
            var valueTextView: TextView = view.findViewById(R.id.listitem_textview_value)
            var valueNameTextView: TextView = view.findViewById(R.id.listitem_textview_valuename)
            var menuImageView: ImageView = view.findViewById(R.id.listitem_imageview_menu)
            var heartImageView: ImageView = view.findViewById(R.id.listitem_imageview_heart)
            var likeNumTextView: TextView = view.findViewById(R.id.listitem_textview_likenum)
            var likeAnimation: LottieAnimationView = view.findViewById(R.id.listitem_animation_like)
        }

        init {
            Firebase.database.getReference("pictures")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        pictureModels.clear()
                        for (snapshot in dataSnapshot.children) {
                            var picValue: PictureModel? = snapshot.getValue(PictureModel::class.java)
                            if (picValue?.isPrivate == null || picValue.isPrivate == false) {
                                picValue?.pictureKey = snapshot.key
                                pictureModels.add(picValue)
                            }
                        }
                        for (i: Int in 0 until pictureModels.size) {
                            Firebase.database.getReference("users").child(pictureModels[i]?.uid!!)
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

        override fun getItemViewType(position: Int): Int {
            return position
        }

        private fun showDialog(key: String?, isMe: Boolean) {
            var builder = AlertDialog.Builder(activity)
            var menu = if (isMe) {
                R.array.list_me
            } else {
                R.array.list_other
            }
            builder.setItems(menu) { dialog, which ->
                if (key != null) {
                    if (isMe) {
                        Firebase.database.getReference("pictures").child(key)
                            .removeValue()
                            .addOnSuccessListener {
                                Firebase.database.getReference("users")
                                    .child(UserObject.userModel?.uid!!).child("pictures").child(key)
                                    .removeValue()
                            }
                    } else {
                        Firebase.database.getReference("report").child(key)
                            .child(UserObject.userModel?.uid!!)
                            .setValue(true)
                    }
                }
            }
            builder.show()
        }
    }

}