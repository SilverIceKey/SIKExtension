package com.sik.skextensionsample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun useFunction(){
//        findViewById<TextView>(R.id.text).text = DeviceUtil.getSN()
    }
}