package com.sk.skextension.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class LockScreenReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action
        if (action == Intent.ACTION_SCREEN_ON) {

        } else if (action == Intent.ACTION_SCREEN_OFF) {

        }
    }
    companion object{
        fun registerReceiver(context: Context?){
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_USER_PRESENT)
            context?.registerReceiver(LockScreenReceiver(), filter)
        }
    }
}