package com.susuryo.berryme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private var id: EditText? = null
    private var password: EditText? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        //        firebaseAuth.signOut();
//        val splash_background = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_color))
//        window.statusBarColor = Color.parseColor(splash_background)
        id = findViewById(R.id.loginActivity_edittext_id)
        password = findViewById(R.id.loginActivity_edittext_password)

        loginActivity_button_login.setOnClickListener {
            loginEvent()
        }
//        loginActivity_button_login.setBackgroundColor(Color.parseColor(splash_background))
//        loginActivity_button_signup.setBackgroundColor(Color.parseColor(splash_background))
        loginActivity_button_signup.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    SignupActivity::class.java
                )
            )
        })

        //로그인 인터페이스 리스너
        authStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //로그인
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                //로그아웃
            }
        }
    }

    private fun loginEvent() {
        if (id?.text.toString() != "" && password?.text.toString() != "") {
            firebaseAuth!!.signInWithEmailAndPassword(
                id!!.text.toString(),
                password!!.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        //로그인 실패
                        Toast.makeText(
                            this@LoginActivity,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        } else {
            Toast.makeText(applicationContext, "Try Again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth!!.addAuthStateListener(authStateListener!!)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth!!.removeAuthStateListener(authStateListener!!)
    }
}