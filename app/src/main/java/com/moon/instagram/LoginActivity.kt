package com.moon.instagram

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.moon.instagram.databinding.ActivityLoginBinding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays


class LoginActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager: CallbackManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        auth = FirebaseAuth.getInstance()
        mBinding.run {
            emailLoginBtn.setOnClickListener {
                signinAndSignup()
            }
            googleSignInBtn.setOnClickListener {
                //First step
                googleLogin()
            }
            facebookSignInBtn.setOnClickListener {
                //First step
                facebookLogin()
            }
        }

        val googleSignOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignOption)
//        printHashKey()
        callbackManager = CallbackManager.Factory.create()
    }

    private fun googleLogin() {
        val signInIntent = googleSignInClient?.signInIntent

        if (signInIntent != null) {
            startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onCancel() {}
            override fun onError(error: FacebookException) {}

            override fun onSuccess(result: LoginResult) {
                //Second step
                handleFacebookAccessToken(result.accessToken)
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
                    //Login
                    moveMainPage(task.result.user)
                } else {
                    //Login if you have account
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    private fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(
            mBinding.emailEditText.text.toString(),
            mBinding.passwordEditText.text.toString()
        )?.addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
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
            if (task.isSuccessful) {
                //Login
                moveMainPage(task.result.user)
            } else {
                //Login if you have account
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun fireBaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
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
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }

            if (result != null) {
                if (result.isSuccess) {
                    var account = result.signInAccount
                    //Second step
                    fireBaseAuthWithGoogle(account)
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }
}