package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Environment
import android.util.Base64
import org.beyka.tiffbitmapfactory.TiffBitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer


/**
 * 图像转换工具类
 */
object ImageConvertUtils {
    /**
     *
     * Min base64size
     * 转换出来的bitmap大小最小限制使用quality控制<p>
     * 当为0时不限制，默认质量90
     */
    var minBase64Size: Float = 0f

    /**
     * Max base64size
     * 转换出来的bitmap大小最大限制使用quality控制<p>
     * 当为0时不限制，默认质量90
     */
    var maxBase64Size: Float = 0f

    /**
     * Current quality
     * 当前符合大小限制的质量
     */
    var currentQuality: Int = 90

    /**
     * bitmap转base64
     * */
    fun bitmapToBase64(bitmap: Bitmap?): String? {
        var result: String? = null
        var baos: ByteArrayOutputStream? = null
        try {
            if (bitmap != null) {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                baos.flush()
                baos.close()
                val bitmapBytes: ByteArray = baos.toByteArray()
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (baos != null) {
                    baos.flush()
                    baos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }

    /**
     * base64转bitmap
     * */
    fun base64ToBitmap(base64: String?): Bitmap? {
        val decode = Base64.decode(base64, Base64.DEFAULT)
        val bitmap: Bitmap? = try {
            BitmapFactory.decodeByteArray(decode, 0, decode.size)
        } catch (e: Exception) {
            null
        }
        return bitmap
    }

    /**
     * base64转bitmap argb8888
     * */
    fun base64ToBitmapARGB8888(base64: String?): Bitmap? {
        val decode = Base64.decode(base64, Base64.DEFAULT)
        val opt = BitmapFactory.Options()
        opt.inDither = true
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap: Bitmap? = try {
            BitmapFactory.decodeByteArray(decode, 0, decode.size, opt)
        } catch (e: java.lang.Exception) {
            null
        }
        return bitmap
    }

    /**
     * nv21转base64
     */
    fun nv21ToBase64(nv21: ByteArray, width: Int, height: Int): String {
        return nv21ToBase64Jpeg(nv21, width, height)
    }

    /**
     * nv21转base64并且压缩大小至100*100px
     */
    fun nv21ToBase64Compress(nv21: ByteArray?, width: Int, height: Int): String? {
        val tmpBitmap = nv21ToBitmap(nv21, width, height)
        val scale = 100f / tmpBitmap!!.height
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val resultBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, width, height, matrix, false)
        if (!tmpBitmap.isRecycled) {
            tmpBitmap.recycle()
        }
        return bitmapToBase64(resultBitmap)
    }

    /**
     * bitmap转base64并且压缩大小至100*100px
     */
    fun BitmapToBase64Compress(tmpBitmap: Bitmap, width: Int, height: Int): String? {
        val scale = 100f / tmpBitmap.height
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val resultBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, width, height, matrix, false)
        if (!tmpBitmap.isRecycled) {
            tmpBitmap.recycle()
        }
        return bitmapToBase64(resultBitmap)
    }

    /**
     *
     *  nv21转bitmap
     * */
    fun nv21ToBitmap(nv21: ByteArray?, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val image = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val stream = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, width, height), 80, stream)
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * Bitmap转化为ARGB数据，再转化为NV21数据
     *
     * @param src    传入ARGB_8888的Bitmap
     * @param width  NV21图像的宽度
     * @param height NV21图像的高度
     * @return nv21数据
     */
    fun bitmapToNv21(src: Bitmap?, width: Int, height: Int): ByteArray? {
        return if (src != null && src.width >= width && src.height >= height) {
            val argb = IntArray(width * height)
            src.getPixels(argb, 0, width, 0, 0, width, height)
            argbToNv21(argb, width, height)
        } else {
            null
        }
    }

