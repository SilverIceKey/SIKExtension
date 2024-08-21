package com.sik.sikcore.graph

import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region

/**
 * 判断这个path是否包含另一个Path
 */
fun Path.contains(path: Path): Boolean {
    val pathRegion = Region()
    val bounds = RectF()
    computeBounds(bounds, true)
    pathRegion.setPath(
        this,
        Region(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt())
    )
    // 创建另一个 Region 来表示矩形
    val targetPathRegion = Region()
    val targetPathBound = RectF()
    path.computeBounds(targetPathBound, true)
    targetPathRegion[targetPathBound.left.toInt(), targetPathBound.top.toInt(), targetPathBound.right.toInt()] =
        targetPathBound.bottom.toInt()
    // 判断矩形是否被 Path 包含
    return pathRegion.op(targetPathRegion, Region.Op.INTERSECT)
}