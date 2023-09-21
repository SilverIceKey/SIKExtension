package com.sik.siknet.net

import android.content.Context
import android.content.SharedPreferences
import com.sik.sikcore.SIKCore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Auto save cookie jar
 * 自动保存cookie
 * @constructor Create empty Auto save cookie jar
 */
class AutoSaveCookieJar : CookieJar {
    private var sharedPreferences: SharedPreferences =
        SIKCore.getApplication().getSharedPreferences("cookies", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = sharedPreferences.edit()
        for (cookie in cookies) {
            editor.putString(url.host, cookie.toString())
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        val cookies: MutableList<Cookie> = mutableListOf()
        val cookieString = sharedPreferences.getString(url.host, null)
        if (cookieString != null) {
            val cookie = Cookie.parse(url, cookieString)
            if (cookie != null) {
                cookies.add(cookie)
            }
        }
        return cookies
    }
}