package uz.uni_team.bluetooth_sample.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothController
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {

    private val _state: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private val _events: Channel<MainEvents> = Channel(Channel.BUFFERED)
    val events: Flow<MainEvents> = _events.receiveAsFlow()

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
    }

    fun startDiscovery() {
        _state.update { it.copy(isScanning = true) }
        bluetoothController.startDiscovery()
    }

    fun stopDiscovery() {
        _state.update { it.copy(isScanning = false) }
        bluetoothController.stopDiscovery()
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }

    fun showSnackMessage(message: String) {
        viewModelScope.launch {
            _events.send(MainEvents.ShowSnackMessage(message))
        }
    }
}