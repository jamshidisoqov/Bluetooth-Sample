package uz.uni_team.bluetooth_sample.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothDevice
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothMessage
import uz.uni_team.bluetooth_sample.presentation.main.MainViewModel

class ChatScreen(device: BluetoothDevice) : AndroidScreen() {
    @Composable
    override fun Content() {
        val viewModel:ChatViewModel = getViewModel<ChatViewModel>()
        val uiState by viewModel.state.collectAsState(initial = ChatUiState())
        val navController = LocalNavigator.current
        ChatScreenContent(chatsList = uiState.messages, onSendMessage = viewModel::sendMessage) {
            viewModel.disconnectFromDevice()
            navController?.pop()
        }
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    @Composable
    private fun ChatScreenContent(
        chatsList: List<BluetoothMessage>, onSendMessage: (String) -> Unit, onDisconnect: () -> Unit
    ) {
        val message = rememberSaveable {
            mutableStateOf("")
        }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Messages", modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDisconnect) {
                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "Disconnect"
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatsList) { message ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChatItem(
                            message = message, modifier = Modifier.align(
                                if (message.isFromLocalUser) {
                                    Alignment.End
                                } else {
                                    Alignment.Start
                                }
                            )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = message.value,
                    onValueChange = { message.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = "Message")
                    },
                )
                IconButton(
                    onClick = {
                        onSendMessage(message.value)
                        message.value = ""
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Send, contentDescription = "Send message"
                    )
                }
            }
        }
    }
}