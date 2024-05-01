
package com.example.passivedatacompose

import android.app.Application
import com.example.passivedatacompose.data.HealthServicesRepository
import com.example.passivedatacompose.data.PassiveDataRepository

const val TAG = "Passive Data Sample"
const val PERMISSION = android.Manifest.permission.BODY_SENSORS

class MainApplication : Application() {
    val healthServicesRepository by lazy { HealthServicesRepository(this) }
    val passiveDataRepository by lazy { PassiveDataRepository(this) }
}
