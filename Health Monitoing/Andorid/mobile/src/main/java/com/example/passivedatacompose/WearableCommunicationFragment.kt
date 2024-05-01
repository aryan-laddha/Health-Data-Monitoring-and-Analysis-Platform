package com.example.passivedatacompose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.passivedatacompose.databinding.FragmentWearableCommunicationBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class WearableCommunicationFragment : Fragment(), CoroutineScope by MainScope(),
    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var userId: String? = null

    private var activityContext: Context? = null
    private val wearableAppCheckPayload = "AppOpenWearable"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var wearableDeviceConnected: Boolean = false

    private var currentAckFromWearForAppOpenCheck: String? = null
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    private val TAG_GET_NODES: String = "getnodes1"
    private val TAG_MESSAGE_RECEIVED: String = "receive1"

    private var messageEvent: MessageEvent? = null
    private var wearableNodeUri: String? = null

    private lateinit var binding: FragmentWearableCommunicationBinding
    private val CONNECTION_CHECK_INTERVAL = 8000L // Interval in milliseconds (e.g., 5000 milliseconds = 5 seconds)

    // Timestamp to track the last message received
    private var lastMessageTimestamp: Long = 0

    // Timeout duration in milliseconds (adjust as needed)
    private val TIMEOUT_DURATION = 70000 // 1 minute

    // Coroutine job for timeout mechanism
    private var timeoutJob: Job? = null
    val influxDBService = InfluxDBService()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWearableCommunicationBinding.inflate(inflater, container, false)
        activityContext = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseDevicePairing(requireActivity())
        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, proceed to fetch userId
            fetchUserId(currentUser)
        } else {
            // No user is signed in, handle as needed
        }
        // Start a coroutine to continuously check for connection status
        launch {
            while (!wearableDeviceConnected) {
                // Wait for a certain interval before checking again
                delay(CONNECTION_CHECK_INTERVAL)
                // Check for connection status
                initialiseDevicePairing(requireActivity())
            }
        }
        startTimeoutCoroutine()
    }
    private fun fetchUserId(user: FirebaseUser) {
        // Get the user's UID
        val userUid = user.uid

        // Reference to userIndex node in database
        val userIndexRef = database.child("userIndex")

        // Retrieve userId using user UID
        userIndexRef.child(userUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userId = snapshot.value as? String
                if (userId != null) {
                    // UserId successfully fetched, store it in class-level variable
                    this@WearableCommunicationFragment.userId = userId
                    // Now you can use userId elsewhere in the class
                    Log.d("Firebase IN WEARABLE", "User ID: $userId")
                    // Now you can use userId to fetch user data from 'Users' node if needed
                } else {
                    // User ID not found or null, handle as needed
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Error handling
                Log.e("Firebase", "Failed to fetch user ID: ${error.message}")
            }
        })
    }



    private fun startTimeoutCoroutine() {
        timeoutJob = launch {
            while (true) {
                // Calculate the expected timestamp for the next minute's data
                val expectedNextMinuteTimestamp = lastMessageTimestamp + TIMEOUT_DURATION

                // Calculate the remaining time until the expected next minute
                val remainingTime = expectedNextMinuteTimestamp - System.currentTimeMillis()

                // If the remaining time is negative, it means we missed the next minute's data
                if (remainingTime <= 0) {
                    // Generate logs to indicate Wear OS connection may have been closed
                    wearableDeviceConnected = false
                    Log.d("WearOSConnection", "Wear OS connection might be closed.")

                    // Re-establish connection and update UI
                    withContext(Dispatchers.Main) {
                        initialiseDevicePairing(activityContext as Activity)
                    }
                } else {
                    // Log remaining time until next minute's data
                    Log.d("WearOSConnection", "Next minute's data expected in ${remainingTime / 1000} seconds.")
                }

                // Delay until the next check (e.g., every second)
                delay(1000)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        Log.d("WearableCommunication", "initialiseDevicePairing() called")
        //Coroutine
        launch(Dispatchers.Default) {
            var getNodesResBool: BooleanArray? = null

            try {
                getNodesResBool = getNodes(tempAct.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //UI Thread
            withContext(Dispatchers.Main) {
                if (getNodesResBool!![0]) {
                    //if message Acknowledgement Received
                    if (getNodesResBool[1]) {
                        Toast.makeText(
                            activityContext,
                            "Wearable device paired and app is open. Tap the \"Send Message to Wearable\" button to send the message to your wearable device.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.deviceconnectionStatusTv.text =
                            "Wearable device paired and app is open."
                        binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                        wearableDeviceConnected = true

                        Log.d("MainActivity", "UI update in initialiseDevicePairing()")

                        // binding.sendmessageButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            activityContext,
                            "A wearable device is paired but the wearable app on your watch isn't open. Launch the wearable app and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.deviceconnectionStatusTv.text =
                            "Wearable device paired but app isn't open."
                        binding.deviceconnectionStatusTv.visibility = View.VISIBLE

                        Log.d("MainActivity", "UI update in initialiseDevicePairing()")

                        wearableDeviceConnected = false
                        // binding.sendmessageButton.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        activityContext,
                        "No wearable device paired. Pair a wearable device to your phone using the Wear OS app and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.deviceconnectionStatusTv.text =
                        "Wearable device not paired and connected."
                    binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                    wearableDeviceConnected = false

                    //binding.sendmessageButton.visibility = View.GONE
                }
            }
        }
    }

    private fun getNodes(context: Context): BooleanArray {
        val nodeResults = HashSet<String>()
        val resBool = BooleanArray(2)
        resBool[0] = false //nodePresent
        resBool[1] = false //wearableReturnAckReceived
        val nodeListTask =
            Wearable.getNodeClient(context).connectedNodes
        try {
            // Block on a task and get the result synchronously (because this is on a background thread).
            val nodes = Tasks.await(nodeListTask)
            Log.e(TAG_GET_NODES, "Task fetched nodes")
            for (node in nodes) {
                Log.e(TAG_GET_NODES, "inside loop")
                nodeResults.add(node.id)
                try {
                    val nodeId = node.id
                    // Set the data of the message to be the bytes of the Uri.
                    val payload: ByteArray = wearableAppCheckPayload.toByteArray()
                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(context)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)
                    try {
                        // Block on a task and get the result synchronously (because this is on a background thread).
                        val result = Tasks.await(sendMessageTask)
                        Log.d(TAG_GET_NODES, "send message result : $result")
                        resBool[0] = true

                        //Wait for 700 ms/0.7 sec for the acknowledgement message
                        //Wait 1
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(100)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 1")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 2
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(250)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 2")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 3
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(350)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 5")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        resBool[1] = false
                        Log.d(
                            TAG_GET_NODES,
                            "ACK thread timeout, no message received from the wearable "
                        )
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                } catch (e1: Exception) {
                    Log.d(TAG_GET_NODES, "send message exception")
                    e1.printStackTrace()
                }
            } //end of for loop
        } catch (exception: Exception) {
            Log.e(TAG_GET_NODES, "Task failed: $exception")
            exception.printStackTrace()
        }
        return resBool
    }


    override fun onDataChanged(p0: DataEventBuffer) {
    }

    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        try {
            val s = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path
            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() Received a message from watch:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s
            )
            lastMessageTimestamp = System.currentTimeMillis()

            //binding.latestMessageTextView.text = s
            val parts = s.split(",")
            if (parts.isNotEmpty()) {
                val heartRateStr = parts[0].trim() // Trim to remove leading/trailing spaces
                val heartRate = heartRateStr.toDoubleOrNull()?.toInt() // Convert to double and then to integer
                //binding.latestMessageTextView.text = heartRate.toString()
                if (heartRate != null ) {
                    binding.latestMessageTextView.text = heartRate.toString()
                    val storedUserId = userId
                    val json = """
                    {
                        "userid": "$storedUserId",
                        "rate": $heartRate
                    }
                """.trimIndent()


                    // Call function to send data to server
                    influxDBService.sendDataToServer(json)
                }
            }

            if (messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                currentAckFromWearForAppOpenCheck = s
                Log.d(
                    TAG_MESSAGE_RECEIVED,
                    "Received acknowledgement message that app is open in wear"
                )

                val sbTemp = StringBuilder()
                // sbTemp.append(binding.messagelogTextView.text.toString())
                sbTemp.append("\nWearable device connected.")
                Log.d("receive1", " $sbTemp")
                //binding.messagelogTextView.text = sbTemp
                //binding.textInputLayout.visibility = View.VISIBLE

                //binding.checkwearablesButton.visibility = View.GONE
                messageEvent = p0
                wearableNodeUri = p0.sourceNodeId
            } else if (messageEventPath.isNotEmpty() && messageEventPath == MESSAGE_ITEM_RECEIVED_PATH) {

                try {
                    //  binding.messagelogTextView.visibility = View.VISIBLE
                    //  binding.textInputLayout.visibility = View.VISIBLE
                    //binding.sendmessageButton.visibility = View.VISIBLE

                    val sbTemp = StringBuilder()
                    sbTemp.append("\n")
                    sbTemp.append(s)
                    sbTemp.append(" - (Received from wearable)")
                    Log.d("receive1", " $sbTemp")
                    // binding.messagelogTextView.append(sbTemp)

                    //   binding.scrollviewText.requestFocus()
                    /* binding.scrollviewText.post {
                         binding.scrollviewText.scrollTo(0, binding.scrollviewText.bottom)
                     }*/
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("receive1", "Handled")
        }
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        /* if (capabilityInfo.nodes.isEmpty()) {
             // Wearable device has been unpaired
             // Handle the unpairing event here
             wearableDeviceConnected = false
         }*/

    }


    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        timeoutJob?.cancel()
    }


    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startTimeoutCoroutine()
    }
}
