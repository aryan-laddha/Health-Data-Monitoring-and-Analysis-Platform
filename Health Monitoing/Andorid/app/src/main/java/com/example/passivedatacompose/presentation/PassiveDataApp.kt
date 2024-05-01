package com.example.passivedatacompose.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.example.passivedatacompose.PERMISSION
import com.example.passivedatacompose.data.HealthServicesRepository
import com.example.passivedatacompose.data.PassiveDataRepository
import com.example.passivedatacompose.theme.PassiveDataTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassiveDataApp(
    healthServicesRepository: HealthServicesRepository,
    passiveDataRepository: PassiveDataRepository
) {
    PassiveDataTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            timeText = { TimeText() }
        ) {
            val viewModel: PassiveDataViewModel = viewModel(
                factory = PassiveDataViewModelFactory(
                    healthServicesRepository = healthServicesRepository,
                    passiveDataRepository = passiveDataRepository
                )
            )
            val hrValue by viewModel.hrValue.collectAsState()
            val hrEnabled by viewModel.hrEnabled.collectAsState()
            val uiState by viewModel.uiState

            if (uiState == UiState.Supported) {
                val permissionState = rememberPermissionState(
                    permission = PERMISSION,
                    onPermissionResult = { granted ->
                        if (granted) viewModel.toggleEnabled()
                    }
                )
                PassiveDataScreen(
                    hrValue = hrValue,
                    hrEnabled = hrEnabled,
                    onEnableClick = { viewModel.toggleEnabled() },
                    permissionState = permissionState
                )
            } else if (uiState == UiState.NotSupported) {
                NotSupportedScreen()
            }
        }
    }
}
