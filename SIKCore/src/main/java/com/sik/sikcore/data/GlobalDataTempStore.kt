package com.sik.sikcore.data

/**
 * Global data temp store
 * 全局参数临时存储池
 * @constructor Create empty Global data temp store
 */
class GlobalDataTempStore private constructor() {

    companion object {
        private val INSTANCE by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            GlobalDataTempStore()
        }

        @JvmStatic
        fun getInstance(): GlobalDataTempStore {
            return INSTANCE
        }
    }

    /**
     * 安全的保存数据
     * @param key
     * @param value
     */
    fun saveDataSafely(key: String, value: Any?): Boolean {
        return if (value != null) {
            saveData(key, value)
        } else {
            false
        }
    }


    /**
     * Save data
     * 保存数据
     * @param key
     * @param value
     */
    external fun saveData(key: String, value: Any):Boolean

    /**
     * Get data
     * 获取数据，默认取出后删除数据
     * @param key
     * @param isDeleteAfterGet
     */
    @JvmOverloads
    external fun getData(key: String, isDeleteAfterGet: Boolean = true): Any?

    /**
     * 是否有数据
     */
    external fun hasData(key: String): Boolean
}
