package com.sik.sikimage

import android.graphics.*
import android.os.Environment
import android.util.Base64
import java.io.*


/**
 * 图像帮助类
 */
object ImageConvertUtils {
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
    fun nv21ToBase64(nv21: ByteArray?, width: Int, height: Int): String? {
        return bitmapToBase64(nv21ToBitmap(nv21, width, height))
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
                val R = argb[index] and 0xFF0000 shr 16
                val G = argb[index] and 0x00FF00 shr 8
                val B = argb[index] and 0x0000FF
                val Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                val U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                val V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
                nv21[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.size - 2) {
                    nv21[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    nv21[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
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

}