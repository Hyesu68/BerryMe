package com.susuryo.berryme

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class SplashActivity : AppCompatActivity() {
    var linearLayout: LinearLayout? = null
    var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        FirebaseApp.initializeApp(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        linearLayout = findViewById(R.id.splashactivity_linearlayout)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds((60 * 10).toLong())
            .build()
        mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig!!.setDefaultsAsync(R.xml.remote_config_defaults)
        mFirebaseRemoteConfig!!.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
//                    val updated = task.result
//                    Log.d(FirebaseRemoteConfig.TAG, "Config params updated: $updated")
//                    Toast.makeText(
//                        this@SplashActivity, "Login Success",
//                        Toast.LENGTH_SHORT
//                    ).show()
                } else {
                    Toast.makeText(
                        this@SplashActivity, "Login failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                displayWelcomeMessage()
            }
    }

    fun displayWelcomeMessage() {
        val splash_background = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_color))
        val caps = mFirebaseRemoteConfig!!.getBoolean("splash_message_caps")
        val splash_message = mFirebaseRemoteConfig!!.getString("splash_message")
//        linearLayout!!.setBackgroundColor(Color.parseColor(splash_background))
        if (caps) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(splash_message)
                .setPositiveButton("확인") { dialogInterface, i -> finish() }
            builder.create().show()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}