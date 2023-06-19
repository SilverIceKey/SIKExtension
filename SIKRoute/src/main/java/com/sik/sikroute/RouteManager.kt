package com.sik.sikroute

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        @JvmStatic
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
    private val routeMap: HashMap<String, RouteEntity> = hashMapOf()

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
                        routeMap[route.name] = RouteEntity(
                            route.name,
                            route.params,
                            routeFunction.call(routeConfigInstance) as BaseView
                        )
                    }
                }
            }
        }
    }

    /**
     * 生成传递参数
     */
    private fun generateParams(params: Array<String>): String {
        return StringBuilder().apply {
            if (params.isNotEmpty()) {
                if (params.size == 1) {
                    append("/")
                    append("{${params[0]}}")
                } else {
                    append("?")
                    for (param in params) {
                        append(param)
                        append("=")
                        append("{$param}")
                        append("&")
                    }
                }

            }
        }.toString()
    }

    /**
     * 界面导航
     */
    @Composable
    fun NavGraphMain(
        viewModelStoreOwner: ViewModelStoreOwner,
        iRoute: IRoute
    ) {
        if (routeClasses.isEmpty()) {
            throw RouteException("Please call init first and set class has Route annotation")
        }
        val navController = rememberNavController()
        NavHost(navController, startDestination = startRouteName) {
            for (mutableEntry in routeMap) {
                composable(
                    mutableEntry.value.routeName + generateParams(mutableEntry.value.routeParams),
                    arguments = mutableListOf<NamedNavArgument>().apply {
                        if (mutableEntry.value.routeParams.size > 1) {
                            for (routeParam in mutableEntry.value.routeParams) {
                                add(navArgument(routeParam) { nullable = true })
                            }
                        }
                    }) { navBackStackEntry ->
                    mutableEntry.value.routeView.setViewModelStoreOwner(viewModelStoreOwner)
                    mutableEntry.value.routeView.iRoute = iRoute
                    mutableEntry.value.routeView.Content(navController, navBackStackEntry)
                }
            }
        }
    }
}

/**
 * 路由跳转
 */
fun NavHostController.navigate(routeName: String, params: HashMap<String, String> = hashMapOf()) {
    val url = StringBuilder().apply {
        append(routeName)
        if (params.isNotEmpty()) {
            if (params.size == 1) {
                for (param in params) {
                    append("/")
                    append("{${param.value}}")
                }
            } else {
                append("?")
                for (param in params) {
                    append(param.key)
                    append("=")
                    append(param.value)
                    append("&")
                }
            }

        }
    }.toString()
    navigate(url)
}