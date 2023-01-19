package com.susuryo.berryme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.susuryo.berryme.fragment.CameraFragment
import com.susuryo.berryme.fragment.ListFragment
import com.susuryo.berryme.fragment.MessageFragment
import com.susuryo.berryme.fragment.MyFragment
import com.susuryo.berryme.model.UserModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var uid = FirebaseAuth.getInstance().currentUser!!.uid //채팅 요구하는 아이디
        FirebaseDatabase.getInstance().reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    UserObject.userModel = dataSnapshot.getValue(UserModel::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        bottomNavigationView = findViewById(R.id.mainactivity_bottomnavigationview)
        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, ListFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_camera -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, CameraFragment()).commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.action_message -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainactivity_framelayout, MessageFragment()).commit()
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

        mainactibity_setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    fun setFragment(id: Int) {
        bottomNavigationView.selectedItemId = id
        when (id) {
            R.id.action_list -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, ListFragment()).commit()
            }
            R.id.action_camera -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, CameraFragment()).commit()
            }
            R.id.action_message -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, MessageFragment()).commit()
            }
            R.id.action_setting -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainactivity_framelayout, MyFragment()).commit()
            }
        }
    }

}