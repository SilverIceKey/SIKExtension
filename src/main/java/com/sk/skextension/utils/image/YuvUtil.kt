package com.sk.skextension.utils.image
import kotlin.math.abs

object YuvUtil {
    init {
        System.loadLibrary("yuvutil")
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
        val pixeCount = (width * height).toLong()
        //采集步长，因为没有必要每个像素点都采集，可以跨一段采集一个，减少计算负担，必须大于等于1。
        val step = 10
        //data.length - allCount * 1.5f的目的是判断图像格式是不是YUV420格式，只有是这种格式才相等
        //因为int整形与float浮点直接比较会出问题，所以这么比
        var cameraLight = 0L
        if (abs(data.size - pixeCount * 1.5f) < 0.00001f) {
            run {
                var i = 0
                while (i < pixeCount) {

                    //如果直接加是不行的，因为data[i]记录的是色值并不是数值，byte的范围是+127到—128，
                    // 而亮度FFFFFF是11111111是-127，所以这里需要先转为无符号unsigned long参考Byte.toUnsignedLong()
                    pixelLightCount += (data[i].toLong()).and(0xffL)
                    i += step
                }
            }
            //平均亮度 一般80左右以及以上为下限,180为上限
            cameraLight = pixelLightCount / (pixeCount / step)

        }
        return cameraLight
    }
}