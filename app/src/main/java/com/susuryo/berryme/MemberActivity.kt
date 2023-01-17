package com.susuryo.berryme

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.susuryo.berryme.model.UserModel
import kotlinx.android.synthetic.main.activity_member.*
import kotlinx.android.synthetic.main.activity_member.back_button
import java.util.*

class MemberActivity : AppCompatActivity() {
    private var destinationUid: String? = null
    private var name: String? = null
    private var profile: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        overridePendingTransition(R.anim.fromright, R.anim.none)

        destinationUid = intent.getStringExtra("Uid")
        memberactivity_title_relativelayout.visibility = View.VISIBLE

        back_button.setOnClickListener {
            onBackPressed()
        }

        FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(UserModel::class.java)
                    name = user?.username
                    memberactivity_textview_name.text = name

                    profile = user?.profileImageUrl
                    Glide.with(applicationContext)
                        .load(profile)
                        .apply(RequestOptions().circleCrop())
                        .into(memberactivity_imageview_profile)

                    memberactivity_textview_info.text = user?.info
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        var pictures: MutableList<UserModel.Picture?> = ArrayList<UserModel.Picture?>()
        FirebaseDatabase.getInstance().reference.child("users").child(destinationUid!!).child("Pictures")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    pictures?.clear()
                    for (item in dataSnapshot.children) {
                        val picture: UserModel.Picture? = item.getValue(UserModel.Picture::class.java)
                        pictures?.add(picture)
                    }

//                    pictures?.reverse()
                    memberactivity_gridview.adapter = MemberActivityGridViewAdapter(
                        applicationContext,
                        pictures
                    )
                    memberactivity_gridview.isExpanded = true
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.none, R.anim.horizon_exit)
    }

    inner class MemberActivityGridViewAdapter(_context: Context, _pictures: MutableList<UserModel.Picture?>) : BaseAdapter() {
        private var context: Context = _context
        var pictures: MutableList<UserModel.Picture?> = _pictures


        override fun getCount(): Int {
            return pictures.size
        }

        override fun getItem(p0: Int): UserModel.Picture? {
            return pictures[p0]
        }

        override fun getItemId(p0: Int): Long {
            return 0
        }

        override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {
            val view : View
            val holder : ViewHolder

            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.grid_item, null)
                holder = ViewHolder()
                holder.picImageView = view.findViewById(R.id.griditem_imageview)

                view.tag = holder

            } else {
                holder = convertView.tag as ViewHolder
                view = convertView
            }

            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            (holder as ViewHolder).picImageView?.let {
                Glide.with(context)
                    .load(pictures[p0]?.picUrl)
                    .apply(RequestOptions().centerCrop())
                    .placeholder(circularProgressDrawable)
                    .into(it)
            }

            holder.picImageView?.setOnClickListener { view ->
                val intent = Intent(view.context, DetailActivity::class.java)
                intent.putExtra("Uid", destinationUid)
                intent.putExtra("name", name)
                intent.putExtra("profile", profile)
                intent.putExtra("picurl", pictures[p0]?.picUrl)
                intent.putExtra("picuid", pictures[p0]?.picUid)
                val activityOptions = ActivityOptions.makeCustomAnimation(
                    view.context,
                    R.anim.fromright,
                    R.anim.toleft
                )
                startActivity(intent, activityOptions.toBundle())
            }

            return view
        }

        private inner class ViewHolder {
            var picImageView : ImageView? = null
        }
    }
}