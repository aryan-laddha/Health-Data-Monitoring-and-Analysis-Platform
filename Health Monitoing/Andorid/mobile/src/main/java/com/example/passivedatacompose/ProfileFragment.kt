package com.example.passivedatacompose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.Serializable

class ProfileFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    // Declare TextView variables
    private lateinit var nameTextView: TextView
    private lateinit var ageTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var bpsTextView: TextView
    private lateinit var fbsTextView: TextView
    private lateinit var cholesterolTextView: TextView
    private lateinit var bloodGroupTextView: TextView
    private lateinit var doctorIdTextView: TextView
    private lateinit var heightTextView: TextView
    private lateinit var weightTextView: TextView

    private var user: User? = null

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize TextViews
        nameTextView = view.findViewById(R.id.nameTextView)
        ageTextView = view.findViewById(R.id.ageTextView)
        genderTextView = view.findViewById(R.id.genderTextView)
        bpsTextView = view.findViewById(R.id.bpsTextView)
        fbsTextView = view.findViewById(R.id.fbsTextView)
        cholesterolTextView = view.findViewById(R.id.cholesterolTextView)
        bloodGroupTextView = view.findViewById(R.id.bloodGroupTextView)
        doctorIdTextView = view.findViewById(R.id.doctorIdTextView)
        heightTextView = view.findViewById(R.id.heightTextView)
        weightTextView = view.findViewById(R.id.weightTextView)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Get userId from arguments
        userId = arguments?.getString("USER_ID") ?: ""

        // Fetch user data from Firebase
        fetchUserData(userId)

        // Initialize edit icon click listener
        val editIcon: ImageView = view.findViewById(R.id.editIcon)
        editIcon.setOnClickListener {
            startEditProfileActivity()
        }

        // Initialize sign-out button
        val signOutButton: Button = view.findViewById(R.id.signOutButton)
        signOutButton.setOnClickListener {
            signOut()
        }

        return view
    }

    private fun startEditProfileActivity() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        intent.putExtra("user", user)
        intent.putExtra("userId", userId) // Pass the userId as an extra
        startActivity(intent)
    }

    private fun fetchUserData(userId: String) {
        database.child("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)
                    user?.let {
                        Log.d("UserData", "Snapshot: $it")
                        displayUserData(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun displayUserData(user: User) {
        nameTextView.text = "Hello, ${user.Name}"
        ageTextView.text = "${user.Age}"
        genderTextView.text = "${user.Gender}"
        doctorIdTextView.text = "Doctor ID: ${user.DocterId}"
        bloodGroupTextView.text = "Blood Group: ${user.`Blood Group`}"
        heightTextView.text = "Height: ${user.Height}"
        weightTextView.text = "Weight: ${user.Weight}"
        bpsTextView.text = "BPS: ${user.BPS}"
        fbsTextView.text = "FBS: ${user.FBS}"
        cholesterolTextView.text = "Cholesterol: ${user.Cholesterol}"
    }

    // Function to sign out the current user
    private fun signOut() {
        auth.signOut()
        redirectToLoginActivity()
    }

    // Function to redirect to LoginActivity
    private fun redirectToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Close the current activity
    }
}

data class User(
    var Name: String = "",
    var Age: Int = 0,
    var Gender: String = "",
    var BPS: Int = 0,
    var FBS: Int = 0,
    var Cholesterol: Int = 0,
    var `Blood Group`: String = "",
    var DocterId: String = "", // Corrected to match the field name in Firebase
    var Height: Int = 0,
    var Weight: Int = 0
) : Serializable
