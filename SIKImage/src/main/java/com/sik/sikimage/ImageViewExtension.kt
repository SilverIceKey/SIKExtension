package com.sik.sikimage

import android.widget.ImageView

/**
 * 加载tif
 */
fun ImageView.loadTif(tifPath: String) {
    this.loadTiff(tifPath)
}

/**
 * 加载tiff
 */
fun ImageView.loadTiff(tiffPath: String) {
    this.setImageBitmap(ImageConvertUtils.tifToBitmap(tiffPath))
}