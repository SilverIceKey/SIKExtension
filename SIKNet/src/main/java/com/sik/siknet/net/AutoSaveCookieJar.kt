package com.sik.siknet.net

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        val cookiesString = Gson().toJson(cookies.map { it.toString() }.toMutableList())
        editor.putString(url.host, cookiesString)
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        val cookies: MutableList<Cookie> = mutableListOf()
        val cookieString = sharedPreferences.getString(url.host, "")
        if (!cookieString.isNullOrEmpty()){
            val cookiesString =
                Gson().fromJson<MutableList<String>>(
                    cookieString,
                    object : TypeToken<MutableList<String>>() {}.type
                )
            cookiesString.forEach {
                val cookie = Cookie.parse(url, it)
                if (cookie != null) {
                    cookies.add(cookie)
                }
            }
        }
        return cookies
    }
}