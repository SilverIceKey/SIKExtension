package com.sik.sikcore.data

import java.beans.Introspector
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

/**
 * Bean 工具类
 * 包含修改后的 copyData 方法和类型转换功能，
 * 仅复制名称和类型完全一致的字段，支持 Object 和 List 的转换。
 */
object BeanUtils {

    /**
     * 修改后的 copyData 方法：使用反射进行数据复制，仅复制字段名称和类型完全一致的字段
     * 目标类型必须提供无参构造函数（或通过 T::class.java 指定），适用于普通 Java Bean 复制
     *
     * @param T 目标类型
     * @param source 源对象
     * @param target 目标对象
     * @return 复制后的目标对象
     */
    @JvmStatic
    inline fun <reified T> copyData(source: Any, target: Any): T {
        val sourceClass = source.javaClass
        var targetClass: Class<*> = T::class.java
        while (targetClass != Any::class.java) {
            targetClass.declaredFields.forEach { targetField ->
                if (Modifier.isStatic(targetField.modifiers) || Modifier.isFinal(targetField.modifiers)) return@forEach
                val sourceField = getField(sourceClass, targetField.name)
                // 仅当源字段存在且类型一致时复制
                if (sourceField != null && !Modifier.isStatic(sourceField.modifiers) && sourceField.type == targetField.type) {
                    try {
                        sourceField.isAccessible = true
                        targetField.isAccessible = true
                        targetField.set(target, sourceField.get(source))
                    } catch (e: Exception) {
                        // 可记录日志或处理异常
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
     * 修改后的 copyData 重载方法：直接将 source 数据复制到 target 中，不返回目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     */
    @JvmStatic
    fun copyData(source: Any, target: Any) {
        val sourceClass = source.javaClass
        var targetClass: Class<*> = target.javaClass
        while (targetClass != Any::class.java) {
            targetClass.declaredFields.forEach { targetField ->
                if (Modifier.isStatic(targetField.modifiers) || Modifier.isFinal(targetField.modifiers)) return@forEach
                val sourceField = getField(sourceClass, targetField.name)
                if (sourceField != null && !Modifier.isStatic(sourceField.modifiers) && sourceField.type == targetField.type) {
                    try {
                        sourceField.isAccessible = true
                        targetField.isAccessible = true
                        targetField.set(target, sourceField.get(source))
                    } catch (e: Exception) {
                        // 可记录日志或处理异常
                    } finally {
                        sourceField.isAccessible = false
                        targetField.isAccessible = false
                    }
                }
            }
            targetClass = targetClass.superclass
        }
    }

    /**
     * 属性复制：参考 Spring BeanUtils，根据 getter/setter 进行复制，可忽略空值和指定字段
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreNull 是否忽略 null 值
     * @param ignoreProperties 需要忽略的字段名称
     */
    @JvmStatic
    fun copyProperties(source: Any, target: Any, ignoreNull: Boolean = false, vararg ignoreProperties: String) {
        val ignoreList = ignoreProperties.toSet()
        val sourceInfo = Introspector.getBeanInfo(source.javaClass)
        val targetInfo = Introspector.getBeanInfo(target.javaClass)
        val sourcePdMap = sourceInfo.propertyDescriptors.associateBy { it.name }
        targetInfo.propertyDescriptors.forEach { pd ->
            if (pd.name == "class" || pd.name in ignoreList) return@forEach
            val writeMethod = pd.writeMethod ?: return@forEach
            val sourcePd = sourcePdMap[pd.name] ?: return@forEach
            val readMethod = sourcePd.readMethod ?: return@forEach
            if (!pd.propertyType.isAssignableFrom(sourcePd.propertyType)) return@forEach
            readMethod.isAccessible = true
            val value = readMethod.invoke(source)
            if (ignoreNull && value == null) return@forEach
            writeMethod.isAccessible = true
            try {
                writeMethod.invoke(target, value)
            } catch (_: Exception) {
                // ignore invoke error
            }
        }
    }

    /**
     * 批量属性复制：为列表中的每个对象创建新的目标实例并复制属性
     */
    @JvmStatic
    inline fun <reified T : Any> copyPropertiesList(sourceList: List<*>, ignoreNull: Boolean = false, vararg ignoreProperties: String): List<T> {
        return sourceList.filterNotNull().map { source ->
            val target = T::class.java.getDeclaredConstructor().newInstance()
            copyProperties(source, target, ignoreNull, *ignoreProperties)
            target
        }
    }

    /**
     * 获取字段，支持父类中的字段
     *
     * @param clazz 要查找的类
     * @param fieldName 字段名称
     * @return 找到的 Field 对象，未找到返回 null
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
     * 类型转换方法：利用 Kotlin 反射实现从 A 类型转换到 B 类型，
     * 仅复制名称和类型完全一致的字段，支持 Kotlin data class。
     * 目标类型通过主构造函数创建实例，不再要求无参构造函数。
     *
     * @param T 目标类型（必须有主构造函数）
     * @param source 源对象
     * @return 转换后的目标对象
     * @throws IllegalArgumentException 如果目标类没有主构造函数
     */
    inline fun <reified T : Any> convert(source: Any): T {
        val targetKClass: KClass<T> = T::class
        // 获取目标类的主构造函数
        val constructor = targetKClass.primaryConstructor
            ?: throw IllegalArgumentException("Class ${targetKClass.simpleName} 必须有主构造函数")
        // 根据构造参数名称和类型，在源对象中查找对应字段的值
        val args = mutableMapOf<KParameter, Any?>()
        constructor.parameters.forEach { param ->
            val name = param.name
            if (name != null) {
                val sourceField = getField(source.javaClass, name)
                if (sourceField != null) {
                    // 仅当源字段类型与目标构造参数类型一致时才复制
                    val targetParamType = (param.type.classifier as? KClass<*>)?.java
                    if (sourceField.type == targetParamType) {
                        try {
                            sourceField.isAccessible = true
                            args[param] = sourceField.get(source)
                        } catch (e: Exception) {
                            // 可记录日志或处理异常
                        } finally {
                            sourceField.isAccessible = false
                        }
                    }
                }
            }
        }
        // 调用主构造函数创建实例，如果部分参数有默认值，则自动使用默认值
        return constructor.callBy(args)
    }

    /**
     * List 类型转换
     * 对 List 中的每个源对象调用 convert 方法进行转换
     *
     * @param T 目标类型
     * @param sourceList 源对象列表
     * @return 转换后的目标对象列表
     * @throws IllegalArgumentException 如果 List 中存在 null 值
     */
    inline fun <reified T : Any> convertList(sourceList: List<*>): List<T> {
        return sourceList.map { source ->
            if (source == null) {
                throw IllegalArgumentException("List 中不允许 null 值")
            }
            convert<T>(source)
        }
    }
}
