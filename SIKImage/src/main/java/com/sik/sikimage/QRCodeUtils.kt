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
import com.google.zxing.NotFoundException
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
     * 二维码解码为字符串或十六进制字符串
     *
     * @param bitmap 二维码图像
     * @param charset 字符集，默认为 ISO-8859-1
     * @param toHex 是否转换为十六进制字符串
     * @return 解码后的字符串或十六进制字符串
     */
    fun readQRCodeString(
        bitmap: Bitmap,
        charset: Charset = Charsets.ISO_8859_1,
        toHex: Boolean = false
    ): String {
        val text = readQRCode(bitmap, charset)?.text ?: ""
        return if (toHex) {
            text.toByteArray(charset).toHexString()
        } else {
            text
        }
    }

    /**
     * 二维码解码为字节数组
     *
     * @param bitmap 二维码图像
     * @param charset 字符集，默认为 ISO-8859-1
     * @return 解码后的字节数组
     */
    fun readQRCodeRawBytes(
        bitmap: Bitmap,
        charset: Charset = Charsets.ISO_8859_1
    ): ByteArray {
        return readQRCode(bitmap, charset)?.rawBytes ?: ByteArray(0)
    }

    /**
     * 根据 bitmap 读取二维码
     *
     * @param bitmap 二维码图像
     * @param charset 字符集，默认为 ISO-8859-1
     * @return 解码结果，可能为 null
     */
    fun readQRCode(
        bitmap: Bitmap,
        charset: Charset = Charsets.ISO_8859_1
    ): Result? {
        val pixels = IntArray(bitmap.width * bitmap.height).apply {
            bitmap.getPixels(this, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        }
        val luminanceSource = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))
        val hints = mapOf(
            DecodeHintType.CHARACTER_SET to charset.name()
        )
        return try {
            QRCodeReader().decode(binaryBitmap, hints)
        } catch (e: NotFoundException) {
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建二维码 bitmap
     *
     * @param info 要编码的信息
     * @param size 二维码的宽高
     * @param color 前景色，默认为黑色
     * @param logo 可选的 logo 图像
     * @param withInfo 是否在二维码下方添加信息文本
     * @return 生成的二维码 Bitmap
     */
    @JvmOverloads
    fun createQRCode(
        info: String,
        size: Int,
        color: Int = Color.BLACK,
        logo: Bitmap? = null,
        withInfo: Boolean = false
    ): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 0,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.CHARACTER_SET to "ISO-8859-1"
        )
        val encodedInfo = info.toByteArray(Charsets.ISO_8859_1).toString(Charsets.ISO_8859_1)
        val bitMatrix = QRCodeWriter().encode(encodedInfo, BarcodeFormat.QR_CODE, size, size, hints)
        return bitMatrixToBitmap(bitMatrix, color, logo, withInfo, info)
    }

    /**
     * 将 BitMatrix 转换为 Bitmap，并添加 logo 和信息文本
     *
     * @param bitMatrix 二维码的 BitMatrix
     * @param color 前景色
     * @param logo 可选的 logo 图像
     * @param withInfo 是否添加信息文本
     * @param info 信息文本内容
     * @return 生成的二维码 Bitmap
     */
    private fun bitMatrixToBitmap(
        bitMatrix: BitMatrix,
        color: Int,
        logo: Bitmap?,
        withInfo: Boolean,
        info: String
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix.get(x, y)) color else Color.WHITE
            }
        }

        val qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }

        return addLogoAndInfoToQRCode(qrBitmap, logo, withInfo, info)
    }

    /**
     * 在二维码上添加 logo 和信息文本
     *
     * @param src 源二维码 Bitmap
     * @param logo logo 图像，可为 null
     * @param withInfo 是否添加信息文本
     * @param info 信息文本内容
     * @return 处理后的二维码 Bitmap
     */
    private fun addLogoAndInfoToQRCode(
        src: Bitmap,
        logo: Bitmap?,
        withInfo: Boolean,
        info: String
    ): Bitmap {
        val srcWidth = src.width
        val srcHeight = src.height
        val canvas = Canvas(src)
        var finalBitmap = src

        // 添加 logo
        logo?.let {
            val scaleFactor = calculateScaleFactor(srcWidth, it.width, it.height)
            val scaledLogo =
                Bitmap.createScaledBitmap(it, it.width * scaleFactor, it.height * scaleFactor, true)
            val logoX = (srcWidth - scaledLogo.width) / 2f
            val logoY = (srcHeight - scaledLogo.height) / 2f
            canvas.drawBitmap(scaledLogo, logoX, logoY, null)
        }

        // 添加信息文本
        if (withInfo) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = srcHeight * 0.05f
                color = Color.BLACK
                textAlign = Paint.Align.CENTER
            }
            val textHeight = getTextHeight(paint, info)
            val totalHeight = srcHeight + textHeight + 20
            finalBitmap =
                Bitmap.createBitmap(srcWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val finalCanvas = Canvas(finalBitmap)
            finalCanvas.drawBitmap(src, 0f, 0f, null)
            finalCanvas.drawText(info, srcWidth / 2f, srcHeight + textHeight + 10f, paint)
        }

        return finalBitmap
    }

    /**
     * 计算 logo 的缩放因子，确保 logo 不超过二维码的1/2.2大小
     *
     * @param qrWidth 二维码宽度
     * @param logoWidth logo 原始宽度
     * @param logoHeight logo 原始高度
     * @return 缩放因子
     */
    private fun calculateScaleFactor(qrWidth: Int, logoWidth: Int, logoHeight: Int): Int {
        val maxLogoSize = qrWidth / 2.2f
        return if (Math.max(logoWidth, logoHeight) > maxLogoSize) {
            (maxLogoSize / Math.max(logoWidth, logoHeight)).toInt()
        } else {
            1
        }
    }

    /**
     * 获取文本高度
     *
     * @param paint Paint 对象
     * @param text 文本内容
     * @return 文本高度
     */
    private fun getTextHeight(paint: Paint, text: String): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return bounds.height()
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @return 十六进制字符串
     */
    private fun ByteArray.toHexString(): String = joinToString("") { "%02X".format(it) }
}
