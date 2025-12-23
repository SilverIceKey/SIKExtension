package com.sik.skextensionsample.views.activity

import android.util.Log
import com.sik.sikcore.thread.ThreadUtils
import com.sik.siknet.http.httpDownloadBytes
import com.sik.skextensionsample.databinding.ActivityDownloadBinding
import java.util.Base64

class DownloadActivity : BaseActivity<ActivityDownloadBinding>() {
    private val imageUrls = "http://10.53.1.225:7770/6669214529c7_20251223150843691_nfc_face.jpg"
    override fun getViewBinding(): ActivityDownloadBinding {
        return ActivityDownloadBinding.inflate(layoutInflater)
    }

    override fun initView() {
        ThreadUtils.runOnIO {
            val rsp = imageUrls.httpDownloadBytes(data = hashMapOf<String, String>())
            val base64String = Base64.getEncoder().encodeToString(rsp?.bytes)
            Log.i("Download", "$base64String")
        }
    }
}