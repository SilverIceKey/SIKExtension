package com.sik.sikcore.bit

/**
 * Bit type utils
 * 位类型工具类
 * @constructor Create empty Bit type utils
 */
object BitTypeUtils {
    /**
     * Has type
     * 目标类型是否存在
     * @param originType
     * @param targetType
     * @return
     */
    fun hasType(originType: Int, targetType: Int): Boolean {
        return (originType and targetType) != 0
    }

    /**
     * Add type
     * 添加类型
     * @param originType
     * @param targetType
     * @return
     */
    fun addType(originType: Int, targetType: Int): Int {
        return originType or targetType
    }

    /**
     * Delete type
     * 删除类型
     * @param originType
     * @param targetType
     * @return
     */
    fun deleteType(originType: Int, targetType: Int): Int {
        return originType and targetType.inv()
    }
}