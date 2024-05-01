
package com.example.passivedatacompose.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.passivedatacompose.PERMISSION
import com.example.passivedatacompose.theme.PassiveDataTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PassiveDataScreen(
    hrValue: Double,
    hrEnabled: Boolean,
    onEnableClick: (Boolean) -> Unit,
    permissionState: PermissionState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeartRateToggle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            checked = hrEnabled,
            onCheckedChange = onEnableClick,
            permissionState = permissionState
        )
        HeartRateCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            heartRate = hrValue
        )
    }
}

@ExperimentalPermissionsApi
@Preview(
    device = Devices.WEAR_OS_SMALL_ROUND,
    showBackground = false,
    showSystemUi = true
)
@Composable
fun PassiveDataScreenPreview() {
    val permissionState = object : PermissionState {
        override val permission = PERMISSION
        override val status: PermissionStatus = PermissionStatus.Granted
        override fun launchPermissionRequest() {}
    }
    PassiveDataTheme {
        PassiveDataScreen(
            hrValue = 65.6,
            hrEnabled = true,
            onEnableClick = {},
            permissionState = permissionState
        )
    }
}
