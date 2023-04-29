package com.susuryo.berryme.fragment

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.*
import com.susuryo.berryme.databinding.ActivityMemberBinding
import com.susuryo.berryme.databinding.GridItemBinding
import com.susuryo.berryme.model.PictureModel
//import com.susuryo.berryme.databinding.FragmentCameraBinding
import com.susuryo.berryme.model.UserModel
//import kotlinx.android.synthetic.main.activity_member.*

class MyFragment : Fragment() {
    private lateinit var binding: ActivityMemberBinding
    lateinit var user: UserModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMemberBinding.inflate(inflater, container, false)
        user = UserObject.userModel!!

        binding.profileButton.setOnClickListener { editProfile() }

        Glide.with(requireContext())
            .load(UserObject.userModel?.profileImageUrl)
            .apply(RequestOptions().circleCrop())
            .into(binding.profile)
        binding.name.text = UserObject.userModel?.username
        binding.info.text = UserObject.userModel?.info

        binding.gridView.visibility = View.GONE
        binding.tabLayout.visibility = View.VISIBLE
        binding.viewPager.visibility = View.VISIBLE

        val adapter = MyPagerAdapter(requireActivity().supportFragmentManager, lifecycle)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.my_public)
                1 -> tab.text = resources.getString(R.string.my_private)
            }
        }.attach()

        return binding.root
    }

    private fun editProfile() {
        val intent = Intent(requireContext(), ChangeProfileActivity::class.java)
        val activityOptions = ActivityOptions.makeCustomAnimation(
            requireContext(),
            R.anim.fromright,
            R.anim.toleft
        )
        startActivity(intent, activityOptions.toBundle())
    }

    class MyPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
            0 to { MyPublicFragment() },
            1 to { MyPrivateFragment() }
        )

        override fun getItemCount(): Int = tabFragmentsCreators.size

        override fun createFragment(position: Int): Fragment {
            return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
        }
    }
}