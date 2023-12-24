package uz.uni_team.bluetooth_sample.presentation.main

sealed interface MainEvents {
    data class ShowSnackMessage(val message: String):MainEvents
}