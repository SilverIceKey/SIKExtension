package com.sik.skextensionsample

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.sik.sikcore.device.DeviceUtil
import com.sik.sikcore.shell.ShellUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.text).setOnClickListener {
            useFunction()
        }
    }

    private fun useFunction(){
//        findViewById<TextView>(R.id.text).text = DeviceUtil.getSN()
    }
}