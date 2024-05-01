package com.example.passivedatacompose

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.passivedatacompose.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userIndexRef: DatabaseReference // Firebase Database reference

    private var userId: String? = null
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    replaceFragment(DashboardFragment(), userId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_report -> {
                    replaceFragment(ReportFragment(), userId)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment(), userId)
                    return@OnNavigationItemSelectedListener true
                }

            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Initialize Firebase Authentication
        auth = Firebase.auth
        userIndexRef = FirebaseDatabase.getInstance().getReference("userIndex")

        // Check if the user is already authenticated
        if (auth.currentUser != null) {
            // If the user is already logged in, proceed with setting up bottom navigation
            startService(Intent(this, FirebaseUserService::class.java))
            val userUID = auth.currentUser?.uid
            getUserIDFromUserIndex(userUID)
            // Log the user ID
            Log.d(TAG, "User UID: $userUID")
            Log.d(TAG, "User ID: $userId")

            setupBottomNavigation()
        } else {
            // If the user is not logged in, start LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Retrieve user ID from intent extras
       // val userId = intent.getStringExtra("USER_ID")
        // Log the user ID
        //Log.d(TAG, "User ID  after the if condition: $userId")
    }
    private fun getUserIDFromUserIndex(userUID: String?) {
        userUID?.let {
            userIndexRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userId = dataSnapshot.getValue(String::class.java)
                    if (userId != null) {
                        // Successfully fetched userId
                        Log.d(TAG, "User ID from userIndex: $userId")
                        // Now you can use this userId for further operations
                        // For example, pass it to a function to fetch user details
                        // fetchUserDetails(userId)
                    } else {
                        // Handle the case where userId is null
                        Log.d(TAG, "User ID not found for user UID: $userUID")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Log.e(TAG, "getUserIDFromUserIndex:onCancelled", databaseError.toException())
                }
            })
        }
    }
    private fun setupBottomNavigation() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // Set the default fragment to display DashboardFragment content
        replaceFragment(DashboardFragment())
    }

    private fun replaceFragment(fragment: Fragment, userId: String? = null) {
        val bundle = Bundle().apply {
            putString("USER_ID", userId)
        }
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

}
