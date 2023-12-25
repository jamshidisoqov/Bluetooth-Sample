package uz.uni_team.bluetooth_sample.presentation.main

import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice

sealed interface MainEvents {
    data class ShowSnackMessage(val message: String) : MainEvents

    data class NavigateToChat(val device: BluetoothDevice):MainEvents
}