package com.sik.skextensionsample

import com.sik.sikroute.Route
import com.sik.sikroute.RouteConfig

@RouteConfig
class DefaultRouteConfig {
    @Route(name = RouteConstants.FIRST_VIEW, isStart = true)
    fun firstView() = FirstView()

    @Route(name = "sec", params = ["id", "name"])
    fun secView() = SecView()
}