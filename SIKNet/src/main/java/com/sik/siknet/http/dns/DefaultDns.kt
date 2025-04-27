package com.sik.siknet.http.dns

import okhttp3.Dns
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 默认dns带缓存时效性
 */
class DefaultDns : Dns {

    companion object {
        /**
         * 静态dns解析地址
         * 使用 ConcurrentHashMap 来保证线程安全
         */
        private val staticHostnameDns: ConcurrentHashMap<String, MutableList<String>> = ConcurrentHashMap()

        /**
         * 动态查询的 DNS 缓存（带过期时间）
         * 域名 -> 缓存的 IP 地址 + 过期时间
         */
        private val dynamicCache: ConcurrentHashMap<String, CachedDns> = ConcurrentHashMap()

        // 静态域名添加方法（永久有效，且做去重操作）
        @Synchronized
        fun addStaticHostnameDns(hostname: String, ipList: List<String>) {
            // 获取当前已存在的 IP 地址列表（如果有的话）
            val existingIpList = staticHostnameDns[hostname] ?: mutableListOf()

            // 合并并去重
            val updatedIpList = (existingIpList + ipList).distinct().toMutableList()

            // 更新静态 DNS 映射
            staticHostnameDns[hostname] = updatedIpList
        }

        // 移除静态域名
        fun removeStaticHostnameDns(hostname: String) {
            staticHostnameDns.remove(hostname)
        }

        // 移除静态dns解析地址的指定IP
        fun removeStaticHostnameDnsIp(hostname: String, ipList: List<String>) {
            staticHostnameDns[hostname]?.let { currentList ->
                val updatedList = currentList.filterNot { ip -> ipList.contains(ip) }.toMutableList()
                staticHostnameDns[hostname] = updatedList
            }
        }

        // 动态 DNS 缓存类：存储 IP 地址及过期时间
        data class CachedDns(val ipList: List<String>, val expirationTime: Long)

        // 获取动态缓存的域名并检查是否过期
        private fun getCachedDns(hostname: String): CachedDns? {
            val cached = dynamicCache[hostname]
            return if (cached != null && cached.expirationTime > System.currentTimeMillis()) {
                cached // 如果缓存有效，返回
            } else {
                dynamicCache.remove(hostname) // 如果缓存过期，清除并返回 null
                null
            }
        }

        // 向缓存添加域名及其 IP 和过期时间
        fun addDynamicHostnameDns(hostname: String, ipList: List<String>, ttl: Long = 3600) {
            val expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttl)
            val cachedDns = CachedDns(ipList, expirationTime)
            dynamicCache[hostname] = cachedDns
        }
    }

    override fun lookup(hostname: String): List<InetAddress> {
        // 1. 检查静态映射
        staticHostnameDns[hostname]?.let { ipList ->
            return ipList.map { InetAddress.getByName(it) }
        }

        // 2. 检查动态缓存
        getCachedDns(hostname)?.let { cachedDns ->
            return cachedDns.ipList.map { InetAddress.getByName(it) }
        }

        // 3. 如果没有缓存或静态映射，使用系统 DNS
        val ipList = Dns.SYSTEM.lookup(hostname)

        // 4. 缓存动态 DNS 查询结果
        addDynamicHostnameDns(hostname, ipList.map { it.hostAddress }, ttl = 3600) // 默认缓存 1 小时
        return ipList
    }
}
