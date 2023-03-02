package com.susuryo.berryme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.susuryo.berryme.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private var firebaseAuth: FirebaseAuth? = null
    private var authStateListener: AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginActivityButtonLogin.setOnClickListener {
            loginEvent()
        }

        binding.loginActivityButtonSignup.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    SignupActivity::class.java
                )
            )
        })

        authStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding.loginEmailInput.editText?.addTextChangedListener(setTextWatcher(binding.loginEmailInput))
        binding.loginPasswordInput.editText?.addTextChangedListener(setTextWatcher(binding.loginPasswordInput))
    }

    private fun loginEvent() {
        val id = binding.loginEmailInput.editText?.text.toString()
        val password = binding.loginPasswordInput.editText?.text.toString()

        if (id.isEmpty()) {
            binding.loginEmailInput.error = "Email must not be empty"
        } else if (password.isEmpty()) {
            binding.loginPasswordInput.error = "Password must not be empty"
        } else {
            logIn()
        }
    }

    private fun logIn() {
        binding.loginEmailInput.isEnabled = false
        binding.loginPasswordInput.isEnabled = false
        binding.loginActivityButtonLogin.visibility = View.INVISIBLE
        binding.loginActivityButtonSignup.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        firebaseAuth!!.signInWithEmailAndPassword(
            binding.loginActivityEdittextId.text.toString(),
            binding.loginActivityEdittextPassword.text.toString())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    showErrorDialog()
                }
            }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Login Failed")
            .setMessage("Username or password is not correct.")
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss()}
            .show()

        binding.loginEmailInput.isEnabled = true
        binding.loginPasswordInput.isEnabled = true
        binding.loginActivityButtonLogin.visibility = View.VISIBLE
        binding.loginActivityButtonSignup.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun setTextWatcher(textInputLayout: TextInputLayout): TextWatcher? {
        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInputLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }
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