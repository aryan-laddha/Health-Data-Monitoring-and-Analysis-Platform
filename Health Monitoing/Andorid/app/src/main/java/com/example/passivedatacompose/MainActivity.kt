package com.example.passivedatacompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.passivedatacompose.presentation.PassiveDataApp
import com.example.passivedatacompose.service.WearableBackgroundService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthServicesRepository = (application as MainApplication).healthServicesRepository
        val passiveDataRepository = (application as MainApplication).passiveDataRepository



        setContent {
            PassiveDataApp(
                healthServicesRepository = healthServicesRepository,
                passiveDataRepository = passiveDataRepository
            )
        }
        // Start the WearableBackgroundService
        startService(Intent(this, WearableBackgroundService::class.java))
    }
    override fun onDestroy() {
        super.onDestroy()
        // Stop the WearableBackgroundService when the activity is destroyed
        stopService(Intent(this, WearableBackgroundService::class.java))
    }
}
