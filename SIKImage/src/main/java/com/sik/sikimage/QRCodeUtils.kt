package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.nio.charset.Charset

/**
 * 二维码工具
 */
object QRCodeUtils {

    /**
     * 二维码解码为想要的字符格式
     */
    fun readQRCodeString(bitmap: Bitmap, charset: Charset = Charset.defaultCharset()): String {
        return readQRCode(bitmap)?.rawBytes?.toString(charset) ?: ""
    }

    /**
     * 根据bitmap读取二维码
     */
    fun readQRCode(bitmap: Bitmap): Result? {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val luminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))
        return QRCodeReader().decode(binaryBitmap)
    }

    /**
     * 快捷创建二维码bitmap
     */
    @JvmOverloads
    fun createQRCode(
        info: String,
        size: Int,
        color: Int = -1,
        logo: Bitmap? = null,
        withInfo: Boolean = false
    ): Bitmap {
        val hint = HashMap<EncodeHintType, Any>()
        hint[EncodeHintType.MARGIN] = 0
        hint[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        return bitMatrixToBitmap(
            QRCodeWriter().encode(
                String(
                    info.toByteArray(Charsets.UTF_8),
                    Charsets.ISO_8859_1
                ), BarcodeFormat.QR_CODE, size, size, hint
            ), color, logo, withInfo, info
        )
    }

    /**
     * bitmap添加logo和信息文本
     */
    private fun bitMatrixToBitmap(
        bitMatrix: BitMatrix, color: Int, logo: Bitmap?,
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
        return addLogoAndInfoToQRCode(bitmap, logo, withInfo, info)
    }

    /**
     * 添加logo和信息文本到二维码图片上
     */
    private fun addLogoAndInfoToQRCode(
        src: Bitmap,
        logo: Bitmap?,
        withInfo: Boolean,
        info: String
    ): Bitmap {
        val srcWidth = src.width
        val srcHeight = src.height
        var logoWidth = logo?.width ?: 0
        var logoHeight = logo?.height ?: 0

        val resultWidth: Int
        val resultHeight: Int
        val bitmap: Bitmap

        if (withInfo) {
            val paint = Paint()
            paint.textSize = srcHeight / 2 * 0.17f
            paint.color = Color.BLACK
            paint.isAntiAlias = true
            val rect = Rect()
            paint.getTextBounds(info, 0, info.length, rect)
            resultWidth = Math.max(srcWidth, rect.width())
            resultHeight = srcHeight + rect.height() + 20 // 调整文本和间距的高度
            bitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, (resultWidth - srcWidth) / 2f, 0f, null)
            canvas.drawText(
                info,
                (resultWidth - rect.width()) / 2f,
                (srcHeight + rect.height() + 10).toFloat(),
                paint
            )
        } else {
            resultWidth = srcWidth
            resultHeight = srcHeight
            bitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawBitmap(src, 0f, 0f, null)
        }

        if (logo != null) {
            try {
                val canvas = Canvas(bitmap)
                val matrix = Matrix()
                val max = Math.max(logoWidth, logoHeight)
                var scale = 0f
                if (max > srcWidth / 2.2f) {
                    scale = srcWidth / 2.2f / logoWidth
                    logoWidth = (logoWidth * scale).toInt()
                    logoHeight = (logoHeight * scale).toInt()
                }
                matrix.postScale(scale, scale)
                val centerLeftPosition = (srcWidth - logoWidth) / 2f
                val centerTopPosition = (srcHeight - logoHeight) / 2f
                matrix.postTranslate(centerLeftPosition, centerTopPosition)
                canvas.drawBitmap(logo, matrix, null)
            } catch (e: Exception) {
                e.printStackTrace()
                return src
            }
        }

        return bitmap
    }
}
