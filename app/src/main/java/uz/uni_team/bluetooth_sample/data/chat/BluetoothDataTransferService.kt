package uz.uni_team.bluetooth_sample.data.chat

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import uz.uni_team.bluetooth_sample.domain.chat.BluetoothMessage
import uz.uni_team.bluetooth_sample.domain.chat.TransferFailedException
import java.io.IOException
import java.net.UnknownHostException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    fun listenForIncomingMessages(): Flow<BluetoothMessage> = flow {

        if (!socket.isConnected) {
           throw UnknownHostException()
        }
        val buffer = ByteArray(1024)
            println("Keldi")
        while (true) {
            val byteCount = try {
                socket.inputStream.read(buffer)
            } catch (e: IOException) {
                throw TransferFailedException()
            }

            emit(
                buffer.decodeToString(
                    endIndex = byteCount
                ).toBluetoothMessage(
                    isFromLocalUser = false
                )
            )
        }
    }.flowOn(Dispatchers.IO)


    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext false
            }
            true
        }
    }
}