package com.sik.sikcore.explain

import com.sik.sikcore.log.LogUtils
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.typeOf

/**
 * 属性类注解扫描
 */
object AnnotationScanner {
    /**
     * 属性-描述存储
     */
    private val fieldMap = mutableMapOf<String, String>()

    inline fun <reified T> KProperty1<*, *>.isTypeOf() = returnType == typeOf<T>()

    fun scan(configClass: KClass<*>) {
        // 获取 ScanConfig 注解
        val scanConfig = configClass.findAnnotation<ScanConfig>()
        LogUtils.getLogger(AnnotationScanner::class).i("${scanConfig?.classesToScan?.size}")
        scanConfig?.classesToScan?.forEach { clazz ->
            clazz.memberProperties.forEach { prop ->
                if (prop.isTypeOf<String>()) {
                    prop.isAccessible = true // 确保可以访问属性
                    val annotation = prop.javaField?.getAnnotation(LogInfo::class.java)
                    if (annotation != null) {
                        try {
                            val value = if (prop.parameters.isEmpty()) {
                                prop.getter.call()
                            } else {
                                prop.getter.call(clazz.objectInstance)
                            }
                            if (value is String) {
                                fieldMap[value] = annotation.description
                            }
                        } catch (e: Exception) {
                            // 处理调用getter时可能发生的异常
                            println("Error accessing property '${prop.name}': ${e.message}")
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据属性内容获取注解描述内容
     */
    fun getDescription(value: String): String? {
        return fieldMap[value]
    }
}
