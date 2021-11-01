package com.sk.skextension

import java.util.*

fun <T : Comparable<T>> Array<out T>.getString(): String {
    return Arrays.toString(this)
}