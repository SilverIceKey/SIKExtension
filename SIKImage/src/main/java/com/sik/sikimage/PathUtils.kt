package com.sik.sikimage

import android.graphics.Path

/**
 * 根据点判断是直线、四边形、三角形并绘制路径
 */
object PathUtils {
    /**
     * 获取路径
     */
    fun getPath(points:FloatArray):Path{
        val defaultPath = Path()
        defaultPath.moveTo(points[0],points[1])
        for (i in 2..points.size-2 step 2){
            defaultPath.lineTo(points[i],points[i+1])
        }
        if (points.size in 3..8 &&points.size%2==0){
            defaultPath.close()
        }
        return defaultPath
    }
}