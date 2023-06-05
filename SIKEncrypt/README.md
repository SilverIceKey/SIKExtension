# 编码和加解密库

## 说明：有基本的编码和加解密的方法，目前有MD5和AES-ECB-pkcs5Padding

## 使用方法：

## 方法介绍：

### [加密工具类](./src/main/java/com/sik/sikencrypt/EncryptUtils)

```kotlin
//ES加密(AES_ECB_PKCS5)
fun AESEncode(key: String, content: String): String
//AES解密(AES_ECB_PKCS7)
fun AESDecode(key: String, content: String): ByteArray
//MD5加密
fun MD5Encode(content: String): String
//key生成
fun generateKey(uuid: String): SecretKey
//AES自带加密
fun encrypt(data: String, key: SecretKey): String
//AES自带解密
fun decrypt(data: String, key: SecretKey): String
```

