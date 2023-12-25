package uz.uni_team.bluetooth_sample.data.chat

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothDiscoveryStateReceiver(
    private val onDiscoveryStarted: () -> Unit, private val onDiscoveryFinished: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                onDiscoveryStarted.invoke()
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onDiscoveryFinished.invoke()
            }
        }
    }
}