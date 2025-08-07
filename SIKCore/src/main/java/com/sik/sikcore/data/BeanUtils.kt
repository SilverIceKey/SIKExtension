package com.sik.sikcore.data

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

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
     * 修改后的 copyData 方法：使用反射进行数据复制，仅复制字段名称和类型完全一致的字段
     * 目标类型必须提供无参构造函数（或通过 T::class.java 指定），适用于普通 Java Bean 复制
     *
     * @param T 目标类型
     * @param source 源对象
     * @param targetClass 目标对象类
     * @return 复制后的目标对象
     */
    @JvmStatic
    inline fun <reified T> copyProperties(source: Any?, targetClass: Class<T>): T? {
        if (source == null) {
            return null
        }
        val sourceClass = source.javaClass
        var tempTargetClass: Class<*> = targetClass
        val ctor = tempTargetClass.getDeclaredConstructor().apply { isAccessible = true }
        val target = ctor.newInstance()
        while (tempTargetClass != Any::class.java) {
            tempTargetClass.declaredFields.forEach { targetField ->
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
            tempTargetClass = tempTargetClass.superclass
        }
        return target as T
    }

    /**
     * 修改后的 copyData 方法：使用反射进行数据复制，仅复制字段名称和类型完全一致的字段
     * 目标类型必须提供无参构造函数（或通过 T::class.java 指定），适用于普通 Java Bean 复制
     *
     * @param T 目标类型
     * @param sourceList 源对象
     * @param targetClass 目标对象类
     * @return 复制后的目标对象列表
     */
    @JvmStatic
    inline fun <reified T> copyList(sourceList: List<Any>?, targetClass: Class<T>): List<T> {
        if (sourceList == null) {
            return listOf()
        }
        val targetList = mutableListOf<T>()
        for (source in sourceList) {
            val copyResult = copyProperties(source, targetClass)
            copyResult?.let {
                targetList.add(it)
            }
        }
        return targetList
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
     * 将任意 source 对象转换为目标 data class T
     * 只复制“主构造参数名和类型可赋值”的属性；支持默认值
     */
    inline fun <reified T : Any> convert(source: Any): T {
        val targetClass: KClass<T> = T::class
        val constructor = targetClass.primaryConstructor
            ?: throw IllegalArgumentException("Class ${targetClass.simpleName} 必须有主构造函数")
        // 拿 source 所有可读属性
        val sourceProps = source::class.memberProperties.associateBy { it.name }
        val args = mutableMapOf<KParameter, Any?>()
        constructor.parameters.forEach { param ->
            val name = param.name
                ?: return@forEach
            val sourceProp = sourceProps[name]
            if (sourceProp != null) {
                // 判断可赋值：targetParamType 可接受 sourceProp.returnType
                val targetType = param.type.jvmErasure
                val sourceType = sourceProp.returnType.jvmErasure
                if (targetType.java.isAssignableFrom(sourceType.java)) {
                    sourceProp.isAccessible = true
                    args[param] = sourceProp.getter.call(source)
                }
            }
            // 不存在于 args 的参数，如果不是 optional，就留给 callBy 去报错／走默认
        }
        return constructor.callBy(args)
    }

    /**
     * 对 List<Any> 做批量转换；如果遇到 null 抛错误
     */
    inline fun <reified T : Any> convertList(sourceList: List<*>): List<T> {
        return sourceList.mapIndexed { idx, source ->
            source ?: throw IllegalArgumentException("List index $idx 不能为 null")
            convert<T>(source)
        }
    }
}
