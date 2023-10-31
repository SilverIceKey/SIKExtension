package com.sik.skextensionsample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sik.sikroute.IRoute
import com.sik.sikroute.RouteManager

class MainActivity : ComponentActivity(), IRoute {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RouteManager.instance.NavGraphMain(
                kClass = DefaultRouteConfig::class,
                viewModelStoreOwner = this,
                iRoute = this
            )
        }
    }

    override fun startActivity(targetClass: Class<*>, requestCode: Int, option: Bundle?) {
        val intent = Intent(this, targetClass)
        option?.let {
            intent.putExtras(it)
        }
        startActivityForResult(intent, requestCode, option)
    }

    override fun startActivityUseIntent(intent: Intent) {
        startActivity(intent)
    }
}