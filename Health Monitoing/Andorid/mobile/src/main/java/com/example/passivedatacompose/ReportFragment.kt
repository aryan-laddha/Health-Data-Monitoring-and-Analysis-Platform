package com.example.passivedatacompose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class ReportFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var userId: String? = null // Variable to store userId

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance()

        // Check if user is signed in (non-null) and update UI accordingly
        val currentUser = auth.currentUser
        currentUser?.let { firebaseUser ->
            val uid = firebaseUser.uid
            // Query userIndex to get the userId using user UID
            val userIndexRef = database.getReference("userIndex")
            userIndexRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        if (childSnapshot.key == uid) {
                            userId = childSnapshot.value.toString()
                            // Use userId to fetch user data
                            fetchUserData(userId!!)
                            break // Exit loop after finding userId
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors here
                }
            })
        }
        // Set onClickListener for the "Add Medical Record" button
        view.findViewById<Button>(R.id.addMedicalRecordButton).setOnClickListener {
            // Open file picker to choose a medical record file
            openFilePicker()
        }
    }

    private fun fetchUserData(userId: String) {
        val userRef = database.getReference("Users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Fetch prescription files from Firebase Storage
                    fetchFiles(userId, "Prescription", R.id.prescriptionContainer)

                    // Fetch medical records files from Firebase Storage
                    fetchFiles(userId, "medical_records", R.id.medicalRecordsContainer)

                    val diagnosisData = snapshot.child("Diagnosis")
                    for (diagnosisSnapshot in diagnosisData.children) {
                        val date = diagnosisSnapshot.child("Date").value.toString()
                        val description = diagnosisSnapshot.child("Description").value.toString()
                        // Display diagnosis data
                        displayDiagnosis(description, date)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun fetchFiles(userId: String, folderName: String, containerId: Int) {
        val storageRef = storage.reference.child("users/$userId/$folderName")
        storageRef.listAll().addOnSuccessListener { result ->
            for (fileRef in result.items) {
                // Display each file name and pass the file reference
                displayFile(fileRef.name, fileRef, containerId)
            }
        }.addOnFailureListener {
            // Handle errors here
        }
    }

    private fun displayDiagnosis(description: String, date: String) {
        val diagnosisContainer = view?.findViewById<LinearLayout>(R.id.diagnosisContainer)
        // Check if the fragment is attached to a context
        if (context != null && diagnosisContainer != null) {
            val diagnosisTextView = TextView(requireContext())
            diagnosisTextView.text = "$description ($date)"
            diagnosisTextView.textSize = 17f
            diagnosisContainer.addView(diagnosisTextView)
        }
    }

    private fun displayFile(fileName: String, fileRef: StorageReference, containerId: Int) {
        val fileContainer = view?.findViewById<LinearLayout>(containerId)
        // Check if the fragment is attached to a context
        if (context != null && fileContainer != null) {
            val fileTextView = TextView(requireContext())
            fileTextView.text = fileName
            fileTextView.textSize = 17f
            fileContainer.addView(fileTextView)

            // Create a button to view the file
            val viewButton = Button(requireContext())
            viewButton.text = "View"
            viewButton.setOnClickListener {
                // Download the file to local storage
                val localFile = File.createTempFile("file", "")
                fileRef.getFile(localFile).addOnSuccessListener {
                    // File downloaded successfully, create content URI
                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().packageName + ".provider",
                        localFile
                    )
                    // Create intent to view the file
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }.addOnFailureListener { exception ->
                    // Handle download failure
                    Toast.makeText(requireContext(), "Failed to download file: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
            fileContainer.addView(viewButton)
        }
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE_FILE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == Activity.RESULT_OK) {
            val selectedFileUri = data?.data
            if (selectedFileUri != null) {
                uploadFileToFirebaseStorage(selectedFileUri)
            }
        }
    }

    private fun uploadFileToFirebaseStorage(fileUri: Uri) {
        val storageRef = storage.reference.child("users/$userId/medical_records")
        val fileName = "medical_record_${System.currentTimeMillis()}.pdf"
        val fileRef = storageRef.child(fileName)
        fileRef.putFile(fileUri)
            .addOnSuccessListener {
                // File uploaded successfully
                Toast.makeText(requireContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Handle upload failure
                Toast.makeText(requireContext(), "Failed to upload file: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_CODE_FILE_PICKER = 123
    }
}
