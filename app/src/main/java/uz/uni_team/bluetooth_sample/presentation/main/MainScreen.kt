package uz.uni_team.bluetooth_sample.presentation.main

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uz.uni_team.bluetooth_sample.R
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice
import uz.uni_team.bluetooth_sample.presentation.chat.ChatScreen
import uz.uni_team.bluetooth_sample.utils.checkPermissions
import uz.uni_team.bluetooth_sample.utils.isPermissionsEnabled


val bluetoothPermissions by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}

class MainScreen : AndroidScreen() {
    @Composable
    override fun Content() {
        val viewModel: MainViewModel = getViewModel()
        val uiState: MainUiState by viewModel.state.collectAsState()
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val snackHostState = remember { SnackbarHostState() }
        val localCoroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.current

        LaunchedEffect(key1 = Unit) {
            viewModel.events.flowWithLifecycle(lifecycle).onEach {
                when (it) {
                    is MainEvents.ShowSnackMessage -> {
                        localCoroutineScope.launch {
                            snackHostState.showSnackbar(it.message)
                        }
                    }

                    is MainEvents.NavigateToChat -> {
                        navigator?.push(ChatScreen(it.device))
                    }
                }
            }.launchIn(this)
        }

        if (uiState.isConnecting) {
            ConnectingStateContent()
        } else {
            MainContent(
                uiState = uiState,
                onScanOrStop = {
                    if (uiState.isScanning) {
                        viewModel.stopDiscovery()
                    } else {
                        if (context.isPermissionsEnabled(bluetoothPermissions)) {
                            viewModel.startDiscovery()
                        } else {
                            context.checkPermissions(
                                permission = bluetoothPermissions,
                                onGranted = {
                                    viewModel.startDiscovery()
                                },
                                onDenied = {
                                    viewModel.showSnackMessage("Denied")
                                },
                            )
                        }
                    }
                },
                onDeviceClick = viewModel::connectToDevice,
                onStartServer = viewModel::waitForIncomingConnections
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent(
        uiState: MainUiState,
        onScanOrStop: () -> Unit,
        onDeviceClick: (BluetoothDevice) -> Unit,
        onStartServer: () -> Unit
    ) {
        val textButtonState: String by remember(uiState.isScanning) {
            mutableStateOf(if (uiState.isScanning) "Stop" else "Scan")
        }
        Column(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        actions = {
                            if (uiState.isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            }
                            TextButton(onClick = onScanOrStop) {
                                Text(text = textButtonState)
                            }
                            TextButton(onClick = onStartServer) {
                                Text(text = "Server")
                            }
                        },
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        if (uiState.pairedDevices.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Paired devices",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            items(items = uiState.pairedDevices) { pairedDevice ->
                                PairedDeviceItem(
                                    bluetoothDevice = pairedDevice,
                                    onClick = {
                                        onDeviceClick.invoke(pairedDevice)
                                    },
                                    onSettingsClick = {},
                                )
                            }
                        }
                        if (uiState.scannedDevices.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Scanned devices",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                            items(items = uiState.scannedDevices) { scannedDevice ->
                                ScannedDeviceItem(bluetoothDevice = scannedDevice) {
                                    onDeviceClick.invoke(scannedDevice)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ConnectingStateContent() {
        var connectingCount: Int by remember {
            mutableIntStateOf(1)
        }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                connectingCount += 1
                if (connectingCount > 3) connectingCount = 0
            }
        }


        val connectingText = "Connecting ".apply {
            for (i in 1..connectingCount) {
                plus(".")
            }
        }


        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text(text = connectingText)
            }
        }
    }
}