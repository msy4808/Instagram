package com.moon.instagram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.moon.instagram.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        auth = FirebaseAuth.getInstance()
        mBinding.emailLoginBtn.setOnClickListener {
            signinAndSignup()
        }
    }

    private fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(
            mBinding.emailEditText.text.toString(),
            mBinding.passwordEditText.text.toString()
        )?.addOnCompleteListener {
            task ->
            if(task.isSuccessful) {
                //Creating a user account
                moveMainPage(task.result.user)
            } else if(!task.exception?.message.isNullOrEmpty()) {
                //Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                //Login if you have account
                signinEmail()
            }
        }
    }

    private fun signinEmail() {
        auth?.signInWithEmailAndPassword(mBinding.emailEditText.text.toString(),
            mBinding.passwordEditText.text.toString()
        )?.addOnCompleteListener {
                task ->
            if(task.isSuccessful) {
                //Login
                moveMainPage(task.result.user)
            } else {
                //Login if you have account
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveMainPage(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}