package uz.uni_team.bluetooth_sample.domain.chat

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?, val deviceAddress: String
)