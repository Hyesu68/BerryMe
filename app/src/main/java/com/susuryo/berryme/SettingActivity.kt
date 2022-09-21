package com.susuryo.berryme

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_setting.back_button

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        back_button.setOnClickListener {
            onBackPressed()
        }

        settingfragment_changeprofile.setOnClickListener {
            val intent = Intent(applicationContext, ChangeProfileActivity::class.java)
            val activityOptions = ActivityOptions.makeCustomAnimation(
                applicationContext,
                R.anim.fromright,
                R.anim.toleft
            )
            startActivity(intent, activityOptions.toBundle())
        }

        settingfragment_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }
}