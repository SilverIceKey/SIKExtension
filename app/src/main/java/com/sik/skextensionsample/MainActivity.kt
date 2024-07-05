package com.sik.skextensionsample

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.sik.sikcore.explain.LogInfo
import com.sik.sikcore.permission.PermissionUtils

@LogInfo(description = "进入主界面")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val triggerPermissionRequest = remember { mutableStateOf(false) }
//            PermissionUtils.RequestPermissions(
//                permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                onPermissionsResult = {
//
//                },
//                triggerPermissionRequest = triggerPermissionRequest
//            )
            PermissionUtils.RequestManageExternalStorage(triggerPermissionRequest) {

            }
            LaunchedEffect(key1 = Unit) {
                triggerPermissionRequest.value = true
            }
        }
    }
}