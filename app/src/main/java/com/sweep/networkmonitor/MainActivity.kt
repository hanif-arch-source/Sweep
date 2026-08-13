package com.sweep.networkmonitor

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sweep.networkmonitor.ui.navigation.SweepNavHost
import com.sweep.networkmonitor.ui.theme.SweepTheme
import com.sweep.networkmonitor.viewmodel.NetworkViewModel
import com.sweep.networkmonitor.viewmodel.NetworkViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: NetworkViewModel by viewModels {
        NetworkViewModelFactory(applicationContext)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Sweep degrades gracefully when a permission is denied — see Section 14 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRuntimePermissions()

        setContent {
            SweepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SweepNavHost(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * Requests only the permissions needed for features that are actually
     * implemented: location (required by Android to read SSID / perform
     * local network discovery on many OS versions) and notifications
     * (Android 13+, required to post alert notifications).
     */
    private fun requestRuntimePermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
