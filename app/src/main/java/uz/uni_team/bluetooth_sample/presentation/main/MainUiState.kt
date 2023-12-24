package uz.uni_team.bluetooth_sample.presentation.main

import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice

data class MainUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isScanning:Boolean = false
)