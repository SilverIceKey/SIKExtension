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
 * 仅复制名称相同且类型“可赋值”的字段，支持 Object 和 List 的转换。
 */
object BeanUtils {

    // 原始类型 <-> 包装类型对照
    private val PRIMITIVE_TO_WRAPPER = mapOf(
        java.lang.Boolean.TYPE to java.lang.Boolean::class.java,
        java.lang.Byte.TYPE to java.lang.Byte::class.java,
        java.lang.Character.TYPE to java.lang.Character::class.java,
        java.lang.Short.TYPE to java.lang.Short::class.java,
        java.lang.Integer.TYPE to java.lang.Integer::class.java,
        java.lang.Long.TYPE to java.lang.Long::class.java,
        java.lang.Float.TYPE to java.lang.Float::class.java,
        java.lang.Double.TYPE to java.lang.Double::class.java,
        java.lang.Void.TYPE to java.lang.Void::class.java
    )

    private fun boxed(c: Class<*>) = PRIMITIVE_TO_WRAPPER[c] ?: c

    /** 允许：子类 → 父类；原始/包装互换。若 value 为 null，我们后续也不会写入目标字段。 */
    fun isTypeCompatible(sourceType: Class<*>, targetType: Class<*>, valueNonNull: Boolean): Boolean {
        val src = boxed(sourceType)
        val tgt = boxed(targetType)
        return if (valueNonNull) {
            // 非空值：目标类型可接收源类型即可
            tgt.isAssignableFrom(src)
        } else {
            // 空值：理论上不写，返回值不影响
            !targetType.isPrimitive
        }
    }

    /**
     * 修改后的 copyData 方法：使用反射进行数据复制（target 已实例化）
     * 只在源值非空且类型兼容时赋值；跳过 static/final 字段。
     */
    @JvmStatic
    inline fun <reified T> copyData(source: Any, target: Any): T {
        val sourceClass = source.javaClass
        var targetClass: Class<*> = target.javaClass   // ✅ 修正：遍历实际 target 类型层次
        while (targetClass != Any::class.java) {
            targetClass.declaredFields.forEach { targetField ->
                if (Modifier.isStatic(targetField.modifiers) || Modifier.isFinal(targetField.modifiers)) return@forEach
                val sourceField = getField(sourceClass, targetField.name) ?: return@forEach
                if (Modifier.isStatic(sourceField.modifiers)) return@forEach

                // 读取源值
                val srcValue = runCatching {
                    sourceField.isAccessible = true
                    sourceField.get(source)
                }.getOrNull().also { sourceField.isAccessible = false }

                // 只在非空且类型兼容时写入；null 一律不写
                if (srcValue != null && isTypeCompatible(sourceField.type, targetField.type, true)) {
                    runCatching {
                        targetField.isAccessible = true
                        targetField.set(target, srcValue) // 自动处理包装 -> 原始的拆箱
                    }.also { targetField.isAccessible = false }
                }
            }
            targetClass = targetClass.superclass
        }
        @Suppress("UNCHECKED_CAST")
        return target as T
    }

    /**
     * 修改后的 copyData 重载方法：直接将 source 数据复制到 target 中
     */
    @JvmStatic
    fun copyData(source: Any, target: Any) {
        val sourceClass = source.javaClass
        var targetClass: Class<*> = target.javaClass
        while (targetClass != Any::class.java) {
            targetClass.declaredFields.forEach { targetField ->
                if (Modifier.isStatic(targetField.modifiers) || Modifier.isFinal(targetField.modifiers)) return@forEach
                val sourceField = getField(sourceClass, targetField.name) ?: return@forEach
                if (Modifier.isStatic(sourceField.modifiers)) return@forEach

                val srcValue = runCatching {
                    sourceField.isAccessible = true
                    sourceField.get(source)
                }.getOrNull().also { sourceField.isAccessible = false }

                if (srcValue != null && isTypeCompatible(sourceField.type, targetField.type, true)) {
                    runCatching {
                        targetField.isAccessible = true
                        targetField.set(target, srcValue)
                    }.also { targetField.isAccessible = false }
                }
            }
            targetClass = targetClass.superclass
        }
    }

