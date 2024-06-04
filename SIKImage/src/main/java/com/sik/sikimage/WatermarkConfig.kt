package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.Color

/**
 * 水印配置
 */
class WatermarkConfig {
    /**
     * 水印颜色
     */
    var color: Int = Color.parseColor("#000000")

    /**
     * 水印字体大小
     */
    var textSize: Float = 16f

    /**
     * 水印文本
     */
    var text: MutableList<String> = mutableListOf()

    /**
     * 水印是否加粗
     */
    var isBold: Boolean = false

    /**
     * 水印旋转角度
     */
    var angle: Int = 0

    /**
     * 水印图像
     */
    var watermarkBitmap: Bitmap? = null

    /**
     * 是否是文本水印
     */
    var isTextWatermark: Boolean = true

    /**
     * 是否循环水印
     */
    var isRepeat: Boolean = true

    /**
     * 水印位置
     */
    var watermarkX: Int = 0
    var watermarkY: Int = 0

    /**
     * 水印之间的间隔，默认为0
     */
    var spacing: Int = 30
}