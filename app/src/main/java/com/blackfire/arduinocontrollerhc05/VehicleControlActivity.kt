package com.blackfire.arduinocontrollerhc05

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException

@Suppress("DEPRECATION")
class VehicleControlActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var address: String
    private lateinit var connection: BluetoothHandler
    private lateinit var bluetoothSocket: BluetoothSocket
    private var sensorDataList = arrayOf("")

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        address = intent.getStringExtra("Address")!!
        connection = BluetoothHandler(bluetoothAdapter, address)
        connection.start()
        bluetoothSocket = connection.createSocket(bluetoothAdapter.getRemoteDevice(address))
        Thread {
            try {

                bluetoothSocket.connect()
            } catch (_: Exception) {

            }
        }.start()

        setContent {
            Display()
            ControlPanel()
        }
    }

//    @Preview
    @Composable
    fun ControlPanel() {
        Column(modifier = Modifier.fillMaxHeight()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(380.dp))
                ControlButton(text = "Fetch Data", data = "+", width = 350.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    ControlButton(text = "Speed1", width = 100.dp, data = "1")
                    ControlButton(text = "Speed2", width = 100.dp, data = "2")
                    ControlButton(text = "Speed3", width = 100.dp, data = "3")
                }

                Spacer(modifier = Modifier.height(40.dp))
                ControlButton(text = "Forward", data = "F")
                Row(horizontalArrangement = Arrangement.SpaceAround) {
                    ControlButton(text = "Left", data = "<")
                    ControlButton(text = "Neutral", data = "N")
                    ControlButton(text = "Right", data = ">")
                }
                ControlButton(text = "Reverse", data = "R")
                Spacer(modifier = Modifier.height(30.dp))
                Row {
                    ControlButton(text = "Sensor Down", data = "D", width = 120.dp)
                    ControlButton(text = "Stop", data = "S")
                    ControlButton(text = "Sensor Up", data = "U", width = 120.dp)
                }

                Button(onClick = {
                    val intent = Intent(this@VehicleControlActivity, ShowDataActivity::class.java)
                    startActivity(intent)
                }, modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 5.dp)
                    .width(200.dp)
                    .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff1b1b1b))
                ) {
                    Text(text = "Show Data")
                }
            }
        }
    }


    private fun saveData(data: String) {
        var fileOutputStream: FileOutputStream? = null
        try{
            fileOutputStream = FileOutputStream("HCO5_data.txt")
            fileOutputStream.write(data.toByteArray())
        }catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                }catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
    }


    @Composable
    private fun Data(width: Float = 1f) {
        // Get the device screen width
        val configuration = LocalConfiguration.current
        val widgetWidth = configuration.screenWidthDp.dp / 2

        Box(
            contentAlignment = Alignment.TopStart,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .border(width = 1.dp, shape = RoundedCornerShape(2.dp), color = Color.Black)
                .background(color = Color.Black),
        ) {
            var sensorValue: String by remember {
                mutableStateOf("")
            }

            val inputStream = bluetoothSocket.inputStream
            val sensorBuffer = ByteArray(1024)
            var sensorBytes: Int
            var sensorReadMessage: String
            Thread {
                // While the hardware is sending data, print the received bytes
                while (true) {
                    try {
                        sensorBytes = inputStream.read(sensorBuffer)
                        val sensorReceiveData = String(sensorBuffer, 0, sensorBytes)
                        if (sensorReceiveData.isNotEmpty()) {
                            sensorReadMessage = sensorReceiveData
                            runOnUiThread {
                                Handler().postDelayed(
                                    {
                                        sensorValue += sensorReceiveData
                                        sensorDataList.plus(sensorReceiveData)
                                    }, 100
                                )
                            }
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                        break
                    }
                }
            }.start()
            ScrollableText(text = sensorValue)
        }
    }

    @Composable
    private fun ScrollableText(text: String) {

//        val listState = rememberLazyListState()
        // Remember CoroutineScope to be able to launch
//        val coroutineScope = rememberCoroutineScope()


        LazyColumn(
            contentPadding = PaddingValues(horizontal = 10.dp),
//            state = listState
        ) {

            for (data in sensorDataList.distinct()) {
                saveData(data+"\n")
                item {
                    Text(
                        text, fontSize = 20.sp, color = Color(0xFF33FF00),
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                }
            }
        }

    }

        @Composable
        fun Display() {
            Row(
                modifier = Modifier
                    .height(360.dp)
                    .fillMaxWidth()
                    .border(
                        shape = RectangleShape,
                        width = 1.dp,
                        color = Color.White
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Data(width = 0.5f)
            }
        }

        @Composable
        fun ControlButton(
            text: String,
            width: Dp = 100.dp,
            data: String,
//            onclick: Unit = sendData(data)
        ) {
            Box {
                Spacer(
                    modifier = Modifier
                        .height(5.dp)
                        .width(10.dp)
                )
                ElevatedButton(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .width(width)
                        .height(40.dp),
                    onClick = { sendData(data) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff1b1b1b))
                ) {
                    Text(text = text)
                }

            }
        }


        @SuppressLint("MissingPermission")
        fun sendData(data: String) {
            Thread { BluetoothDataStream(bluetoothSocket).sendData(data) }.start()
        }


}