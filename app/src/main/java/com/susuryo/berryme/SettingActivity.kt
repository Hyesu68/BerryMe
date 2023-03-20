package com.susuryo.berryme

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.susuryo.berryme.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.settingfragmentChangeprofile.setOnClickListener {
            val intent = Intent(applicationContext, ChangeProfileActivity::class.java)
            val activityOptions = ActivityOptions.makeCustomAnimation(
                applicationContext,
                R.anim.fromright,
                R.anim.toleft
            )
            startActivity(intent, activityOptions.toBundle())
        }

        binding.settingfragmentLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }
}