package com.susuryo.berryme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.susuryo.berryme.databinding.ActivityMainBinding
import com.susuryo.berryme.fragment.*
import com.susuryo.berryme.model.UserModel

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var uid = Firebase.auth.currentUser!!.uid
//        var uid = FirebaseAuth.getInstance().currentUser!!.uid //채팅 요구하는 아이디
        Firebase.database.getReference("users").child(uid)
//        FirebaseDatabase.getInstance().reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    UserObject.userModel = dataSnapshot.getValue(UserModel::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.mainactivityBottomnavigationview.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, ListFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_all -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, AllMemberFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_camera -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, CameraFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_setting -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, MyFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainactivity_framelayout, ListFragment()).commit()

        binding.toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.message -> {
                    startActivity(Intent(this, MessageListActivity::class.java))
                    true
                }
                R.id.menu -> {
//                    startActivity(Intent(this, SettingActivity::class.java))
                    val modalBottomSheet = ModalBottomSheet()
                    modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
                    true
                }
                else -> false
            }
        }
    }

    fun setFragment(id: Int) {
        binding.mainactivityBottomnavigationview.selectedItemId = id
        when (id) {
            R.id.action_list -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, ListFragment()).commit()
            }
            R.id.action_all -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, AllMemberFragment()).commit()
            }
            R.id.action_camera -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, CameraFragment()).commit()
            }
            R.id.action_setting -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, MyFragment()).commit()
            }
        }
    }

}