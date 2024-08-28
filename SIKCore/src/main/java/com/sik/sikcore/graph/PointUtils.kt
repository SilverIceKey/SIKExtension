package com.sik.sikcore.graph

/**
 * 点位计算工具类
 */
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
        p1: Pair<Float, Float>,
        p2: Pair<Float, Float>,
        totalPoints: Int = 4
    ): List<Pair<Float, Float>> {
        require(totalPoints >= 4) { "Total points must be at least 4." }

        // 计算矩形的另外两个顶点
        val p3 = Pair(p1.first, p2.second) // (p1的x, p2的y)
        val p4 = Pair(p2.first, p1.second) // (p2的x, p1的y)

        // 确定 p1 为基准点，按顺时针顺序排列顶点
        val points = mutableListOf<Pair<Float, Float>>()
        points.add(p1)

        when {
            p1.first <= p2.first && p1.second <= p2.second||p1.first > p2.first && p1.second <= p2.second  -> { // p1左上，p2右下 或者 p1右上，p2左下
                points.add(p3)
                points.add(p2)
                points.add(p4)
            }
            else -> { // p1右下，p2左上 或者 p1左下，p2右上
                points.add(p4)
                points.add(p2)
                points.add(p3)
            }
        }

        // 如果需要补全点位，添加到主点列表中
        if (totalPoints > 4) {
            val extraPoints = totalPoints - 4

            // 在每条边上均匀分布额外的点
            val edgeMidpoints = listOf(
                calculateIntermediatePoints(points[0], points[1], extraPoints / 4),
                calculateIntermediatePoints(points[1], points[2], extraPoints / 4),
                calculateIntermediatePoints(points[2], points[3], extraPoints / 4),
                calculateIntermediatePoints(points[3], points[0], extraPoints / 4)
            )

            // 将计算出的中间点添加到主点列表中
            points += edgeMidpoints.flatten()
        }
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
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        count: Int
    ): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        for (i in 1..count) {
            val ratio = i.toFloat() / (count + 1)
            val x = start.first + ratio * (end.first - start.first)
            val y = start.second + ratio * (end.second - start.second)
            points.add(Pair(x, y))
        }
        return points
    }

}