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
import com.susuryo.berryme.databinding.ActivityLoginBinding

//import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
//    private var id: EditText? = null
//    private var password: EditText? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        //        firebaseAuth.signOut();
//        val splash_background = mFirebaseRemoteConfig!!.getString(getString(R.string.rc_color))
//        window.statusBarColor = Color.parseColor(splash_background)
//        binding.loginActivityEdittextId = findViewById(R.id.loginActivity_edittext_id)
//        binding.loginActivityEdittextPasswordpassword = findViewById(R.id.loginActivity_edittext_password)

        binding.loginActivityButtonLogin.setOnClickListener {
            loginEvent()
        }
//        loginActivity_button_login.setBackgroundColor(Color.parseColor(splash_background))
//        loginActivity_button_signup.setBackgroundColor(Color.parseColor(splash_background))
        binding.loginActivityButtonSignup.setOnClickListener(View.OnClickListener {
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
        if (binding.loginActivityEdittextId.text.toString() != "" && binding.loginActivityEdittextPassword.text.toString() != "") {
            firebaseAuth!!.signInWithEmailAndPassword(
                binding.loginActivityEdittextId.text.toString(),
                binding.loginActivityEdittextPassword.text.toString()
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