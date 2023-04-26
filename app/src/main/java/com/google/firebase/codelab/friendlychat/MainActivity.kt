package com.google.firebase.codelab.friendlychat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.codelab.friendlychat.databinding.ActivityMainBinding
import com.google.firebase.codelab.friendlychat.model.FriendlyMessage
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var manager: LinearLayoutManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: FriendlyMessageAdapter


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
        db = Firebase.database
        val messagesRef = db.reference.child(MESSAGES_CHILD)

        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
            .setQuery(messagesRef, FriendlyMessage::class.java)
            .build()
        adapter = FriendlyMessageAdapter(options, getUserName())
        binding.progressBar.visibility = ProgressBar.INVISIBLE
        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = manager
        binding.messageRecyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(binding.messageRecyclerView,adapter,manager)
        )

        // Disable the send button when there's no text in the input field
        // See MyButtonObserver for details
        binding.messageEditText.addTextChangedListener(
            MyButtonObserver(binding.sendButton)
        )

        binding.sendButton.setOnClickListener {
            val friendlyMessage = FriendlyMessage(
                text = binding.messageEditText.text.toString(),
                name = getUserName(),
                photoUrl = getPhotoUrl(),
                imageUrl = null
            )
            messagesRef.child(MESSAGES_CHILD).push().setValue(friendlyMessage)
            binding.messageEditText.setText("")
        }

        // When the image button is clicked, launch the image picker
        binding.addMessageImageView.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }
    }


    public override fun onStart() {
        super.onStart()
        checkAuthForCurrentUser()
    }

    override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        adapter.startListening()
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
        Log.d(TAG, "onImageSelected, Uri: $uri")
        val user = auth.currentUser
        val tempMessage =
            FriendlyMessage(
                null, getUserName(), getPhotoUrl(), LOADING_IMAGE_URL
            )
        db.reference.child(MESSAGES_CHILD).push().setValue(
            tempMessage,
            DatabaseReference.CompletionListener { dbError, dbRef ->
                if (dbError != null) {
                    Log.e(TAG, "Unable to write message to database.", dbError.toException())
                    return@CompletionListener
                }
                val key = dbRef.key
                val storageReference = Firebase.storage
                    .getReference(user!!.uid)
                    .child(key!!)
                    .child(uri.lastPathSegment!!)
                putImageInStorage(storageReference, uri, key)
            }
        )


    }

    private fun putImageInStorage(
        storageReference: StorageReference, uri: Uri, key: String?
    ) {

        storageReference.putFile(uri)
            .addOnSuccessListener(this) { task ->
                task.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        val friendlyMessage =
                            FriendlyMessage(null, getUserName(), getPhotoUrl(), uri.toString())
                        db.reference
                            .child(MESSAGES_CHILD)
                            .child(key!!)
                            .setValue(friendlyMessage)
                    }
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, "Image upload task was unsuccessful.", e)
            }
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
