package uz.uni_team.bluetooth_sample.domain.chat

import java.io.IOException

class TransferFailedException: IOException("Reading incoming data failed")