package uz.uni_team.bluetooth_sample.domain.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val lastConnectedDevice:BluetoothDevice

    val scannedDevices: StateFlow<List<BluetoothDevice>>

    val isScanning:StateFlow<Boolean>

    val pairedDevices: StateFlow<List<BluetoothDevice>>

    val isConnected: StateFlow<Boolean>

    val errors: SharedFlow<String>

    suspend fun trySendMessage(message: String): BluetoothMessage?

    fun startDiscovery()
    fun stopDiscovery()
    fun startBluetoothServer(): Flow<ConnectionResult>

    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>
    fun closeConnection()
    fun release()
    fun getSubscribeMessages():Flow<List<BluetoothMessage>>

}