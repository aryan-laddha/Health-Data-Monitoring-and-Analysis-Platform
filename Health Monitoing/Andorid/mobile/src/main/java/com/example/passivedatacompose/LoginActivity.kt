package com.example.passivedatacompose

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupTextView: TextView // Added TextView for signup option

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signupTextView = findViewById(R.id.signupTextView) // Initialize signup TextView

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password) // Call the signIn function here
        }

        // Set click listener for signup TextView
        signupTextView.setOnClickListener {
            // Redirect to SignupActivity when signup TextView is clicked
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    val uid = user?.uid // Retrieve the user UID

                    // Query the userIndex node to retrieve the userId using the uid
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    databaseReference.child("userIndex").child(uid ?: "").get()
                        .addOnSuccessListener { snapshot ->
                            val userId = snapshot.value as? String ?: ""
                            Log.d(TAG, "signInWithEmail:success")
                            Toast.makeText(baseContext, "Login Successful", Toast.LENGTH_SHORT).show()
                            // Pass the userId to MainActivity
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("USER_ID", userId) // Pass the userId with key "USER_ID"
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error getting userId", e)
                            Toast.makeText(baseContext, "Error getting userId: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Toast.makeText(baseContext, "Invalid email address", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(baseContext, "Invalid password", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
