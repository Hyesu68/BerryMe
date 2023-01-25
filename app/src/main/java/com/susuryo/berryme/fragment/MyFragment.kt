package com.susuryo.berryme.fragment

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.susuryo.berryme.*
import com.susuryo.berryme.databinding.ActivityMemberBinding
import com.susuryo.berryme.databinding.FragmentCameraBinding
//import com.susuryo.berryme.databinding.FragmentCameraBinding
import com.susuryo.berryme.model.UserModel
//import kotlinx.android.synthetic.main.activity_member.*
import java.util.ArrayList

class MyFragment : Fragment() {
    private var _binding: ActivityMemberBinding? = null
    private val binding get() = _binding!!
    lateinit var user: UserModel
//    lateinit var name: TextView
//    lateinit var profile: ImageView
//    lateinit var info: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view : View = inflater.inflate(R.layout.activity_member, container, false)
        _binding = ActivityMemberBinding.inflate(inflater, container, false)
        user = UserObject.userModel
//        name = view.findViewById(R.id.memberactivity_textview_name)
//        profile = view.findViewById(R.id.memberactivity_imageview_profile)
//        info = view.findViewById(R.id.memberactivity_textview_info)

        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(requireContext())
            .load(UserObject.userModel?.profileImageUrl)
            .apply(RequestOptions().circleCrop())
            .placeholder(circularProgressDrawable)
            .into(binding.memberactivityImageviewProfile)
        binding.memberactivityTextviewName.text = UserObject.userModel?.username
        binding.memberactivityTextviewInfo.text = UserObject.userModel?.info
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        FirebaseDatabase.getInstance().reference.child("users").child(user.uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userTmp: UserModel? = dataSnapshot.getValue(UserModel::class.java)
                    if (userTmp != null) {
                        UserObject.userModel = userTmp
                    }

                    val circularProgressDrawable = CircularProgressDrawable(requireContext())
                    circularProgressDrawable.strokeWidth = 5f
                    circularProgressDrawable.centerRadius = 30f
                    circularProgressDrawable.start()
                    Glide.with(requireContext())
                        .load(userTmp?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .placeholder(circularProgressDrawable)
                        .into(binding.memberactivityImageviewProfile)

                    binding.memberactivityTextviewName.text = userTmp?.username
                    binding.memberactivityTextviewInfo.text = userTmp?.info
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        var pictures: MutableList<UserModel.Picture?> = ArrayList<UserModel.Picture?>()
        FirebaseDatabase.getInstance().reference.child("users").child(user.uid!!).child("Pictures")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    pictures?.clear()
                    for (item in dataSnapshot.children) {
                        val picture: UserModel.Picture? = item.getValue(UserModel.Picture::class.java)
                        pictures?.add(picture)
                    }

//                    pictures?.reverse()
                    if (pictures.size == 0) {
                        binding.memberactivityNopictureLinearlayout.visibility = View.VISIBLE
                        binding.memberactivityGridview.visibility = View.GONE

                        binding.memberactivityNopictureLinearlayout.setOnClickListener {
                            val mActivity = activity as MainActivity
                            mActivity.setFragment(R.id.action_camera)
                        }
                    } else {
                        binding.memberactivityNopictureLinearlayout.visibility = View.GONE
                        binding.memberactivityGridview.visibility = View.VISIBLE
                        binding.memberactivityGridview.adapter = MemberActivityGridViewAdapter(
                            requireContext(),
                            pictures
                        )
                        binding.memberactivityGridview.isExpanded = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
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

            val circularProgressDrawable = CircularProgressDrawable(requireContext())
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
                intent.putExtra("Uid", user.uid)
                intent.putExtra("name", user.username)
                intent.putExtra("profile", user.profileImageUrl)
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