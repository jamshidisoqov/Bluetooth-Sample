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
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothController
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDeviceDomain
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothMessage
import uz.uni_team.bluetooth_sample.domain.chat.ConnectionResult
import uz.uni_team.bluetooth_sample.utils.isPermissionEnabled
import java.io.IOException
import java.util.UUID
import kotlin.properties.Delegates


@SuppressLint("MissingPermission")
class AndroidBluetoothControllerImpl(private val context: Context) : BluetoothController {

    init {
        println("init")
    }

    private val _scannedDevices: MutableStateFlow<List<BluetoothDeviceDomain>> = MutableStateFlow(emptyList())

    private val _pairedDevices: MutableStateFlow<List<BluetoothDeviceDomain>> = MutableStateFlow(emptyList())

    private lateinit var dataTransferService: BluetoothDataTransferService

    private var _lastConnectedDevice: BluetoothDeviceDomain by Delegates.notNull()
    override val lastConnectedDevice: BluetoothDeviceDomain
        get() = _lastConnectedDevice

    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean>
        get() = _isScanning

    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val _messages: ArrayList<BluetoothMessage> = arrayListOf()


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

    private val scanPermission by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.BLUETOOTH_ADMIN
        }
    }

    private val connectPermission by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH_ADMIN
        }
    }

    private val foundDeviceReceiver by lazy {
        FoundDeviceReceiver { device ->
            _scannedDevices.update { devices ->
                val newDevice = device.mapToBluetoothDeviceDomain()
                if (newDevice in devices) devices else devices + newDevice
            }
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver(
        onStateChanged = { isConnected, bluetoothDevice ->
            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
                println("Is connected :$isConnected" +
                        "")
                _lastConnectedDevice = bluetoothDevice.mapToBluetoothDeviceDomain()
                _isConnected.update { isConnected }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    _errors.emit("Can't connect to a non-paired device.")
                }
            }
        },
        onBluetoothTurnStateChanged = {
            closeConnection()
            openBluetoothRequest()
        },
    )

    private val bluetoothDiscoveryStateReceiver by lazy {
        BluetoothDiscoveryStateReceiver(
            onDiscoveryStarted = {
                _isScanning.tryEmit(true)
            },
            onDiscoveryFinished = {
                _isScanning.tryEmit(false)
            },
        )
    }

    init {
        getPairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            },
        )
        context.registerReceiver(
            bluetoothDiscoveryStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            },
        )
    }


    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!context.isPermissionEnabled(connectPermission)) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message, senderName = bluetoothAdapter?.name ?: "Unknown name", isFromLocalUser = true
        )

        dataTransferService.sendMessage(bluetoothMessage.toByteArray()).also {
            if (it) {
                _messages.add(bluetoothMessage)
            }
        }

        return bluetoothMessage
    }

    private fun openBluetoothRequest() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(enableBtIntent)
    }

    private fun getPairedDevices() {
        if (!context.isPermissionEnabled(scanPermission)) return
        bluetoothAdapter?.bondedDevices?.map {
            it.mapToBluetoothDeviceDomain()
        }?.also { devices ->
            _pairedDevices.update { devices }
        }
    }

    override fun startDiscovery() {
        if (!context.isPermissionEnabled(scanPermission)) return
        if (!isBluetoothEnabled) return openBluetoothRequest()

        context.registerReceiver(
            foundDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        getPairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!context.isPermissionEnabled(scanPermission)) return
        if (!isBluetoothEnabled) return openBluetoothRequest()
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> = flow {
        if (!context.isPermissionEnabled(connectPermission)) {
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
            currentClientSocket?.also {

                val service = BluetoothDataTransferService(it)

                dataTransferService = service

                currentServerSocket?.close()

            }
            emit(ConnectionResult.ConnectionEstablished)
        }

    }.flowOn(Dispatchers.IO).flowOn(Dispatchers.IO)

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> = flow {

        if (!context.isPermissionEnabled(connectPermission)) {
            throw SecurityException("No bluetooth connection permission")
        }

        currentClientSocket = bluetoothAdapter.getRemoteDevice(device.deviceAddress).apply {
            this.createBond()
        }.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))

        stopDiscovery()

        currentClientSocket?.let { socket ->
            try {
                socket.connect()
                println("data 1")
                dataTransferService = BluetoothDataTransferService(socket)
                println("data 2")
                emit(ConnectionResult.ConnectionEstablished)
            } catch (e: Exception) {
                socket.close()
                currentClientSocket = null
                emit(ConnectionResult.Error(e.message.toString()))
            }
        }

    }.flowOn(Dispatchers.IO)


    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentServerSocket = null
        currentClientSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        context.unregisterReceiver(bluetoothDiscoveryStateReceiver)
        closeConnection()
    }

    override fun getSubscribeMessages(): Flow<List<BluetoothMessage>> = callbackFlow {
        dataTransferService.listenForIncomingMessages().onEach {
            _messages.add(it)
            trySend(_messages.toList())
        }.collect()
        awaitClose()
    }.flowOn(Dispatchers.IO)

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}