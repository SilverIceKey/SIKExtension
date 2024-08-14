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

/**
 * 加载tif
 */
fun loadTif(imageView: ImageView, tifPath: String) {
    imageView.loadTif(tifPath)
}

/**
 * 加载tiff
 */
fun loadTiff(imageView: ImageView, tiffPath: String) {
    imageView.loadTif(tiffPath)
}