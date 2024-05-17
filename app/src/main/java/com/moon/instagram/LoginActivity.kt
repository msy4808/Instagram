package com.moon.instagram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.moon.instagram.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        auth = FirebaseAuth.getInstance()
        mBinding.emailLoginBtn.setOnClickListener {
            signinAndSignup()
        }
        mBinding.googleSignInBtn.setOnClickListener {
            //First step
            googleLogin()
        }
        var googleSignOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignOption)
    }

    private fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent

        if (signInIntent != null) {
            startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
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

    private fun fireBaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }

            if (result != null) {
                if (result.isSuccess) {
                    var account = result.signInAccount
                    //Second step
                    fireBaseAuthWithGoogle(account)
                }

            }
        }
    }
}