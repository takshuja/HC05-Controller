@file:Suppress("DEPRECATION", "UNUSED_EXPRESSION")

package com.blackfire.arduinocontrollerhc05

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val bondedDevices = mutableListOf<BluetoothDevice>()
    private lateinit var bluetoothHandler: BluetoothHandler
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var device: BluetoothDevice
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionManager(this).requestPermissions()
        bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter
        progressDialog = ProgressDialog(this)

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        registerReceiver(DeviceConnectionReceiver(), filter)

        if (!bluetoothAdapter.isEnabled) {
            val requestBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(requestBtIntent, 0)
            finish()
            startActivity(intent)
        }

        // Add bonded bluetooth devices to list
        for (device in bluetoothAdapter.bondedDevices) {
            bondedDevices.add(device)
        }

        setContent {
            Scaffold(
                topBar = {
                        TopAppBar(
                            title = {
                                Text("HC05 Controller", fontSize = 26.sp, color = Color.White)
                            },
                            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xff000000))
                        )
                    },
                    content = { paddingValues ->
                        paddingValues
                        MainUI(bondedDevices = bondedDevices)
                    }
            )
        }
    }

    private var count = 2

    private fun connectToDevice(deviceAddress: String) {
        device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        try {
            bluetoothHandler = BluetoothHandler(bluetoothAdapter, deviceAddress)
            bluetoothHandler.start()
            try {
                bluetoothSocket = bluetoothHandler.createSocket(device)
                if (bluetoothSocket.isConnected) {
                    bluetoothSocket.close()
                }
                while(!bluetoothSocket.isConnected) {
                    bluetoothSocket.connect()
                }
                if(bluetoothSocket.isConnected){
                    progressDialog.dismiss()
                    val intent = Intent(this, VehicleControlActivity::class.java)
                    intent.putExtra("Address", deviceAddress)
                    startActivity(intent)
                    bluetoothSocket.close()
                    count++
                }

            } catch (e: Exception) {
                bluetoothSocket.close()
                progressDialog.dismiss()
                runOnUiThread { Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show() }
            }

        } catch (e: IOException) {
            bluetoothSocket.close()
            progressDialog.dismiss()
            runOnUiThread { Toast.makeText(this, "$e", Toast.LENGTH_SHORT).show() }
        }

    }


    private fun showDialog(bluetoothDevice: BluetoothDevice) {

        progressDialog.setTitle("Connecting...")
        progressDialog.setMessage("Connecting to ${bluetoothDevice.name}")
        progressDialog.setCancelable(true)
        progressDialog.setOnCancelListener {
            bluetoothSocket.close()
            Toast.makeText(this, "Connection Cancelled!", Toast.LENGTH_SHORT).show()
        }
        progressDialog.show()
    }

@SuppressLint("MissingPermission")
@Composable
fun MainUI(
    bondedDevices:MutableList<BluetoothDevice>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
            .padding(5.dp)
    ) {

        Spacer(modifier = Modifier.height(10.dp))
        for(device in bondedDevices) {
            Spacer(modifier = Modifier.height(10.dp))
            PairedDevices(name = device.name, address = device.address)
        }

    }
}



@Composable
fun PairedDevices(name: String, address: String) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .background(color = Color(0x0C1b1b1b))
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xff1b1b1b)
            )
            .padding(horizontal = 5.dp)
            .height(70.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 15.dp))
        Button(
            modifier = Modifier.height(40.dp).width(100.dp),
            onClick = {
                device = bluetoothAdapter.getRemoteDevice(address)
                showDialog(device)
                Thread{connectToDevice(address)}.start()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff000000))
        ) {
            Text(text = "Connect")
        }
    }
}

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth Turned On :)", Toast.LENGTH_SHORT).show()
            } else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth Operation Cancelled!", Toast.LENGTH_SHORT).show()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        count--
        if(count < 1) {
            super.onBackPressed()
        }
        bluetoothSocket.close()

    }

    override fun onDestroy() {
        super.onDestroy()
        try{
            unregisterReceiver(DeviceConnectionReceiver())
        } catch (_: Exception) {

        }
    }
}