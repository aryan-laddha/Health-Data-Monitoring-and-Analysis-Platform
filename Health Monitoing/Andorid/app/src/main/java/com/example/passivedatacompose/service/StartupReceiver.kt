package com.example.passivedatacompose.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.passivedatacompose.PERMISSION
import com.example.passivedatacompose.TAG
import com.example.passivedatacompose.data.HealthServicesRepository
import com.example.passivedatacompose.data.PassiveDataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val repository = PassiveDataRepository(context)
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        runBlocking {
            if (repository.passiveDataEnabled.first()) {
                // Make sure we have permission.
                val result = context.checkSelfPermission(PERMISSION)
                if (result == PackageManager.PERMISSION_GRANTED) {
                    scheduleWorker(context)
                } else {
                    // We may have lost the permission somehow. Mark that background data is
                    // disabled so the state is consistent the next time the user opens the app UI.
                    repository.setPassiveDataEnabled(false)
                }
            }
        }
    }

    private fun scheduleWorker(context: Context) {
        // BroadcastReceiver's onReceive must complete within 10 seconds. During device startup,
        // sometimes the call to register for background data takes longer than that and our
        // BroadcastReceiver gets destroyed before it completes. Instead we schedule a WorkManager
        // job to perform the registration.
        Log.i(TAG, "Enqueuing worker")
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RegisterForBackgroundDataWorker>().build()
        )
    }
}

class RegisterForBackgroundDataWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Worker running")
        val healthServicesRepository = HealthServicesRepository(appContext)
        healthServicesRepository.registerForHeartRateData()
        return Result.success()
    }
}
