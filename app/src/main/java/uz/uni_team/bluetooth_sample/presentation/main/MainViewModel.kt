package uz.uni_team.bluetooth_sample.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothController
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDeviceDomain
import uz.uni_team.bluetooth_sample.domain.chat.ConnectionResult
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _state: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private val _events: Channel<MainEvents> = Channel(Channel.BUFFERED)
    val events: Flow<MainEvents> = _events.receiveAsFlow()

    private var deviceConnectionJob: Job? = null

    private var lastConnectionDevice: BluetoothDevice? = null

    init {
        viewModelScope.launch {
            combine(
                bluetoothController.pairedDevices, bluetoothController.scannedDevices
            ) { pairedDevices, scannedDevice ->
                _state.update { mainUiState ->
                    mainUiState.copy(scannedDevices = scannedDevice, pairedDevices = pairedDevices)
                }
            }.collect()
        }

        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
            if (isConnected) {
                _events.send(MainEvents.NavigateToChat(bluetoothController.lastConnectedDevice))
            }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(
                    errorMessage = error
                )
            }
        }.launchIn(viewModelScope)
    }

    fun startDiscovery() {
        _state.update { it.copy(isScanning = true) }
        bluetoothController.startDiscovery()
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        _state.update { it.copy(isConnecting = true) }
        lastConnectionDevice = device
        deviceConnectionJob = bluetoothController.connectToDevice(device).listen()
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().listen()
    }

    fun stopDiscovery() {
        bluetoothController.stopDiscovery()
        _state.update { it.copy(isScanning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("onCleared", "Realize")
        bluetoothController.release()
    }

    fun showSnackMessage(message: String) {
        viewModelScope.launch {
            _events.send(MainEvents.ShowSnackMessage(message))
        }
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true, isConnecting = false, errorMessage = null
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false, isConnecting = false, errorMessage = result.message
                        )
                    }
                }
            }
        }.catch { throwable ->
            bluetoothController.closeConnection()
            _events.send(MainEvents.ShowSnackMessage(throwable.message ?: ""))
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }


}