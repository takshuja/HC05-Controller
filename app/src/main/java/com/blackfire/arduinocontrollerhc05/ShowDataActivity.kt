package com.blackfire.arduinocontrollerhc05

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.blackfire.arduinocontrollerhc05.ui.theme.ArduinoControllerHC05Theme
import java.io.FileInputStream
import java.io.IOException

class ShowDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArduinoControllerHC05Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    dataScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArduinoControllerHC05Theme {
        Greeting("Android")
    }
}


private fun getData(): String {
    var fileInputStream: FileInputStream? = null
    try {
        fileInputStream = FileInputStream("HCO5_data.txt")
        var i = -1
        val buffer = StringBuffer()
        while(fileInputStream.read().also { i = it } != -1) {
            buffer.append(i.toChar())
        }
        return buffer.toString()
    }catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        if(fileInputStream != null) {
            try{
                fileInputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    return ""
}


@Composable
fun dataScreen() {
    Column(modifier = Modifier.fillMaxSize())
    {
        Text(text = getData())
    }
}