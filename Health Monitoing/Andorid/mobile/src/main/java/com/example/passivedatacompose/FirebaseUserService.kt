package com.example.passivedatacompose

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FirebaseUserService : Service() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var userId: String? = null
    private var userIdListener: UserIdListener? = null

    inner class LocalBinder : Binder() {
        fun getService(): FirebaseUserService = this@FirebaseUserService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Initialize Firebase Auth and Database references
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Fetch the user ID on app start
        fetchUserId()

        return START_STICKY
    }

    private fun fetchUserId() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        databaseReference.child("userIndex").child(uid ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userId = dataSnapshot.value as? String
                    userId?.let {
                        Log.d("FirebaseUserService", "User ID: $it")
                        setupNecessaryOperations() // Perform operations after fetching user ID
                        userIdListener?.onUserIdFetched(it) // Notify listener
                    }
                } else {
                    Log.d("FirebaseUserService", "User ID not found for UID: $uid")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseUserService", "Error fetching user ID: ${databaseError.message}")
            }
        })
    }

    private fun setupNecessaryOperations() {
        // Add your necessary operations here
    }

    fun setUserIdListener(listener: UserIdListener) {
        userIdListener = listener
    }

    fun getUserId(): String? {
        return userId
    }

    interface UserIdListener {
        fun onUserIdFetched(userId: String)
    }
}
