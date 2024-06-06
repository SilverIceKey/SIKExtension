package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF

/**
 * 图像裁剪工具类
 *
 */
object CropImageUtils {
    /**
     * 裁剪Bitmap
     * @param bitmap 要裁剪的Bitmap
     * @param cropRect 裁剪区域
     * @return 裁剪后的Bitmap
     */
    @JvmStatic
    fun cropBitmap(bitmap: Bitmap, cropRect: Rect): Bitmap {
        // 创建空白Bitmap
        val croppedBitmap = Bitmap.createBitmap(cropRect.width(), cropRect.height(), bitmap.config)

        // 创建Canvas，并在其上绘制裁剪区域的Bitmap
        val canvas = Canvas(croppedBitmap)
        val paint = Paint()
        val srcRect = Rect(0, 0, cropRect.width(), cropRect.height())
        canvas.drawBitmap(bitmap, cropRect, srcRect, paint)

        return croppedBitmap
    }

    /**
     * 从中心裁剪出正方形Bitmap
     * @param bitmap 要裁剪的Bitmap
     * @return 裁剪后的正方形Bitmap
     */
    @JvmStatic
    fun cropSquareBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newDimen = Math.min(width, height)

        val cropRect = Rect(
            (width - newDimen) / 2,
            (height - newDimen) / 2,
            (width + newDimen) / 2,
            (height + newDimen) / 2
        )
        return cropBitmap(bitmap, cropRect)
    }

    /**
     * 裁剪圆形Bitmap
     * @param bitmap 要裁剪的Bitmap
     * @return 裁剪后的圆形Bitmap
     */
    fun cropCircleBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        paint.isAntiAlias = true

        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    /**
     * 裁剪指定大小的圆形Bitmap
     * @param bitmap 要裁剪的Bitmap
     * @param diameter 圆的直径
     * @return 裁剪后的圆形Bitmap
     */
    fun cropCircleBitmap(bitmap: Bitmap, diameter: Int): Bitmap {
        val output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, diameter, diameter)
        val rectF = RectF(rect)

        paint.isAntiAlias = true

        // 画圆形区域
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.BLACK
        canvas.drawOval(rectF, paint)

        // 使用SRC_IN模式绘制Bitmap
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // 计算Bitmap的中心点，确保裁剪区域居中
        val left = (bitmap.width - diameter) / 2
        val top = (bitmap.height - diameter) / 2
        val srcRect = Rect(left, top, left + diameter, top + diameter)

        canvas.drawBitmap(bitmap, srcRect, rect, paint)

        return output
    }
}