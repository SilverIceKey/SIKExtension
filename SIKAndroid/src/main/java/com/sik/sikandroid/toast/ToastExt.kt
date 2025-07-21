package com.sik.sikandroid.toast

import android.content.Context
import android.widget.Toast

/**
 * Toast
 */
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}