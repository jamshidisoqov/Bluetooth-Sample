package uz.uni_team.bluetooth_sample.presentation.main

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.flowWithLifecycle
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uz.uni_team.bluetooth_sample.R
import uz.uni_team.bluetooth_sample.utils.checkPermissions
import uz.uni_team.bluetooth_sample.utils.isPermissionsEnabled


val bluetoothPermissions by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        listOf()
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


        LaunchedEffect(key1 = Unit) {
            viewModel.events.flowWithLifecycle(lifecycle).onEach {
                when (it) {
                    is MainEvents.ShowSnackMessage -> {
                        localCoroutineScope.launch {
                            snackHostState.showSnackbar(it.message)
                        }
                    }
                }
            }.launchIn(this)
        }

        MainContent(uiState = uiState) {
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
        }


    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent(uiState: MainUiState, onScanOrStop: () -> Unit) {
        val textButtonState: String by remember(uiState.isScanning) {
            mutableStateOf(if (uiState.isScanning) "Stop" else "Scan")
        }
        Column(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(id = R.string.app_name)) },
                        actions = {
                            TextButton(onClick = onScanOrStop) {
                                Text(text = textButtonState)
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
                                PairedDeviceItem(bluetoothDevice = pairedDevice) {
                                    //Click to open menu
                                }
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
                                    //Click to connect
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}