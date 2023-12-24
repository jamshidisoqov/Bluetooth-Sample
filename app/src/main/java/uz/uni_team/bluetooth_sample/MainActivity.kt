package uz.uni_team.bluetooth_sample

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import uz.uni_team.bluetooth_sample.ui.theme.BluetoothSampleTheme
import uz.uni_team.bluetooth_sample.utils.checkPermissions
import uz.uni_team.bluetooth_sample.utils.isPermissionsEnabled

class MainActivity : ComponentActivity() {

    private val bluetoothManager: BluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothPermissions: List<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH
            )
        }
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) { bluetoothManager.adapter }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            checkPermissions(
                bluetoothPermissions,
                onGranted = {

                },
                onDenied = {

                },
            )

            BluetoothSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {

                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    //TODO kotlin contract enabled
    override fun onResume() {
        super.onResume()
        if (isPermissionsEnabled(bluetoothPermissions)) {
            if (bluetoothAdapter.isEnabled) {
                val enableRequest = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableRequest)
            }
        }
    }
}

