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

### [加密工具类](./src/main/java/com/sik/sikencrypt/EncryptUtils.kt)

```kotlin
//根据配置返回加解密工具
fun <T : IEncryptConfig> getAlgorithm(iEncryptConfig: T): IEncrypt
//根据受保护配置和业务侧 Provider 返回加解密工具
fun getProtectedAlgorithm(
    iEncryptConfig: IProtectedEncryptConfig,
    provider: IProtectedEncryptProvider
): IEncrypt
//获取rsa加密工具
fun <T : IRSAEncryptConfig> getAlgorithm(iRSAEncryptConfig: T): IRSAEncrypt
//----------------针对rsa加密工具---------------------
//生成公钥和私钥
fun generateKeyPair(): IRSAEncrypt
//获取公钥
fun getPublicKeyBytes(): ByteArray
//获取私钥
fun getPrivateKeyBytes(): ByteArray
//----------------针对rsa加密工具---------------------
//加密使用十六进制输出
fun encryptToHex(dataBytes: ByteArray): String
//加密使用Base64输出
fun encryptToBase64(dataBytes: ByteArray): String
//加密使用byte数组输出 一般用于文件加密
fun encryptToByteArray(dataBytes: ByteArray):ByteArray
//从十六进制解密
fun decryptFromHex(dataStr: String): String
//从Base64解密
fun decryptFromBase64(dataStr: String): String
//从byte数组解密 一般用于文件解密
fun decryptFromByteArray(dataBytes: ByteArray):ByteArray
```

### 受保护加密

`IProtectedEncryptConfig` 只传 `keyId`、算法、模式和 IV，不向 SDK 暴露密钥。
`IProtectedEncryptProvider` 由业务侧实现，真正的 KeyVault 和 Cipher 可以放在 Native 层。
文件、流和 GCM 等有状态模式通过 `IProtectedCipherSession` 连续 `update`，不能按块重新创建 Cipher。
历史协议中 IV 与 Key 相同时，设置 `ivFromKey = true`，由 C++ 从已注册 Key 派生 IV，
避免把实际 Key 当作 IV 从 Java 传入。

SDK 内置 `NativeCipherProvider`。发布 AAR 使用预编译的 `libsikencrypt_crypto.so`，
该库由 `src/main/cpp` 中的源码和 OpenSSL 静态 `libcrypto.a` 构建，不打包
`libcrypto.so` 或 `libssl.so`。
静态库符号通过链接参数隐藏，只导出 `NativeCipher` 的 JNI 入口，避免与 MMKV、
其他网络库或厂商 SDK 的 OpenSSL/BoringSSL 发生全局符号冲突。

需要重新生成 Native 文件时，使用本机 NDK/CMake 按 `src/main/cpp/CMakeLists.txt`
构建，并替换 `src/main/jniLibs/<abi>/libsikencrypt_crypto.so`。

业务侧 Native KeyVault 可直接加载 `libsikencrypt_crypto.so` 并调用：

```c
int sikencrypt_register_key(
    const char* keyId,
    const unsigned char* key,
    size_t keySize
);
```

`keyId` 映射和业务 Key 不应写入本仓库。当前 `registerKeyForTest` 只用于兼容性测试；
业务接入时应由 App 自己的 Native KeyVault 注册密钥。

### 更新日志
- 2026-11：1.2.11 修复 Native session 关闭竞态，未知或已关闭句柄改为抛出异常，
  并补充并发关闭与连续生命周期内存测试。
- 2025-06：整理文档格式并补充说明。

