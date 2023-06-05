package com.sik.sikroute

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

/**
 * 导航管理
 */
class RouteManager {
    companion object {
        /**
         * 实例
         */
        val instance: RouteManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RouteManager()
        }
    }

    /**
     * 导航类
     */
    private val routeClasses: MutableList<KClass<*>> = mutableListOf()

    /**
     * 导航表
     */
    private val routeMap: HashMap<String, BaseView> = hashMapOf()

    /**
     * 起始导航
     */
    private var startRouteName: String = ""

    /**
     * 初始化，设置包含Router注解的类
     */
    fun init(vararg kClass: KClass<*>) {
        routeClasses.addAll(kClass)
        initRoute()
    }

    /**
     * 初始化导航
     */
    private fun initRoute() {
        for (routeClass in routeClasses) {
            val routeConfig: RouteConfig? = routeClass.findAnnotation()
            if (routeConfig != null) {
                val routeConfigInstance = routeClass.createInstance()
                val routeFunctions = routeClass.memberFunctions
                for (routeFunction in routeFunctions) {
                    val route: Route? = routeFunction.findAnnotation()
                    if (route != null) {
                        if (route.isStart) {
                            startRouteName = route.name
                        }
                        routeMap[route.name] =
                            routeFunction.call(routeConfigInstance) as BaseView
                    }
                }
            }
        }
    }

    /**
     * 界面导航
     */
    @Composable
    fun NavGraphMain(viewModelStoreOwner: ViewModelStoreOwner) {
        if (routeClasses.isEmpty()) {
            throw RouteException("Please call init first and set class has Route annotation")
        }
        val navController = rememberNavController()
        NavHost(navController, startDestination = startRouteName) {
            for (mutableEntry in routeMap) {
                composable(mutableEntry.key) {
                    mutableEntry.value.setViewModelStoreOwner(viewModelStoreOwner)
                    mutableEntry.value.Content(navController)
                }
            }
        }
    }
}