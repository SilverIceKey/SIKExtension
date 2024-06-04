package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect

/**
 * 给bitmap添加水印
 */
fun Bitmap.addWatermark(config: WatermarkConfig): Bitmap {
    val resultBitmap = Bitmap.createBitmap(this.width, this.height, this.config)
    val canvas = Canvas(resultBitmap)
    val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    watermarkPaint.color = config.color
    watermarkPaint.textSize = config.textSize
    canvas.drawBitmap(this, 0f, 0f, watermarkPaint)
    // 准备绘制水印
    if (config.isTextWatermark) {
        // 文本水印
        watermarkPaint.textSize = config.textSize.toFloat()
        watermarkPaint.isFakeBoldText = config.isBold
        // 计算文本位置
        val textBounds = Rect()
        config.text.forEach { watermarkPaint.getTextBounds(it, 0, it.length, textBounds) }
        // 绘制文本水印
        if (config.isRepeat) {
            // 重复绘制文本水印
            val horizontalSpacing = textBounds.width() + config.textSize + config.spacing
            val verticalSpacing = textBounds.height() + config.textSize + config.spacing
            for (y in config.watermarkY until this.height step verticalSpacing.toInt()) {
                for (x in config.watermarkX until this.width step horizontalSpacing.toInt()) {
                    config.text.forEach { text ->
                        canvas.drawText(text, x.toFloat(), y.toFloat(), watermarkPaint)
                    }
                }
            }
        } else {
            // 仅绘制一次文本水印
            val x = config.watermarkX.toFloat()
            val y = config.watermarkY.toFloat()
            config.text.forEach { text ->
                canvas.drawText(text, x, y, watermarkPaint)
            }
        }
    } else {
        // 图像水印
        val watermark = config.watermarkBitmap ?: return resultBitmap
        val matrix = Matrix()
        matrix.postRotate(config.angle.toFloat())

        if (config.isRepeat) {
            // 重复绘制图像水印
            val horizontalSpacing = watermark.width + config.spacing
            val verticalSpacing = watermark.height + config.spacing
            for (y in config.watermarkY until this.height step verticalSpacing) {
                for (x in config.watermarkX until this.width step horizontalSpacing) {
                    canvas.drawBitmap(watermark, matrix, watermarkPaint)
                }
            }
        } else {
            // 仅绘制一次图像水印
            matrix.postTranslate(config.watermarkX.toFloat(), config.watermarkY.toFloat())
            canvas.drawBitmap(watermark, matrix, watermarkPaint)
        }
    }
    return resultBitmap
}