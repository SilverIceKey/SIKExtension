package com.sik.sikcore.device

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.sik.sikcore.SIKCore

/**
 * 屏幕工具
 */
object ScreenUtils {

    /**
     * 获取屏幕宽度（像素）
     */
    fun getScreenWidth(context: Context = SIKCore.getApplication()): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    /**
     * 获取屏幕高度（像素）
     */
    fun getScreenHeight(context: Context = SIKCore.getApplication()): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    /**
     * dp转px
     */
    fun dpToPx(context: Context = SIKCore.getApplication(), dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    /**
     * px转dp
     */
    fun pxToDp(context: Context = SIKCore.getApplication(), px: Float): Int {
        val density = context.resources.displayMetrics.density
        return (px / density + 0.5f).toInt()
    }

    /**
     * 获取屏幕密度
     */
    fun getScreenDensity(context: Context = SIKCore.getApplication()): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * 获取屏幕密度DPI
     */
    fun getScreenDensityDpi(context: Context = SIKCore.getApplication()): Int {
        return context.resources.displayMetrics.densityDpi
    }
}
