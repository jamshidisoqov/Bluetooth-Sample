package uz.uni_team.bluetooth_sample.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothController
import uz.uni_team.bluetooth_sample.utils.logging
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val controller: BluetoothController
) : ViewModel() {


    private val _state: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState())
    val state: Flow<ChatUiState> = _state.asStateFlow()


    init {
        listenMessages()
    }


    fun sendMessage(message: String) {
        viewModelScope.launch {
            val bluetoothMessage = controller.trySendMessage(message)
            if (bluetoothMessage != null) {
                _state.update {
                    it.copy(
                        messages = it.messages + bluetoothMessage
                    )
                }
            }
        }
    }

    private fun listenMessages() {
        viewModelScope.launch {
            controller.getSubscribeMessages().catch {
                logging(it.message)
            }.collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
    }

    fun disconnectFromDevice() {
        controller.closeConnection()
    }
}