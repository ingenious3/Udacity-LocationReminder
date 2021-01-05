package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
//import com.udacity.project4.authentication.AuthenticationActivity.Companion.RESULT_CODE
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthenticationBinding

    companion object {
        val AUTH_REQUEST_CODE = 100
        val TAG = AuthenticationActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        val useraccount = FirebaseAuth.getInstance().currentUser
        if (useraccount != null) {
            finish()
            startActivity(Intent(this, RemindersActivity::class.java))
        }
        binding.loginButton.setOnClickListener{ launchSignInFlow() }

    }

    private fun launchSignInFlow() {
        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.AppTheme2)
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()))
                    .build(),
                AUTH_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Successfully signed in")
                finish()
                startActivity(Intent(this@AuthenticationActivity, RemindersActivity::class.java))
            } else {
                Log.i(TAG, "Sign in failed ")
            }
        }
    }
}
