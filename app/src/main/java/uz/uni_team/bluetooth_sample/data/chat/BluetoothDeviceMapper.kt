package uz.uni_team.bluetooth_sample.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.mapToBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(name = this.name, deviceAddress = address)
}
