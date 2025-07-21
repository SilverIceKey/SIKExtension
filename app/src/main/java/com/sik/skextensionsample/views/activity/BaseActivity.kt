package com.sik.skextensionsample.views.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        initView()
    }

    abstract fun getViewBinding(): T

    abstract fun initView()
}