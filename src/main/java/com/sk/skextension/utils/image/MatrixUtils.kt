package com.sk.skextension.utils.image

import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.values

/**
 * 矩阵操作
 */
object MatrixUtils {
    /**
     * 缩放，缩放之后位置不移动
     */
    fun scale(matrix: Matrix, scale: Float) {
        //取出当前矩阵的坐标
        val tmpMatrix = matrix.values()
        val x = tmpMatrix[Matrix.MTRANS_X]
        val y = tmpMatrix[Matrix.MTRANS_Y]
        //矩阵缩放
        matrix.setScale(scale, scale)
        //移动到之前取出坐标的位置
        matrix.postTranslate(x, y)
    }
}