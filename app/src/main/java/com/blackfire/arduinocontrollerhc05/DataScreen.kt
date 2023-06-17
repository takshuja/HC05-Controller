package com.blackfire.arduinocontrollerhc05

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blackfire.arduinocontrollerhc05.ui.theme.ArduinoControllerHC05Theme

class DataScreen : ComponentActivity() {

    var sensorList = mutableListOf("")
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothSocket: BluetoothSocket
    lateinit var address: String
    lateinit var connection: BluetoothHandler

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        address = intent.getStringExtra("Address")!!
        connection = BluetoothHandler(bluetoothAdapter, address)
        connection.start()
        bluetoothSocket = connection.createSocket(bluetoothAdapter.getRemoteDevice(address))
        Thread{
            bluetoothSocket.connect()
        }.start()
        try{
//            Handler().postDelayed(
//                { receiveData() },5000
//            )

        }catch (e: Exception) {
            Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show()
        }
        setContent {
            ArduinoControllerHC05Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                }
            }
        }
    }


    @Composable
    fun Screen() {
        Column(modifier = Modifier.fillMaxSize()) {

        }
    }

    @Composable
    fun items() {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
                .border(color = Color.Black, shape = RoundedCornerShape(12.dp), width = 4.dp)
        ) {

        }
    }


    fun receiveData() {
        val inputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int
        var readMessage: String
    }
}