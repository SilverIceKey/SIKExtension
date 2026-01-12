package com.sik.skextensionsample.views.activity

import android.util.Base64
import android.util.Log
import com.sik.sikcore.data.ConvertUtils
import com.sik.sikcore.thread.ThreadUtils
import com.sik.siknet.http.httpDownloadBytes
import com.sik.skextensionsample.databinding.ActivityDownloadBinding

class DownloadActivity : BaseActivity<ActivityDownloadBinding>() {
    private val imageUrls = "http://10.53.1.225:7770/6669214529c7_20251223160624169_nfc_face.jpg"
    override fun getViewBinding(): ActivityDownloadBinding {
        return ActivityDownloadBinding.inflate(layoutInflater)
    }

    override fun initView() {
        ThreadUtils.runOnIO {
            val rsp = imageUrls.httpDownloadBytes(data = hashMapOf<String, String>())
            Log.i("Download", ConvertUtils.bytesToHex(byteArrayOf(rsp?.bytes?.get(0) ?: 0)))
            val base64String = Base64.encodeToString(rsp?.bytes, Base64.NO_WRAP)
            Log.i("Download", "$base64String")
        }
    }
}