package com.sk.skextension.utils.explain

import java.lang.reflect.Field

object ExplainUtils {
    /**
     * 根据属性获取类里的介绍说明
     */
    fun <T> getExplainValueWithKey(clazz: Class<T>, key: String): String {
        for (declaredField in clazz.declaredFields) {
            if (declaredField.name == key) {
                val anno = declaredField.getAnnotation(Explain::class.java)
                if (anno != null) {
                    return anno.explainValue
                }
            }
        }
        return "未知"
    }

    /**
     * 获取类里所有介绍说明
     */
    fun <T> getExplainValues(clazz: Class<T>): Map<String, String> {
        val explainValues = mutableMapOf<String, String>()
        for (declaredField in clazz.declaredFields) {
            explainValues.put(
                declaredField.name,
                declaredField.getAnnotation(Explain::class.java)?.explainValue ?: "未知"
            )
        }
        return explainValues
    }
}