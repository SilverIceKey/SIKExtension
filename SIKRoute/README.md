# 路由库

## 说明：主要作用于Compose的路由跳转，用于单Activity存在多界面的情况

## 使用方法：

在项目中创建一个继承自RouteConfig的类，我这边使用DefaultRouteConfig作为样例。

在DefaultRouteConfig中使用@RouteConfig注解标记为路由配置类如下：

```kotlin
import com.sik.sikroute.Route
import com.sik.sikroute.RouteConfig

@RouteConfig
class DefaultRouteConfig {
    //...路由
}
```

在路由添加路由方法，样例如下：

```kotlin
import com.sik.sikroute.Route
import com.sik.sikroute.RouteConfig

@RouteConfig
class DefaultRouteConfig {
    @Route(name = "first", isStart = true)
    fun firstView() = FirstView()

    @Route(name = "sec", params = ["id", "name"])
    fun secView() = SecView()
}
```

方法添加@Route注解标记为路由界面，@Route中name为路由跳转的名称，推荐使用静态类进行统一管理防止因为名称错误无法跳转，params字段为跳转携带的参数字段，isStart为是否为起始界面

```kotlin
//扩展函数进行路由跳转，可携带参数，默认不携带参数
fun NavHostController.navigate(routeName: String, params: HashMap<String, String> = hashMapOf())
```

关于DefaultRouteConfig中返回的View需要继承BaseView并实现相关方法，在InitView方法上需要增加@Composable注解，样例如下：

```kotlin
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.sik.sikroute.BaseView

class FirstView : BaseView() {
    override fun initViewModel() {

    }

    @Composable
    override fun InitView(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        //界面相关参数
    }
}
```