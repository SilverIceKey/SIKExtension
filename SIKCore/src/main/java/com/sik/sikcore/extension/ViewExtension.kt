package com.sik.sikcore.extension

import android.view.View
import android.view.ViewTreeObserver

/**
 * Do after rendered
 * 在View渲染完之后执行的操作
 * @param task
 * @receiver
 */
fun View.doAfterRendered(task: () -> Unit = {}) {
    val onRenderedListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            this@doAfterRendered.viewTreeObserver.removeOnDrawListener(this)
            task()
        }
    }
    this.viewTreeObserver.addOnGlobalLayoutListener(onRenderedListener)
}