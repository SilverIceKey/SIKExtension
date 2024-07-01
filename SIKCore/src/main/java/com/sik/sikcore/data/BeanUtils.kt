package com.sik.sikcore.data

import java.lang.reflect.Field

/**
 * Bean utils
 * bean工具类
 * @constructor Create empty Bean utils
 */
object BeanUtils {
    /**
     * Copy data
     * 使用反射进行相同字段的数据复制
     * @param T
     * @param source
     * @param target
     * @return
     */
    @JvmStatic
    inline fun <reified T> copyData(source: Any, target: Any): T {
        val sourceClass = source.javaClass
        var targetClass: Class<*> = T::class.java
        while (targetClass != Any::class.java) {
            targetClass.declaredFields.forEach { targetField ->
                val sourceField = getField(sourceClass, targetField.name)
                if (sourceField != null) {
                    try {
                        sourceField.isAccessible = true
                        targetField.isAccessible = true
                        targetField.set(target, sourceField.get(source))
                    } catch (e: Exception) {
                        // 可以记录日志或处理异常
                    } finally {
                        sourceField.isAccessible = false
                        targetField.isAccessible = false
                    }
                }
            }
            targetClass = targetClass.superclass
        }
        return target as T
    }

    /**
     * 获取字段，支持父类字段
     * @param clazz
     * @param fieldName
     * @return
     */
    @JvmStatic
    fun getField(clazz: Class<*>, fieldName: String): Field? {
        var currentClass: Class<*>? = clazz
        while (currentClass != null && currentClass != Any::class.java) {
            try {
                return currentClass.getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }
        return null
    }

    /**
     * Copy data
     * 使用反射进行相同字段的数据复制
     * @param source
     * @param target
     */
    @JvmStatic
    fun copyData(source: Any, target: Any) {
        val sourceClass = source.javaClass
        var targetClass: Class<*> = target.javaClass
        while (targetClass != Any::class.java) {
            targetClass.declaredFields.forEach { targetField ->
                val sourceField = getField(sourceClass, targetField.name)
                if (sourceField != null) {
                    try {
                        sourceField.isAccessible = true
                        targetField.isAccessible = true
                        targetField.set(target, sourceField.get(source))
                    } catch (e: Exception) {
                        // 可以记录日志或处理异常
                    } finally {
                        sourceField.isAccessible = false
                        targetField.isAccessible = false
                    }
                }
            }
            targetClass = targetClass.superclass
        }
    }
}
