package com.sik.sikcore.extension

/**
 * Array数组转[参数1,参数2]形式文本
 */
fun <T : Comparable<T>> Array<out T>.getString(): String {
    return this.contentToString()
}

/**
 * kotlin list转mutablelist
 */
fun <T : Comparable<T>> List<T>.toMutableList(): MutableList<T> {
    val resultMutableList = mutableListOf<T>()
    this.stream().forEach {
        resultMutableList.add(it)
    }
    return resultMutableList
}