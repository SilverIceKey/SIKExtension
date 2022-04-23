package com.sk.skextension.kotlin

import java.io.File
import java.io.FileOutputStream


/**
 * Constructs a new FileOutputStream of this file and returns it as a result.
 */
fun File.emptyOutputStream(): FileOutputStream {
    return FileOutputStream(this,false)
}
