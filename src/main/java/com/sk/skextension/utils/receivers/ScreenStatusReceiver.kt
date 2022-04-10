package com.sk.skextension.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sk.skextension.utils.eventbus.DefaultBusModel
import org.greenrobot.eventbus.EventBus

/**
 * 显示器状态监听
 */
class ScreenStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action
        if (action == Intent.ACTION_SCREEN_ON) {
            EventBus.getDefault()
                .post(DefaultBusModel(EVENTBUS_TYPE, SCREEN_ON))
        } else if (action == Intent.ACTION_SCREEN_OFF) {
            EventBus.getDefault()
                .post(DefaultBusModel(EVENTBUS_TYPE, SCREEN_OFF))
        }
    }

    companion object {
        /**
         * 事件总线类型
         */
        val EVENTBUS_TYPE: String = "ScreenStatusChange"

        /**
         * 屏幕打开
         */
        val SCREEN_ON: Int = 1

        /**
         * 屏幕关闭
         */
        val SCREEN_OFF: Int = 2

        /**
         * 注册屏幕开关接收器
         */
        fun registerReceiver(context: Context?) {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_SCREEN_ON)
            filter.addAction(Intent.ACTION_SCREEN_OFF)
            filter.addAction(Intent.ACTION_USER_PRESENT)
            context?.registerReceiver(ScreenStatusReceiver(), filter)
        }
    }
}