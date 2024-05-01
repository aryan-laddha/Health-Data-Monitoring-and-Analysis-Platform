package com.example.passivedatacompose

import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.IBinder
import android.util.Log
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType

class InfluxDBService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun sendDataToServer(data: String) {

        Log.d(TAG, "Sending data to server: $data")

        val url = "http://10.0.2.2:8080/heartRate" // Replace with your server endpoint
        //val url = "http://localhost:8080/heartRate" // Replace with your server endpoint
        val client = OkHttpClient()


        val requestBody = RequestBody.create("application/json".toMediaType(), data)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send data to server: ${e.message}")
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Unexpected response code: $response")
                        // Handle unexpected response
                    } else {
                        Log.d(TAG, "Data sent successfully")
                        // Handle success
                    }
                }
            }
        })
    }

}