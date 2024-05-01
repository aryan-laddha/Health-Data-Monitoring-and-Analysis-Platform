package com.example.passivedatacompose

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseDataManager {
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun writeUserData(userId: String, name: String, age: Int) {
        val userData = hashMapOf<String, Any>("Name" to name, "Age" to age)
        databaseReference.child("Users").child(userId).setValue(userData)
    }

    fun writeHeartRateData(userId: String, timestamp: String, heartRate: Int) {
        val heartRateData = hashMapOf<String, Any>("timestamp" to timestamp, "heartRate" to heartRate)
        databaseReference.child("Users").child(userId).child("heartRateData").push().setValue(heartRateData)
    }
}