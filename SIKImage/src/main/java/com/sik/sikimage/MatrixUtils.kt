package com.sik.sikimage

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * 矩阵操作
 */
object MatrixUtils {
    /**
     * 缩放，缩放之后位置不移动
     *  @param scaleX 缩放x轴 默认0
     *  @param scaleY 缩放y轴 默认0
     */
    @JvmStatic
    fun scale(matrix: Matrix, scale: Float) {
        //取出当前矩阵的坐标
        val tmpMatrix = matrix.value()
        val x = tmpMatrix[Matrix.MTRANS_X]
        val y = tmpMatrix[Matrix.MTRANS_Y]
        //矩阵缩放
        matrix.setScale(scale, scale)
        //移动到之前取出坐标的位置
        matrix.postTranslate(x, y)
    }

    /**
     * 缩放，缩放之后位置移动
     *  @param scaleX 缩放x轴 默认0
     *  @param scaleY 缩放y轴 默认0
     */
    @JvmStatic
    fun scale(matrix: Matrix, scale: Float, scaleX: Float = 0f, scaleY: Float = 0f) {
        //矩阵缩放
        matrix.setScale(scale, scale, scaleX, scaleY)
    }

    /**
     * bitmap旋转角度
     *
     * @param bitmap 源bitmap
     * @param rotate 旋转角度
     * @param sourceRecycler 是否回收源bitmap
     * @return 旋转后的bitmap
     */
    @JvmStatic
    @JvmOverloads
    fun bitmapRotate(bitmap: Bitmap, rotate: Float, sourceRecycler: Boolean = false): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotate)
        val resultBitmap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (bitmap.isRecycled && sourceRecycler) {
            bitmap.recycle()
        }
        return resultBitmap
    }
}

/**
 * 获取阵列的值
 *
 */
fun Matrix.value(): FloatArray {
    val matrixValues = FloatArray(9)
    this.getValues(matrixValues)
    return matrixValues
}