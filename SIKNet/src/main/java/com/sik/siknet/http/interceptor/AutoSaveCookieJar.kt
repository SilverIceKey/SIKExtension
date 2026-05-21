package com.sik.siknet.http.interceptor

import com.sik.sikcore.extension.getMMKVData
import com.sik.sikcore.extension.saveMMKVData
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Auto save cookie jar
 * 自动保存cookie
 * @constructor Create empty Auto save cookie jar
 */
class AutoSaveCookieJar : CookieJar {

    companion object {
        val urls: HashSet<String> = HashSet()

        fun clearCookie() {
            urls.forEach {
                it.saveMMKVData("")
            }
            urls.clear()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            urls.add(url.host)
            url.host.saveMMKVData(cookie.toString())
        }
        // 限制存储域名数量，防止长期运行后内存无限增长
        while (urls.size > 200) {
            val oldest = urls.first()
            urls.remove(oldest)
            oldest.saveMMKVData("")
        }
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
        val cookies: MutableList<Cookie> = mutableListOf()
        val cookieString = url.host.getMMKVData("")
        val cookie = Cookie.parse(url, cookieString)
        if (cookie != null) {
            cookies.add(cookie)
        }
        return cookies
    }
}