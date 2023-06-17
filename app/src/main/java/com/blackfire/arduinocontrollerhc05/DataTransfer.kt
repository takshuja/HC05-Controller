package com.blackfire.arduinocontrollerhc05

import android.bluetooth.BluetoothSocket
import java.io.InputStream
import java.io.OutputStream

class BluetoothDataStream(
    bluetoothSocket: BluetoothSocket
){
    private var bluetoothOutputStream: OutputStream= bluetoothSocket.outputStream
    fun sendData(data: String) {
        bluetoothOutputStream.write(data.toByteArray())
    }
}