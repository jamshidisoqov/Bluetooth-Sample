package uz.uni_team.bluetooth_sample.presentation.main

import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothMessage

data class MainUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isScanning:Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
)