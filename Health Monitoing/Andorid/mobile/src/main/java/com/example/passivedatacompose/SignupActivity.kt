package com.example.passivedatacompose

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var nameEditText: EditText
    private lateinit var userIdEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var bloodGroupEditText: EditText
    private lateinit var bpsEditText: EditText
    private lateinit var fbsEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var maleRadioButton: RadioButton
    private lateinit var femaleRadioButton: RadioButton
    private lateinit var doctorIdEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var alreadyHaveAccountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        nameEditText = findViewById(R.id.nameEditText)
        userIdEditText = findViewById(R.id.userIdEditText)
        emailEditText = findViewById(R.id.emailEditText)
        ageEditText = findViewById(R.id.ageEditText)
        bloodGroupEditText = findViewById(R.id.bloodGroupEditText)
        bpsEditText = findViewById(R.id.bpsEditText)
        fbsEditText = findViewById(R.id.fbsEditText)
        heightEditText = findViewById(R.id.heightEditText)
        weightEditText = findViewById(R.id.weightEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        maleRadioButton = findViewById(R.id.maleRadioButton)
        femaleRadioButton = findViewById(R.id.femaleRadioButton)
        doctorIdEditText = findViewById(R.id.doctorIdEditText)
        signupButton = findViewById(R.id.signupButton)
        alreadyHaveAccountTextView = findViewById(R.id.alreadyHaveAccountTextView)

        signupButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val userId = userIdEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()
            val bloodGroup = bloodGroupEditText.text.toString().trim()
            val bps = bpsEditText.text.toString().trim()
            val fbs = fbsEditText.text.toString().trim()
            val height = heightEditText.text.toString().trim()
            val weight = weightEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val gender = if (maleRadioButton.isChecked) "Male" else if (femaleRadioButton.isChecked) "Female" else ""
            val doctorId = doctorIdEditText.text.toString().trim() // New line to get Doctor ID

            // Check if any field is empty
            if (name.isEmpty() || userId.isEmpty() || email.isEmpty() || age.isEmpty() || bloodGroup.isEmpty() ||
                bps.isEmpty() || fbs.isEmpty() || height.isEmpty() || weight.isEmpty() || password.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign up success, save user data to Firebase Realtime Database
                        val uid = auth.currentUser?.uid ?: ""
                        val databaseReference = FirebaseDatabase.getInstance().reference

                        // Save user data under 'Users' node with userId as the key
                        val userData = hashMapOf(
                            "Name" to name,
                            "Email" to email,
                            "Age" to age.toInt(),
                            "Blood Group" to bloodGroup,
                            "BPS" to bps.toInt(),
                            "FBS" to fbs.toInt(),
                            "Height" to height.toInt(),
                            "Weight" to weight.toInt(),
                            "Gender" to gender,
                            "DoctorId" to doctorId // Add Doctor ID to user data
                        )

                        // Save user data
                        databaseReference.child("Users").child(userId).setValue(userData)
                            .addOnSuccessListener {
                                // Save userId under 'userIndex' node with uid as the key
                                databaseReference.child("userIndex").child(uid).setValue(userId)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Failed to save userId: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // If sign up fails, display a message to the user.
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            // Handle weak password error
                            Toast.makeText(this, "Weak password: ${e.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            // Handle invalid email error
                            Toast.makeText(this, "Invalid email: ${e.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: FirebaseAuthUserCollisionException) {
                            // Handle email already in use error
                            Toast.makeText(this, "Email already in use: ${e.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            // Handle other errors
                            Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
        alreadyHaveAccountTextView.setOnClickListener {
            // Handle navigation to LoginActivity when "Already have an account?" TextView is clicked
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Optional: Finish the current activity to prevent going back to it when pressing back button
        }
    }
}
