package com.sik.sikroute

/**
 * 导航实体
 */
data class RouteEntity(
    val routeName: String,
    val routeParams: Array<String>,
    val routeView: BaseView
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteEntity

        if (routeName != other.routeName) return false
        if (!routeParams.contentEquals(other.routeParams)) return false
        if (routeView != other.routeView) return false

        return true
    }

    override fun hashCode(): Int {
        var result = routeName.hashCode()
        result = 31 * result + routeParams.contentHashCode()
        result = 31 * result + routeView.hashCode()
        return result
    }
}