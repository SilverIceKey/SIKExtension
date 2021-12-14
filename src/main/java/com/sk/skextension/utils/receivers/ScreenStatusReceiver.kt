package com.sk.skextension.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sk.skextension.utils.eventbus.BusModel
import com.sk.skextension.utils.net.mqtt.EMQXHelper
import org.greenrobot.eventbus.EventBus

/**
 * 显示器状态监听
 */
class ScreenStatusReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action
        if (action == Intent.ACTION_SCREEN_ON) {
            EventBus.getDefault().post(BusModel(EMQXHelper.EVENTBUS_TYPE,EMQXHelper.SCREEN_ON))
        } else if (action == Intent.ACTION_SCREEN_OFF) {

        }
    }
    companion object{
        fun registerReceiver(context: Context?){
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_USER_PRESENT)
            context?.registerReceiver(ScreenStatusReceiver(), filter)
        }
    }
}