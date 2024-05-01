package com.example.passivedatacompose.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
//import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.passivedatacompose.data.PassiveDataRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class WearableBackgroundService : Service(), MessageClient.OnMessageReceivedListener {

    private val TAG_MESSAGE_RECEIVED: String = "receive1"
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var messageEvent: MessageEvent? = null
    private var mobileDeviceConnected: Boolean = false
    private var mobileNodeUri: String? = null
    private var activityContext: Context? = null
    private lateinit var context: Context // Define context variable
    // Define a Handler
    private val handler = Handler()
    private val sendDataRunnable = object : Runnable {
        override fun run() {
            if (mobileDeviceConnected) {
                // Get the current time
                val latestHeartRate = runBlocking { PassiveDataRepository(context).latestHeartRate.firstOrNull() }
                val currentTime = System.currentTimeMillis()

                // Construct the default message
                val hrString = "$latestHeartRate,$currentTime" // Concatenate heart rate and timestamp

                // Convert the message to a byte array
                val payload: ByteArray = hrString.toByteArray()

                // Send the message
                val sendMessageTask =
                    Wearable.getMessageClient(activityContext!!)
                        .sendMessage(mobileNodeUri!!, MESSAGE_ITEM_RECEIVED_PATH, payload)

                sendMessageTask.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("send1", "Message sent successfully")
                    } else {
                        Log.d("send1", "Message failed.")
                    }
                }
            } else {
                Log.d("send1", "Mobile device is not connected. Data not sent.")
            }

            // Schedule the next execution after 1 minute
            handler.postDelayed(this, 60000) // 60000 milliseconds = 1 minute
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext // Assign context in onCreate
        activityContext = applicationContext
        Wearable.getMessageClient(activityContext!!).addListener(this)
        handler.post(sendDataRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(activityContext!!).removeListener(this)
        handler.removeCallbacks(sendDataRunnable)
    }

    override fun onMessageReceived(p0: MessageEvent) {
        try {
            Log.d(TAG_MESSAGE_RECEIVED, "onMessageReceived event received")
            val s1 = String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path

            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() A message from watch was received:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s1
            )

            if (messageEventPath.isNotEmpty() && messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                try {
                    val nodeId: String = p0.sourceNodeId.toString()
                    val returnPayloadAck = wearableAppCheckPayloadReturnACK
                    val payload: ByteArray = returnPayloadAck.toByteArray()

                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)

                    Log.d(
                        TAG_MESSAGE_RECEIVED, "Acknowledgement message successfully with payload : $returnPayloadAck"
                    )

                    messageEvent = p0
                    mobileNodeUri = p0.sourceNodeId

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message sent successfully")
                            mobileDeviceConnected = true
                        } else {
                            Log.d(TAG_MESSAGE_RECEIVED, "Message failed.")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }//end of if*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


