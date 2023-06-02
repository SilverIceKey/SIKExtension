package com.sik.sikimage
import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.math.abs

/**
 * yuv工具类
 */
object ImageUtil {
    init {
        System.loadLibrary("SIKImage")
    }


    /**
     * 获取画面平均亮度
     * 平均亮度 一般80左右以及以上为下限,180为上限
     */
    fun getCameraPreviewLight(previewWidth: Int, previewHeight: Int, data: ByteArray): Long {
        val width: Int = previewWidth
        val height: Int = previewHeight
        //像素点的总亮度
        var pixelLightCount = 0L
        //像素点的总数
        val pixelCount = (width * height).toLong()
        //采集步长，因为没有必要每个像素点都采集，可以跨一段采集一个，减少计算负担，必须大于等于1。
        val step = 10
        //data.length - allCount * 1.5f的目的是判断图像格式是不是YUV420格式，只有是这种格式才相等
        //因为int整形与float浮点直接比较会出问题，所以这么比
        var cameraLight = 0L
        if (abs(data.size - pixelCount * 1.5f) < 0.00001f) {
            run {
                var i = 0
                while (i < pixelCount) {

                    //如果直接加是不行的，因为data[i]记录的是色值并不是数值，byte的范围是+127到—128，
                    // 而亮度FFFFFF是11111111是-127，所以这里需要先转为无符号unsigned long参考Byte.toUnsignedLong()
                    pixelLightCount += (data[i].toLong()).and(0xffL)
                    i += step
                }
            }
            //平均亮度 一般80左右以及以上为下限,180为上限
            cameraLight = pixelLightCount / (pixelCount / step)

        }
        return cameraLight
    }


    /**
     * nv21旋转角度
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    fun rotateNV21(input: ByteArray, width: Int, height: Int, rotation: Int): ByteArray {
        val output = ByteArray(input.size)
        val swap = rotation == 90 || rotation == 270
        val yflip = rotation == 90 || rotation == 180
        val xflip = rotation == 270 || rotation == 180
        for (x in 0 until width) {
            for (y in 0 until height) {
                var xo = x
                var yo = y
                var w = width
                var h = height
                var xi = xo
                var yi = yo
                if (swap) {
                    xi = w * yo / h
                    yi = h * xo / w
                }
                if (yflip) {
                    yi = h - yi - 1
                }
                if (xflip) {
                    xi = w - xi - 1
                }
                output[w * yo + xo] = input[w * yi + xi]
                val fs = w * h
                val qs = fs shr 2
                xi = xi shr 1
                yi = yi shr 1
                xo = xo shr 1
                yo = yo shr 1
                w = w shr 1
                h = h shr 1
                // adjust for interleave here
                val ui = fs + (w * yi + xi) * 2
                val uo = fs + (w * yo + xo) * 2
                // and here
                val vi = ui + 1
                val vo = uo + 1
                output[uo] = input[ui]
                output[vo] = input[vi]
            }
        }
        return output
    }

    /**
     * 等比缩放图片
     */
    fun zoomImg(bm: Bitmap, Scale: Float): Bitmap? {
        // 获得图片的宽高
        val width = bm.width
        val height = bm.height
        // 取得想要缩放的matrix参数
        val matrix = Matrix()
        matrix.postScale(Scale, Scale)
        // 得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
    }
}