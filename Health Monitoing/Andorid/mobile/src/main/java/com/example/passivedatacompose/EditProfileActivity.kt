package com.example.passivedatacompose

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var userId: String
    private lateinit var user: User
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userId = intent.getStringExtra("userId") ?: ""
        user = intent.getSerializableExtra("user") as User

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference

        val nameEditText: EditText = findViewById(R.id.nameEditText)
        nameEditText.setText(user.Name)

        val ageEditText: EditText = findViewById(R.id.ageEditText)
        ageEditText.setText(user.Age.toString())

        val genderEditText: EditText = findViewById(R.id.genderEditText)
        genderEditText.setText(user.Gender)

        val bpsEditText: EditText = findViewById(R.id.bpsEditText)
        bpsEditText.setText(user.BPS.toString())

        val fbsEditText: EditText = findViewById(R.id.fbsEditText)
        fbsEditText.setText(user.FBS.toString())

        val cholesterolEditText: EditText = findViewById(R.id.cholesterolEditText)
        cholesterolEditText.setText(user.Cholesterol.toString())

        val bloodGroupEditText: EditText = findViewById(R.id.bloodGroupEditText)
        bloodGroupEditText.setText(user.`Blood Group`)

        val doctorIdEditText: EditText = findViewById(R.id.doctorIdEditText)
        doctorIdEditText.setText(user.DocterId)

        val heightEditText: EditText = findViewById(R.id.heightEditText)
        heightEditText.setText(user.Height.toString())

        val weightEditText: EditText = findViewById(R.id.weightEditText)
        weightEditText.setText(user.Weight.toString())

        val confirmButton: Button = findViewById(R.id.confirmButton)
        confirmButton.setOnClickListener {
            updateUserInFirebase()
        }
    }

    private fun updateUserInFirebase() {
        val nameEditText: EditText = findViewById(R.id.nameEditText)
        val ageEditText: EditText = findViewById(R.id.ageEditText)
        val genderEditText: EditText = findViewById(R.id.genderEditText)
        val bpsEditText: EditText = findViewById(R.id.bpsEditText)
        val fbsEditText: EditText = findViewById(R.id.fbsEditText)
        val cholesterolEditText: EditText = findViewById(R.id.cholesterolEditText)
        val bloodGroupEditText: EditText = findViewById(R.id.bloodGroupEditText)
        val doctorIdEditText: EditText = findViewById(R.id.doctorIdEditText)
        val heightEditText: EditText = findViewById(R.id.heightEditText)
        val weightEditText: EditText = findViewById(R.id.weightEditText)

        val editedName = nameEditText.text.toString()
        val editedAge = ageEditText.text.toString().toIntOrNull() ?: 0
        val editedGender = genderEditText.text.toString()
        val editedBPS = bpsEditText.text.toString().toIntOrNull() ?: 0
        val editedFBS = fbsEditText.text.toString().toIntOrNull() ?: 0
        val editedCholesterol = cholesterolEditText.text.toString().toIntOrNull() ?: 0
        val editedBloodGroup = bloodGroupEditText.text.toString()
        val editedDoctorId = doctorIdEditText.text.toString()
        val editedHeight = heightEditText.text.toString().toIntOrNull() ?: 0
        val editedWeight = weightEditText.text.toString().toIntOrNull() ?: 0

        // Update fields if changed
        if (editedName != user.Name) user.Name = editedName
        if (editedAge != user.Age) user.Age = editedAge
        if (editedGender != user.Gender) user.Gender = editedGender
        if (editedBPS != user.BPS) user.BPS = editedBPS
        if (editedFBS != user.FBS) user.FBS = editedFBS
        if (editedCholesterol != user.Cholesterol) user.Cholesterol = editedCholesterol
        if (editedBloodGroup != user.`Blood Group`) user.`Blood Group` = editedBloodGroup
        if (editedDoctorId != user.DocterId) user.DocterId = editedDoctorId
        if (editedHeight != user.Height) user.Height = editedHeight
        if (editedWeight != user.Weight) user.Weight = editedWeight

        // Update user data in Firebase
        val userMap: MutableMap<String, Any> = mutableMapOf()
        userMap["Name"] = user.Name
        userMap["Age"] = user.Age
        userMap["Gender"] = user.Gender
        userMap["BPS"] = user.BPS
        userMap["FBS"] = user.FBS
        userMap["Cholesterol"] = user.Cholesterol
        userMap["Blood Group"] = user.`Blood Group`
        userMap["DocterId"] = user.DocterId
        userMap["Height"] = user.Height
        userMap["Weight"] = user.Weight

        database.child("Users").child(userId).updateChildren(userMap)
            .addOnSuccessListener {
                // Successfully updated
                finish()
            }
            .addOnFailureListener {
                // Failed to update
            }
    }

}
