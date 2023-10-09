package com.sik.sikroute

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * view基类
 */
abstract class BaseView {

    private lateinit var viewModelStoreOwner: ViewModelStoreOwner

    /**
     * route interface
     * 路由接口
     */
    lateinit var iRoute: IRoute

    /**
     * 设置viewModelStore
     */
    fun setViewModelStoreOwner(viewModelStoreOwner: ViewModelStoreOwner) {
        this.viewModelStoreOwner = viewModelStoreOwner
        initViewModel()
    }

    /**
     * 初始化数据交互模型
     */
    fun <VM : ViewModel> initViewModel(clazz: Class<VM>): VM {
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
    fun Content(navController: NavHostController,navBackStackEntry: NavBackStackEntry){
        initViewModel()
        InitView(navController, navBackStackEntry)
        loadData()
    }

    /**
     * 初始化界面
     */
    @Composable
    abstract fun InitView(navController: NavHostController, navBackStackEntry: NavBackStackEntry)

    /**
     * Load data
     * 加载数据
     */
    abstract fun loadData()
}