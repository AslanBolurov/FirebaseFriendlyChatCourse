package com.google.firebase.codelab.friendlychat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.codelab.friendlychat.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var manager: LinearLayoutManager
    private lateinit var auth: FirebaseAuth

    val intentForSignInActivity by lazy {
        Intent(this, SignInActivity::class.java)
    }

    private val openDocument =
        registerForActivityResult(MyOpenDocumentContract()) { uri ->
            uri?.let { onImageSelected(it) }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        checkAuthForCurrentUser()


        // Initialize Realtime Database and FirebaseRecyclerAdapter
        // TODO: implement

        // Disable the send button when there's no text in the input field
        // See MyButtonObserver for details
        binding.messageEditText.addTextChangedListener(
            MyButtonObserver(binding.sendButton)
        )

        // When the send button is clicked, send a text message
        // TODO: implement

        // When the image button is clicked, launch the image picker
        binding.addMessageImageView.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }
    }


    public override fun onStart() {
        super.onStart()
        checkAuthForCurrentUser()
    }


    public override fun onResume() {
        super.onResume()
        checkAuthForCurrentUser()
    }

    private fun getPhotoUrl(): String? {
        val user = auth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String {
        val user = auth.currentUser
        return user?.displayName ?: ANONYMOUS
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onImageSelected(uri: Uri) {
        // TODO: implement
    }

    private fun putImageInStorage(
        storageReference: StorageReference, uri: Uri, key: String?
    ) {
        // Upload the image to Cloud Storage
        // TODO: implement
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        startActivity(intentForSignInActivity)
    }

    private fun checkAuthForCurrentUser() {
        if (auth.currentUser == null) {
            startActivity(intentForSignInActivity)
            finish()
            return
        }

    }

    companion object {
        private const val TAG = "MainActivity"
        const val MESSAGES_CHILD = "messages"
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}
