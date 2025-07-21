package com.sik.skextensionsample.views.activity

import com.sik.sikandroid.toast.toast
import com.sik.sikcore.extension.setDebouncedClickListener
import com.sik.sikencrypt.EncryptUtils
import com.sik.skextensionsample.config.EncryptorCBCConfig
import com.sik.skextensionsample.databinding.ActivityEncryptorBinding

class EncryptorActivity : BaseActivity<ActivityEncryptorBinding>() {
    private var encryptData: String = ""
    private val encryptor by lazy { EncryptUtils.getAlgorithm(EncryptorCBCConfig.encryptorConfig) }
    override fun getViewBinding(): ActivityEncryptorBinding {
        return ActivityEncryptorBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.encrypt.setDebouncedClickListener {
            encryptData = encryptor
                .encryptToHex(binding.encryptorEt.text.toString().toByteArray())
            binding.encryptResult.text = encryptData
        }
        binding.decrypt.setDebouncedClickListener {
            if (encryptData.isEmpty()) {
                toast("加密数据为空")
                return@setDebouncedClickListener
            }
            val decryptResult = encryptor
                .decryptFromHex(encryptData)
            binding.decryptResult.text = decryptResult
        }
    }
}