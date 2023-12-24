package uz.uni_team.bluetooth_sample.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDeviceDomain
import uz.uni_team.bluetooth_sample.presentation.theme.BluetoothSampleTheme

@Composable
fun PairedDeviceItem(bluetoothDevice: BluetoothDevice, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .background(
                color = Color(0x80A3A3A3),
                shape = CircleShape,
            )
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = bluetoothDevice.name ?: "No name", fontSize = 18.sp, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onSettingsClick) {
            Icon(imageVector = Icons.Rounded.Settings, contentDescription = "")
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PairedDeviceItemPreview() {
    BluetoothSampleTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            PairedDeviceItem(
                bluetoothDevice = BluetoothDeviceDomain("Aqil mazgi", "America"),
                onSettingsClick = {},
            )
        }
    }
}