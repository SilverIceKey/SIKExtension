# SKExtension
### 介绍：

这是一个Android使用的使用Kotlin编写的工具库以及扩展库，主要是用于方便使用Kotlin开发Android用的。

### 项目包括以下模块

| 名称       | 描述                                                         | 介绍                                               |
| ---------- | ------------------------------------------------------------ | -------------------------------------------------- |
| SIKCore    | 核心库-包含一些基本的例如时间工具类、文件工具类等一些常用工具类 | [核心库(必须依赖)](./SIKCore/README.md)            |
| SIKAndroid | Activity、Fragment相关的库                                   | [Android](./SIKAndroid/README.md)                  |
| SIKImage   | 图像库-包含一些base64和bitmap的转换，保存，Matrix的操作以及二维码的生成 | [图像扩展工具介绍](./SIKImage/README.md)           |
| SIKNet     | 网络库-包含okhttp的扩展、netty的封装以及udp的简单工具        | [网络扩展工具介绍](./SIKNet/README.md)             |
| SIKEncrypt | 编码加解密库-有基本的编码和加解密的方法，目前有MD5、SHA256、SM3的信息摘要生成的方法，预计还会添加AES、DES、TripleDES、SM4、RSA的加解密方法 | [编码和加解密扩展工具介绍](./SIKEncrypt/README.md) |
| SIKMedia   | 媒体库-主要用于录音和媒体的编码                              | [媒体工具介绍](./SIKMedia/README.md)               |
| SIKSensors | 传感器库-主要用于调用指纹验证以及获取一些传感器的数据        | [传感器工具介绍](./SIKSensors/README.md)           |

### 集成方式：

在项目的setting.gradle或者root下的build.gradle中找到

```groovy
repositories {
	maven { url 'https://jitpack.io' }
}
```

在app的build.gradle中进行依赖,版本：[![](https://jitpack.io/v/SilverIceKey/SIKExtension.svg)](https://jitpack.io/#SilverIceKey/SIKExtension)所有模块版本相同

```groovy
//这样会集成所有模块
implementation 'com.github.SilverIceKey:SIKExtension:Tag'
//如果想集成单个模块
implementation 'com.github.SilverIceKey.SIKExtension:模块名称:Tag'
```
