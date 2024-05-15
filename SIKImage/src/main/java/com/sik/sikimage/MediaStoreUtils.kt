package com.sik.sikimage

import android.Manifest
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.sik.sikcore.SIKCore
import com.sik.sikcore.permission.PermissionUtils

/**
 * 系统媒体存储工具类
 */
object MediaStoreUtils {
    /**
     * 拉取图片
     */
    fun fetchImages(
        sortKey: String = MediaStore.Images.Media.DATE_MODIFIED,
        sortType: String = "DESC"
    ): List<Uri> {
        val imageList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val cursor = SIKCore.getApplication().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "$sortKey $sortType"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageList.add(uri)
            }
        }
        return imageList
    }

    /**
     * 拉取视频
     */
    fun fetchVideos(
        sortKey: String = MediaStore.Video.Media.DATE_MODIFIED,
        sortType: String = "DESC"
    ): List<Uri> {
        val videoList = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME
        )

        val cursor = SIKCore.getApplication().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "$sortKey $sortType"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val uri =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                videoList.add(uri)
            }
        }
        return videoList
    }

}