package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * 条形码工具
 */
object BarCodeUtils {
    /**
     * 推测的条形码格式
     */
    private val barCodeFormatList = mutableListOf<BarcodeFormat>(
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODE_128,
        BarcodeFormat.EAN_8,
        BarcodeFormat.EAN_13,
    )

    /**
     * 默认条形码格式
     */
    private var defaultBarCodeFormat = BarcodeFormat.CODE_128

    /**
     * 设置条形码格式
     */
    fun setBarCodeFormat(barcodeFormat: BarcodeFormat) {
        this.defaultBarCodeFormat = barcodeFormat
    }

    /*
    * 根据bitmap读取条形码
    */
    fun readBarCode(bitmap: Bitmap): String {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val luminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))
        return MultiFormatReader().apply {
            setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to barCodeFormatList))
        }.decode(binaryBitmap).text
    }

    /**
     * 快捷创建条形码bitmap
     */
    @JvmOverloads
    fun createBarCode(
        info: String,
        width: Int,
        height: Int,
        color: Int = -1,
        withInfo: Boolean = false
    ): Bitmap {
        val hint = HashMap<EncodeHintType, Any>()
        hint[EncodeHintType.MARGIN] = 0
        hint[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        return bitMatrixToBitmap(
            MultiFormatWriter().encode(
                String(
                    info.toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1
                ), defaultBarCodeFormat, width, height, hint
            ), color, withInfo, info
        )
    }

    /**
     * bitmap添加信息文本
     */
    private fun bitMatrixToBitmap(
        bitMatrix: BitMatrix, color: Int,
        withInfo: Boolean, info: String
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix.get(x, y)) {
                    if (color == -1 || color == Color.parseColor("#ffffff")) Color.BLACK else color
                } else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return addInfoToBarcode(bitmap, withInfo, info)
    }

    /**
     * 添加信息文本到条形码图片上
     */
    private fun addInfoToBarcode(src: Bitmap, withInfo: Boolean, info: String): Bitmap {
        if (!withInfo) {
            return src
        }
        val srcWidth = src.width
        val srcHeight = src.height
        val paint = Paint()
        paint.textSize = srcHeight * 0.17f
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        val rect = Rect()
        paint.getTextBounds(info, 0, info.length, rect)
        val resultWidth = srcWidth.coerceAtLeast(rect.width())
        val resultHeight = srcHeight + rect.height() + 20 // 调整文本和间距的高度
        val bitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(src, (resultWidth - srcWidth) / 2f, 0f, null)
        canvas.drawText(info, (resultWidth - rect.width()) / 2f, (srcHeight + rect.height() + 10).toFloat(), paint)
        canvas.save()
        canvas.restore()
        return bitmap
    }
}
