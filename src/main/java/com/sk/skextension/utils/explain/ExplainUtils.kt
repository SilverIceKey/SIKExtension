package com.sk.skextension.utils.explain

import com.google.gson.JsonObject

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
     * 获取class的介绍说明
     */
    fun <T> getClassDescription(clazz: Class<T>): String {
        var explain: Explain = clazz.getAnnotation(Explain::class.java)
        return explain.explainValue
    }

    /**
     * 获取class标题
     */
    fun <T> getClassTitle(clazz: Class<T>): String {
        var explain: Title = clazz.getAnnotation(Title::class.java)
        return explain.titleValue
    }

    /**
     * 获取必要属性
     */
    fun <T> getRequiredField(clazz: Class<T>):String{
        val requiredFields = arrayListOf<String>()
        for (declaredField in clazz.declaredFields) {
            if (declaredField.getAnnotation(Required::class.java)!=null){
                requiredFields.add(declaredField.name)
            }
        }
        return requiredFields.toString()
    }

    /**
     * 获取类里所有介绍说明
     */
    fun <T> getExplainValues(clazz: Class<T>): Map<String, String> {
        val explainValues = mutableMapOf<String, String>()
        for (declaredField in clazz.declaredFields) {
            explainValues[declaredField.name] = declaredField.getAnnotation(Explain::class.java)?.explainValue ?: "未知"
        }
        return explainValues
    }
    /**
     * 获取类里所有介绍说明
     */
    fun <T> getExplainValuesToJson(clazz: Class<T>): JsonObject {
        val properties = JsonObject()
        for (declaredField in clazz.declaredFields) {
            val fieldDescription = JsonObject()
            fieldDescription.addProperty("type",declaredField.type.simpleName)
            fieldDescription.addProperty("description",declaredField.getAnnotation(Explain::class.java)?.explainValue ?: "未知")
            properties.add(declaredField.name,fieldDescription)
        }
        return properties
    }
}