package com.sik.sikcore.graph

object PointUtils {
    /**
     * 计算矩形的所有顶点和额外的点。
     *
     * @param p1 斜对角的一个点，使用 Pair 表示，x 坐标为 first，y 坐标为 second。
     * @param p2 斜对角的另一个点，使用 Pair 表示，x 坐标为 first，y 坐标为 second。
     * @param totalPoints 需要的总点数，至少为4，代表矩形的4个顶点。
     * @return 返回包含所有点的列表。
     */
    fun calculateRectanglePoints(
        p1: Pair<Int, Int>,
        p2: Pair<Int, Int>,
        totalPoints: Int = 4
    ): List<Pair<Int, Int>> {
        require(totalPoints >= 4) { "Total points must be at least 4." }

        // 计算矩形的另外两个顶点
        val p3 = Pair(p1.first, p2.second)
        val p4 = Pair(p2.first, p1.second)

        // 添加四个顶点到列表中
        val points = mutableListOf(p1, p2, p3, p4)

        if (totalPoints > 4) {
            val extraPoints = totalPoints - 4

            // 在每条边上均匀分布额外的点
            val edgeMidpoints = listOf(
                calculateIntermediatePoints(p1, p3, extraPoints / 4),
                calculateIntermediatePoints(p3, p2, extraPoints / 4),
                calculateIntermediatePoints(p2, p4, extraPoints / 4),
                calculateIntermediatePoints(p4, p1, extraPoints / 4)
            )

            // 将计算出的中间点添加到主点列表中
            edgeMidpoints.flatten().forEach { points.add(it) }
        }

        // 返回指定数量的点（不超过总数）
        return points.take(totalPoints)
    }

    /**
     * 计算两个点之间的中间点。
     *
     * @param start 起始点，使用 Pair 表示。
     * @param end 结束点，使用 Pair 表示。
     * @param count 需要计算的中间点数量。
     * @return 返回包含所有中间点的列表。
     */
    fun calculateIntermediatePoints(
        start: Pair<Int, Int>,
        end: Pair<Int, Int>,
        count: Int
    ): List<Pair<Int, Int>> {
        val points = mutableListOf<Pair<Int, Int>>()
        for (i in 1..count) {
            val ratio = i.toDouble() / (count + 1)
            val x = (start.first + ratio * (end.first - start.first)).toInt()
            val y = (start.second + ratio * (end.second - start.second)).toInt()
            points.add(Pair(x, y))
        }
        return points
    }
}