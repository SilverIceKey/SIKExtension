package com.sik.sikcore.view

import android.view.View

/**
 * 防抖监听器
 */
class DebouncedClickListener(private val listener: View.OnClickListener, private val debounceMillis: Long = 500L) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceMillis) {
            lastClickTime = currentTime
            listener.onClick(v)
        }
    }
}
