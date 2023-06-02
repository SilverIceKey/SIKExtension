package com.sk.skextension.utils.route

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController

/**
 * view基类
 */
abstract class BaseView {

    private lateinit var viewModelStoreOwner: ViewModelStoreOwner

    /**
     * 设置viewModelStore
     */
    fun setViewModelStoreOwner(viewModelStoreOwner: ViewModelStoreOwner){
        this.viewModelStoreOwner = viewModelStoreOwner
        initViewModel()
    }

    /**
     * 初始化数据交互模型
     */
    fun <VM:ViewModel> initViewModel(clazz: Class<VM>):VM{
        return ViewModelProvider(viewModelStoreOwner)[clazz]
    }

    /**
     * 初始化数据交互模型
     */
    abstract fun initViewModel()

    /**
     * 设置界面
     */
    @Composable
    fun Content(navController: NavHostController){
        initViewModel()
        initView(navController)
    }

    /**
     * 初始化界面
     */
    @Composable
    abstract fun initView(navController: NavHostController)
}