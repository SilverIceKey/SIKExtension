package com.sik.sikcore.graph

import kotlin.math.sqrt

/**
 * 图像计算工具类
 */
object GraphCalcUtils {
    /**
     * 计算点 (px, py) 到线段 (x1, y1)-(x2, y2) 的最短距离
     */
    fun distancePointToSegment(
        px: Float,
        py: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Double {
        val dx = x2 - x1
        val dy = y2 - y1

        if (dx == 0f && dy == 0f) {
            // 线段的起点和终点重合，直接计算点到这个点的距离
            return sqrt(((px - x1) * (px - x1) + (py - y1) * (py - y1)).toDouble())
        }

        // 计算投影位置，找出点在这条线段上的投影点
        val t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)

        return when {
            t < 0 -> {
                // 投影点在线段之前，返回点到线段起点的距离
                sqrt(((px - x1) * (px - x1) + (py - y1) * (py - y1)).toDouble())
            }

            t > 1 -> {
                // 投影点在线段之后，返回点到线段终点的距离
                sqrt(((px - x2) * (px - x2) + (py - y2) * (py - y2)).toDouble())
            }

            else -> {
                // 投影点在线段上，计算点到投影点的距离
                val projX = x1 + t * dx
                val projY = y1 + t * dy
                sqrt(((px - projX) * (px - projX) + (py - projY) * (py - projY)).toDouble())
            }
        }
    }
}