package com.sik.sikcore.data

/**
 * Global data temp store
 * 全局参数临时存储池
 * @constructor Create empty Global data temp store
 */
class GlobalDataTempStore {
    /**
     * Data store
     * 数据存储池
     */
    private val dataStore: HashMap<String, Any?> = hashMapOf()

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
     * Save data
     * 保存数据
     * @param key
     * @param value
     */
    fun saveData(key: String, value: Any) {
        dataStore[key] = value
    }

    /**
     * Get data
     * 获取数据，默认取出后删除数据
     * @param key
     * @param isDeleteAfterGet
     */
    @JvmOverloads
    fun getData(key: String, isDeleteAfterGet: Boolean = true): Any? {
        val data = dataStore[key]
        if (isDeleteAfterGet) {
            dataStore[key] = null
        }
        return data
    }
}