    /**
     * ARGB数据转化为NV21数据
     *
     * @param argb   argb数据
     * @param width  宽度
     * @param height 高度
     * @return nv21数据
     */
    private fun argbToNv21(argb: IntArray, width: Int, height: Int): ByteArray? {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var index = 0
        val nv21 = ByteArray(width * height * 3 / 2)
        for (j in 0 until height) {
            for (i in 0 until width) {
                val r = argb[index] and 0xFF0000 shr 16
                val g = argb[index] and 0x00FF00 shr 8
                val b = argb[index] and 0x0000FF
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                val v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                nv21[yIndex++] = (y.coerceIn(0, 255)).toByte()
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.size - 2) {
                    nv21[uvIndex++] = (v.coerceIn(0, 255)).toByte()
                    nv21[uvIndex++] = (u.coerceIn(0, 255)).toByte()
                }
                ++index
            }
        }
        return nv21
    }

    /**
     * 保存bitmap
     *
     * @param bitmap
     * @return
     */
    fun saveFile(bitmap: Bitmap): File? {
        val tmpBitmapPath =
            "${Environment.getExternalStorageDirectory()}${File.separator}tmpImages${File.separator}"
        if (!File(tmpBitmapPath).exists()) {
            File(tmpBitmapPath).mkdirs()
        }
        val tmpImage = File(tmpBitmapPath, System.currentTimeMillis().toString() + ".jpg")
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tmpImage)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            if (out != null) {
                out.flush()
                out.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tmpImage
    }

    /**
     * 文件转base64字符串
     *
     * @param file
     * @return
     */
    fun fileToBase64(file: File?): String? {
        var base64: String? = null
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(file)
            val bytes = ByteArray(inputStream.available())
            val length: Int = inputStream.read(bytes)
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return base64
    }

    /**
     * nv21转Base64
     *
     * @param nv21
     * @param width
     * @param height
     * @param quality
     * @return
     */
    fun nv21ToBase64Jpeg(
        nv21: ByteArray,
        width: Int,
        height: Int,
        minSize: Float = minBase64Size,
        maxSize: Float = maxBase64Size,
        quality: Int = currentQuality
    ): String {
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val jpegStream = ByteArrayOutputStream()
        val jpegByteArray =
            compressToJpeg(yuvImage, width, height, minSize, maxSize, quality, jpegStream)
        // Convert to Base64 string
        val base64Jpeg = Base64.encodeToString(jpegByteArray, Base64.DEFAULT)
        jpegStream.close()
        return base64Jpeg
    }

    /**
     * CameraX的Image转Bitmap
     *
     * @param image
     * @param rotationDegrees
     * @return
     */
    fun imageToBitmap(image: Image, rotationDegrees: Int): Bitmap {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

        // 旋转Bitmap以纠正方向
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        return bitmap
    }

    /**
     * nv21压缩jpeg到指定大小
     *
     * @param yuvImage
     * @param jpegStream
     * @return
     */
    private fun compressToJpeg(
        yuvImage: YuvImage, width: Int, height: Int,
        minSize: Float,
        maxSize: Float, quality: Int, jpegStream: ByteArrayOutputStream
    ): ByteArray {
        jpegStream.reset()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), quality, jpegStream)
        return if (minSize != 0f && jpegStream.toByteArray().size / 1024f < minSize) {
            compressToJpeg(yuvImage, width, height, minSize, maxSize, quality + 1, jpegStream)
        } else if (maxSize != 0f && jpegStream.toByteArray().size / 1024f > maxSize) {
            compressToJpeg(yuvImage, width, height, minSize, maxSize, quality - 1, jpegStream)
        } else {
            currentQuality = quality
            jpegStream.toByteArray()
        }
    }

    /**
     * 将图片文件转换为指定格式的图片文件
     *
     * @param inputFile 输入文件
     * @param outputFile 输出文件
     * @param format 目标格式 (CompressFormat.JPEG, CompressFormat.PNG, etc.)
     * @return 转换后的文件
     */
    fun convertImageFormat(
        inputFile: File,
        outputFile: File,
        format: Bitmap.CompressFormat
    ): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath)
            val out = FileOutputStream(outputFile)
            bitmap.compress(format, 100, out)
            out.flush()
            out.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将RAW文件转换为Bitmap
     *
     * @param inputFile 输入文件 (RAW文件)
     * @param width 图像宽度
     * @param height 图像高度
     * @return Bitmap
     */
    fun rawToBitmap(inputFile: File, width: Int, height: Int): Bitmap? {
        return try {
            val inputStream = FileInputStream(inputFile)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer))
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将Bitmap保存为RAW文件
     *
     * @param bitmap Bitmap对象
     * @param outputFile 输出文件 (RAW文件)
     * @return 保存后的文件
     */
    fun bitmapToRaw(bitmap: Bitmap, outputFile: File): File? {
        return try {
            val buffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(buffer)
            val rawBytes = buffer.array()

            val out = FileOutputStream(outputFile)
            out.write(rawBytes)
            out.flush()
            out.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将JPG文件转换为PNG文件
     *
     * @param inputFile 输入JPG文件
     * @param outputFile 输出PNG文件
     * @return 转换后的文件
     */
    fun jpgToPng(inputFile: File, outputFile: File): File? {
        return convertImageFormat(inputFile, outputFile, Bitmap.CompressFormat.PNG)
    }

    /**
     * 将PNG文件转换为JPG文件
     *
     * @param inputFile 输入PNG文件
     * @param outputFile 输出JPG文件
     * @return 转换后的文件
     */
    fun pngToJpg(inputFile: File, outputFile: File): File? {
        return convertImageFormat(inputFile, outputFile, Bitmap.CompressFormat.JPEG)
    }

    /**
     * 将TIF/TIFF文件转换为JPG或PNG
     *
     * @param inputFile 输入TIF/TIFF文件
     * @param outputFile 输出JPG/PNG文件
     * @param format 目标格式 (CompressFormat.JPEG, CompressFormat.PNG)
     * @return 转换后的文件
     */
    fun tifToImage(inputFile: File, outputFile: File, format: Bitmap.CompressFormat): File? {
        // 注意：TIF/TIFF格式在原生的Android API中不支持直接处理
        // 需要使用第三方库如TiffBitmapFactory来处理
        return try {
            val bitmap = TiffBitmapFactory.decodePath(inputFile.absolutePath)
            val out = FileOutputStream(outputFile)
            bitmap.compress(format, 100, out)
            out.flush()
            out.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 读取TIF/TIFF为Bitmap
     */
    fun tifToBitmap(filePath: String): Bitmap? {
        // 注意：TIF/TIFF格式在原生的Android API中不支持直接处理
        // 需要使用第三方库如TiffBitmapFactory来处理
        return try {
            TiffBitmapFactory.decodePath(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}