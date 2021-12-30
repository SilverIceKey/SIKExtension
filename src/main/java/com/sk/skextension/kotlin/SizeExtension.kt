package com.wangjing.cashier.base

import android.content.res.Resources
import androidx.compose.ui.unit.Dp

/**
 * 屏幕适配
 */
val Number.composeWidthDp
    get() = Resources.getSystem().displayMetrics.run {
        val dp = widthPixels / density
        Dp((toFloat() * dp / 1366))
    }
val Number.composeHeightDp
    get() = Resources.getSystem().displayMetrics.run {
        val dp = widthPixels / density
        Dp((toFloat() * dp / 768))
    }