package uz.uni_team.bluetooth_sample.data.chat

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothStateReceiver(
    private val onStateChanged: (isConnected: Boolean, device: BluetoothDevice) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {

    }
}