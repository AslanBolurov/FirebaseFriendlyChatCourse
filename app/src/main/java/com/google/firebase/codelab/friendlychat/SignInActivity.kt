package com.google.firebase.codelab.friendlychat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.codelab.friendlychat.databinding.ActivitySignInBinding
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    // ActivityResultLauncher
    private val signIn: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            FirebaseAuthUIActivityResultContract(), this::onSignInResult
        )

    // Firebase instance variables
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            val signInIntent =
                AuthUI.getInstance().createSignInIntentBuilder()
                    .setLogo(R.mipmap.ic_launcher)
                    .setAvailableProviders(
                        listOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                    )
                    .build()
            signIn.launch(signInIntent)

        } else {
            goToMainActivity()
        }


    }

    private fun signIn() {
        // TODO: implement
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "onSignInResult: Sign in successful")
            goToMainActivity()

        } else {
            Toast.makeText(
                this,
                "There was an error signing in",
                Toast.LENGTH_LONG)
                .show()

            if (result.idpResponse == null){
                Log.e(TAG, "onSignInResult: Sign in cancelled!")
            }else{
                Log.e(TAG, "onSignInResult: Sign in error: ${result.idpResponse!!.error}")
            }
        }


    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}
