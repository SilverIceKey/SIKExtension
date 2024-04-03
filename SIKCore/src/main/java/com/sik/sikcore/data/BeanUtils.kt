package com.sik.sikcore.data

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
        var fatherClass: Class<*> = T::class.java
        while (fatherClass != Object::class.java) {
            fatherClass.declaredFields.forEach {
                val sourceFiledsHasField =
                    sourceClass.declaredFields.any { sourceField -> sourceField.name == it.name }
                if (sourceFiledsHasField) {
                    val sourceField = sourceClass.getDeclaredField(it.name)
                    sourceField.isAccessible = true
                    val targetField = fatherClass.getDeclaredField(it.name)
                    targetField.isAccessible = true
                    targetField[target] = sourceField[source]
                }
            }
            fatherClass = fatherClass.superclass
        }
        return target as T
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
        var fatherClass: Class<*> = target.javaClass
        while (fatherClass != Object::class.java) {
            fatherClass.declaredFields.forEach {
                val sourceFiledsHasField =
                    sourceClass.declaredFields.any { sourceField -> sourceField.name == it.name }
                if (sourceFiledsHasField) {
                    val sourceField = sourceClass.getDeclaredField(it.name)
                    sourceField.isAccessible = true
                    val targetField = fatherClass.getDeclaredField(it.name)
                    targetField.isAccessible = true
                    targetField[target] = sourceField[source]
                }
            }
            fatherClass = fatherClass.superclass
        }
    }
}