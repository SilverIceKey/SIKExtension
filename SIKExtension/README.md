# 扩展库
## 说明：主要是利用kotlin的扩展函数特性来编写的一些方法减少代码重复编写

## 使用方法：

## 方法介绍：

- ### [数组扩展函数](./src/main/java/com/sik/sikextension/ArrayExtension.kt)

```kotlin
//对Array进行扩展，Array调用getString可以转化为[参数1,参数2]形式文本
fun <T : Comparable<T>> Array<out T>.getString(): String

//list转mutableList
fun <T : Comparable<T>> List<T>.toMutableList(): MutableList<T>
```

- ### [文件扩展函数](./src/main/java/com/sik/sikextension/FileExtension.kt)

```kotlin
//获取文件的输出流
fun File.outputStream(): FileOutputStream
//文件路径直接返回输出流
fun String.fileOutputStream(): FileOutputStream?
//文件路径直接返回文件
fun String.file(): File?
//文件路径直接返回输入流
fun String.fileInputStream(): FileInputStream?
//判断文件是否存在
fun String.exists(): Boolean
//如果文件存在则删除文件
fun String.existsDelete(): Boolean
//判断文件夹是否存在，不存在则创建
fun String.existsAndCreateFolder()
//文件不存的情况下创建文件,存在的情况下直接返回true
fun String.createNewFile(): Boolean
//文件路径写入数据
fun String.write(data: ByteArray)
//文件路径获取文本数据
fun String.getData(): String
```

