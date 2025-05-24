package com.sik.sikcore.extension

/**
 * Array数组转[参数1,参数2]形式文本
 */
fun <T : Comparable<T>> Array<out T>.getString(): String {
    return this.contentToString()
}

/**
 * 列表转byte 用于modbus的二进制开关转换
 */
fun List<Boolean>.toPackedBytes(
    byteCount: Int = 2,
    bigEndian: Boolean = true  // 默认大端
): ByteArray {
    require(byteCount in 1..4) { "byteCount 只能在 1~4 字节之间" }

    // 限制最多 byteCount * 8 位
    val limitedBits = this.take(byteCount * 8)

    var packed = 0
    for (i in limitedBits.indices) {
        if (limitedBits[i]) {
            packed = packed or (1 shl i)
        }
    }

    return ByteArray(byteCount) { index ->
        val shift = if (bigEndian) (byteCount - 1 - index) * 8 else index * 8
        ((packed shr shift) and 0xFF).toByte()
    }
}

/**
 * 构造部分控制的状态和掩码数组（默认大端）
 * @param controlMap 控制的开关位 -> 开/关状态
 * @param byteCount 字节数（默认2字节）
 * @param bigEndian 是否大端模式（默认true）
 * @return Pair<状态ByteArray, 掩码ByteArray>
 */
fun Map<Int, Boolean>.toControlAndMaskBytes(
    byteCount: Int = 2,
    bigEndian: Boolean = true
): Pair<ByteArray, ByteArray> {
    require(byteCount in 1..4) { "byteCount 只能在 1~4 字节之间" }

    var state = 0
    var mask = 0
    for ((index, value) in this) {
        require(index in 0 until byteCount * 8) { "开关索引超出范围" }
        if (value) state = state or (1 shl index)
        mask = mask or (1 shl index)
    }

    fun toBytes(int: Int): ByteArray =
        ByteArray(byteCount) { i ->
            val shift = if (bigEndian) (byteCount - 1 - i) * 8 else i * 8
            ((int shr shift) and 0xFF).toByte()
        }

    return toBytes(state) to toBytes(mask)
}