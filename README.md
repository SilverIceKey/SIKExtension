# SKExtension
### 这个库原先为子模块类型，接下来将改为多模块jitpack选择集成类型。

### 项目包括以下模块

| 名称         | 描述                                                         | 介绍                                               |
| ------------ | ------------------------------------------------------------ | -------------------------------------------------- |
| SIKCore      | 核心库-包含一些基本的例如时间工具类、文件工具类等一些常用工具类 | [核心库(必须依赖)](./SIKCore/README.md)            |
| SIKImage     | 图像库-包含一些base64和bitmap的转换，保存，Matrix的操作以及二维码的生成 | [图像扩展工具介绍](./SIKImage/README.md)           |
| SIKExtension | 扩展库-主要是利用kotlin的扩展函数特性来编写的一些方法减少代码重复编写 | [Kotlin扩展函数库](./SIKExtension/README.md)       |
| SIKNet       | 网络库-主要以[liangjingkanji/NET](https://github.com/liangjingkanji/Net)这个库为基础进行扩展的库 | [网络扩展工具介绍](./SIKNet/README.md)             |
| SIKEncrypt   | 编码加解密库-有基本的编码和加解密的方法，目前有MD5和AES-ECB-pkcs5Padding | [编码和加解密扩展工具介绍](./SIKEncrypt/README.md) |
| SIKMedia     | 媒体库-主要用于录音和媒体的编码                              | [媒体工具介绍](./SIKMedia/README.md)               |
| SIKRoute     | 路由库-主要作用于Compose的路由跳转，用于单Activity存在多界面的情况 | [Compose路由工具介绍](./SIKRoute/README.md)        |

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



### 最后一个子模块类型地址

### [SKExtension-last-submodule](https://github.com/SilverIceKey/SKExtension/tree/last_submodule )

### 项目包含以下元素：

- #### 缝合怪
- #### 屎山代码
- #### 高耦合
- #### 拎不清代码一堆



