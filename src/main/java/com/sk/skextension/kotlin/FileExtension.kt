package com.sk.skextension.kotlin

import java.io.File
import java.io.FileOutputStream
import kotlin.internal.*


/**
 * Constructs a new FileOutputStream of this file and returns it as a result.
 */
public fun File.emptyOutputStream(): FileOutputStream {
    return FileOutputStream(this,false)
}
