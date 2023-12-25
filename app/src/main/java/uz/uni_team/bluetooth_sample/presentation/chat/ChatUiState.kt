package uz.uni_team.bluetooth_sample.presentation.chat

import uz.uni_team.bluetooth_sample.domain.chat.BluetoothMessage

data class ChatUiState(
    val messages: List<BluetoothMessage> = emptyList()
)
