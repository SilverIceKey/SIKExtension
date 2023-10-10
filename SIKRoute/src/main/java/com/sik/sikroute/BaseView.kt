package com.sik.sikroute

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
    fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        initViewModel()
        val lifecycle = navBackStackEntry.getLifecycle()
        DisposableEffect(key1 = lifecycle, effect = {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        // 处理 ON_CREATE 事件
                        onCreate()
                    }

                    Lifecycle.Event.ON_START -> {
                        // 处理 ON_START 事件
                        onStart()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        // 处理 ON_RESUME 事件
                        onResume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        // 处理 ON_PAUSE 事件
                        onPause()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        // 处理 ON_STOP 事件
                        onStop()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        // 处理 ON_DESTROY 事件
                        onDestroy()
                    }

                    else -> Unit
                }
            }
            lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycle.removeObserver(lifecycleObserver)
            }
        })
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

    open fun onCreate() {

    }

    open fun onStart() {

    }

    open fun onResume() {

    }

    open fun onPause() {

    }

    open fun onStop() {

    }

    open fun onDestroy() {

    }
}