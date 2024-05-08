package com.sik.skextensionsample

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.sik.sikcore.data.GlobalDataTempStore
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.permission.PermissionUtils
import java.security.Permission
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private val logger = LogUtils.getLogger(MainActivity::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logger.i("onCreate")
        logger.i("开始请求授权")
        PermissionUtils.requestAllFilesAccessPermission()
        PermissionUtils.checkAndRequestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) { granted -> logger.i("是否授权成功${granted}") }
    }
}