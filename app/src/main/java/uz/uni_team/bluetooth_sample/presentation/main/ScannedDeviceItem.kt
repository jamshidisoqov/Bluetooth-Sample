package uz.uni_team.bluetooth_sample.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDeviceDomain
import uz.uni_team.bluetooth_sample.presentation.theme.BluetoothSampleTheme

@Composable
fun ScannedDeviceItem(bluetoothDevice: BluetoothDevice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .background(
                color = Color(0x80A3A3A3),
                shape = CircleShape,
            )
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = bluetoothDevice.name ?: "No name", fontSize = 18.sp, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ScannedDeviceItemPreview() {
    BluetoothSampleTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            ScannedDeviceItem(
                bluetoothDevice = BluetoothDeviceDomain("Aqil mazgi", "America"),
            ){

            }
        }
    }
}