    /**
     * 创建并复制：只在源值非空且类型兼容时写入；跳过 static/final
     */
    @JvmStatic
    inline fun <reified T> copyProperties(source: Any?, targetClass: Class<T>): T? {
        if (source == null) return null
        val sourceClass = source.javaClass
        val ctor = targetClass.getDeclaredConstructor().apply { isAccessible = true }
        val target = ctor.newInstance()

        var temp: Class<*> = targetClass
        while (temp != Any::class.java) {
            temp.declaredFields.forEach { targetField ->
                if (Modifier.isStatic(targetField.modifiers) || Modifier.isFinal(targetField.modifiers)) return@forEach
                val sourceField = getField(sourceClass, targetField.name) ?: return@forEach
                if (Modifier.isStatic(sourceField.modifiers)) return@forEach

                val srcValue = runCatching {
                    sourceField.isAccessible = true
                    sourceField.get(source)
                }.getOrNull().also { sourceField.isAccessible = false }

                if (srcValue != null && isTypeCompatible(sourceField.type, targetField.type, true)) {
                    runCatching {
                        targetField.isAccessible = true
                        targetField.set(target, srcValue)
                    }.also { targetField.isAccessible = false }
                }
            }
            temp = temp.superclass
        }
        @Suppress("UNCHECKED_CAST")
        return target as T
    }

    /**
     * 列表复制
     */
    @JvmStatic
    inline fun <reified T> copyList(sourceList: List<Any>?, targetClass: Class<T>): List<T> {
        if (sourceList.isNullOrEmpty()) return emptyList()
        val targetList = ArrayList<T>(sourceList.size)
        for (source in sourceList) {
            val copyResult = copyProperties(source, targetClass)
            if (copyResult != null) targetList.add(copyResult)
        }
        return targetList
    }

    /**
     * 获取字段，支持父类中的字段
     */
    @JvmStatic
    fun getField(clazz: Class<*>, fieldName: String): Field? {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            try {
                return current.getDeclaredField(fieldName)
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            }
        }
        return null
    }

    /**
     * 将任意 source 对象转换为目标 data class T
     * 仅复制“主构造参数名匹配且类型可赋值”的属性；支持默认值与可空性
     */
    inline fun <reified T : Any> convert(source: Any): T {
        val targetClass: KClass<T> = T::class
        val constructor = targetClass.primaryConstructor
            ?: throw IllegalArgumentException("Class ${targetClass.simpleName} 必须有主构造函数")

        val sourceProps = source::class.memberProperties.associateBy { it.name }
        val args = mutableMapOf<KParameter, Any?>()

        constructor.parameters.forEach { param ->
            val name = param.name ?: return@forEach
            val sourceProp = sourceProps[name] ?: return@forEach

            val targetType = param.type.jvmErasure.java
            val sourceType = sourceProp.returnType.jvmErasure.java

            // 取值
            sourceProp.isAccessible = true
            val value = sourceProp.getter.call(source)

            // 类型兼容（含原始/包装）
            val compatible = if (value == null) {
                // null 仅在参数可空时可接受；否则让默认值或抛错
                !targetType.isPrimitive && param.type.isMarkedNullable
            } else {
                isTypeCompatible(sourceType, targetType, true)
            }

            if (!compatible) return@forEach

            // 赋参策略：
            // - 非空值：直接传
            // - 空值：仅当 param 可空才传 null；否则：
            //   - 若 param 有默认值（isOptional）→ 不传，让默认值生效
            //   - 若无默认值且非空 → 不传，后面统一检查抛错（更清晰提示）
            if (value != null) {
                args[param] = value
            } else if (param.type.isMarkedNullable) {
                args[param] = null
            } else if (param.isOptional) {
                // 跳过，走默认
            } else {
                // 留给后面缺参检查
            }
        }

        // 必填非空但未提供的参数，明确报错（比 callBy 的异常更易读）
        val missing = constructor.parameters.filter {
            !it.isOptional && !it.type.isMarkedNullable && !args.containsKey(it)
        }
        if (missing.isNotEmpty()) {
            val names = missing.joinToString(", ") { it.name ?: "unknown" }
            throw IllegalArgumentException("convert<${targetClass.simpleName}> 缺少必填非空参数：$names")
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
