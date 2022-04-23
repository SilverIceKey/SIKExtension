package com.sk.skextension.utils.reflex

/**
 * 反射工具类
 */
class ReflexUtils {
    companion object {
        val instance: ReflexUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ReflexUtils()
        }
    }

    lateinit var clazz: Class<*>

    /**
     * 优先调用，之后会使用同一个类
     */
    fun getClassInstance(className: String): ReflexUtils {
        clazz = Class.forName(className)
        return this
    }

    /**
     * 反射执行方法
     */
    fun invoke(
        classInstance:Any = clazz.newInstance(),
        methodName: String,
        vararg params: Any,
        onInvokeCallback: (Any?) -> Unit
    ): ReflexUtils {
        val declaredMethod = clazz.getDeclaredMethod(methodName)
        declaredMethod.isAccessible = true
        onInvokeCallback(declaredMethod.invoke(classInstance, params))
        return this
    }

    /**
     * 反射获取数据
     * srcObj 上级类
     */
    fun getData(fieldName: String, srcObj: Any, onGetCallback: (Any?) -> Unit): ReflexUtils {
        val declaredField = clazz.getDeclaredField(fieldName)
        declaredField.isAccessible = true
        onGetCallback(declaredField.get(srcObj))
        return this
    }

    /**
     * 反射设置数据
     * srcObj 上级类
     * targetObj 要替换的参数
     */
    fun setData(
        fieldName: String,
        srcObj: Any,
        targetObj: Any,
        onSetCallback: (Any?) -> Unit
    ): ReflexUtils {
        val declaredField = clazz.getDeclaredField(fieldName)
        declaredField.isAccessible = true
        onSetCallback(declaredField.set(srcObj, targetObj))
        return this
    }
}