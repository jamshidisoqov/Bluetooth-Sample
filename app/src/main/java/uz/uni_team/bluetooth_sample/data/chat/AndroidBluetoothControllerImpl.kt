package uz.uni_team.bluetooth_sample.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothController
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDeviceDomain
import uz.uni_team.bluetooth_sample.domain.chat.ConnectionResult
import uz.uni_team.bluetooth_sample.utils.isPermissionEnabled
import java.io.IOException
import java.util.UUID


@SuppressLint("MissingPermission")
class AndroidBluetoothControllerImpl(private val context: Context) : BluetoothController {

    private val _scannedDevices: MutableStateFlow<List<BluetoothDeviceDomain>> =
        MutableStateFlow(emptyList())

    private val _pairedDevices: MutableStateFlow<List<BluetoothDeviceDomain>> =
        MutableStateFlow(emptyList())

    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothManager.adapter
    }

    private var currentServerSocket: BluetoothServerSocket? = null

    private var currentClientSocket: BluetoothSocket? = null

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val foundDeviceReceiver by lazy {
        FoundDeviceReceiver { device ->
            _scannedDevices.update { devices ->
                val newDevice = device.mapToBluetoothDeviceDomain()
                if (newDevice in devices) devices else devices + newDevice
            }
        }
    }

    init {
        getPairedDevices()
    }

    private fun openBluetoothRequest() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(enableBtIntent)
    }

    private fun getPairedDevices() {
        if (!context.isPermissionEnabled(Manifest.permission.BLUETOOTH_SCAN)) return
        if (!isBluetoothEnabled) return openBluetoothRequest()
        bluetoothAdapter?.bondedDevices?.map {
            it.mapToBluetoothDeviceDomain()
        }?.also { devices ->
            _pairedDevices.update { devices }
        }
    }

    override fun startDiscovery() {
        if (!context.isPermissionEnabled(Manifest.permission.BLUETOOTH_SCAN)) return
        if (!isBluetoothEnabled) return openBluetoothRequest()

        context.registerReceiver(
            foundDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        getPairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!context.isPermissionEnabled(Manifest.permission.BLUETOOTH_SCAN)) return
        if (!isBluetoothEnabled) return openBluetoothRequest()
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> = flow {
        if (!context.isPermissionEnabled(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException("No bluetooth connection permission")
        }

        currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
            "bluetooth_sample_service", UUID.fromString(SERVICE_UUID)
        )

        var shouldLoop = true
        while (shouldLoop) {
            currentClientSocket = try {
                currentServerSocket?.accept()
            } catch (e: IOException) {
                shouldLoop = false
                null
            }
            emit(ConnectionResult.ConnectionEstablished)
            currentClientSocket?.let {
                currentServerSocket?.close()
            }
        }

    }.onCompletion {
        closeConnection()
    }.flowOn(Dispatchers.IO).flowOn(Dispatchers.IO)

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> = flow {
        if (!context.isPermissionEnabled(Manifest.permission.BLUETOOTH_CONNECT)) {
            throw SecurityException("No bluetooth connection permission")
        }

        currentClientSocket = bluetoothAdapter.getRemoteDevice(device.deviceAddress)
            .createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))

        stopDiscovery()

        currentClientSocket?.let { socket ->
            try {
                socket.connect()
                emit(ConnectionResult.ConnectionEstablished)
            } catch (e: Exception) {
                socket.close()
                currentClientSocket = null
                emit(ConnectionResult.Error(e.message.toString()))
            }
        }

    }.onCompletion {
        closeConnection()
    }.flowOn(Dispatchers.IO)

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentServerSocket = null
        currentClientSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        closeConnection()
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}