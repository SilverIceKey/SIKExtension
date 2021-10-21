package com.sk.skextension

import java.util.*

public inline fun <T : Comparable<T>> Array<out T>.getString(): String {
    return Arrays.toString(this)
}