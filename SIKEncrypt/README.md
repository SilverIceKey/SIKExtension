# 编码和加解密库

## 说明：

囊括目前主流的一些算法，关于国密的算法底层使用的是[guanzhi/GmSSL: 支持国密SM2/SM3/SM4/SM9/SSL的密码工具箱 (github.com)](https://github.com/guanzhi/GmSSL)

## 方法：

## 方法介绍：

### [信息摘要工具类](./src/main/java/com/sik/sikencrypt/MessageDigestUtils.kt)

```kotlin
//根据输入的type返回的对应的信息摘要的接口，目前有MD5、SHA256、SM3
fun getMode(messageDigestTypes: MessageDigestTypes): IMessageDigest
//获取信息摘要使用十六进制输出
fun digestToHex(dataBytes: ByteArray): String
//获取信息摘要使用Base64输出
fun digestToBase64(dataBytes: ByteArray): String
```

