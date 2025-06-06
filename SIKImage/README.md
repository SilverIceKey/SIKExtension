# 图像扩展工具

## 说明：这个库主要用于图像处理以及显示作用，基于Coil拥有Coil所有的Api，当前使用Coil版本为1.4.0，不定期更新

## 使用方法：

## 方法介绍：

### [图像转换工具类](./src/main/java/com/sik/sikimage/ImageConvertUtils.kt)

```kotlin
//bitmap转base64
fun bitmapToBase64(bitmap: Bitmap?): String?
//base64转bitmap
fun base64ToBitmap(base64: String?): Bitmap?
//base64转bitmap argb8888
fun base64ToBitmapARGB8888(base64: String?): Bitmap?
//nv21转base64
fun nv21ToBase64(nv21: ByteArray?, width: Int, height: Int): String?
//nv21转base64并且压缩大小至100*100px
fun nv21ToBase64Compress(nv21: ByteArray?, width: Int, height: Int): String?
//bitmap转base64并且压缩大小至100*100px
fun BitmapToBase64Compress(tmpBitmap: Bitmap, width: Int, height: Int): String?
//nv21转bitmap
fun nv21ToBitmap(nv21: ByteArray?, width: Int, height: Int): Bitmap?
//bitmap转nv21
fun bitmapToNv21(src: Bitmap?, width: Int, height: Int): ByteArray?
//保存bitmap
fun saveFile(bitmap: Bitmap): File?
//从文件读取Base64
fun fileToBase64(file: File?): String?
//nv21转Base64
//可以指定转换之后的大小，默认不指定，可以全局设置
fun nv21ToBase64Jpeg(
        nv21: ByteArray,
        width: Int,
        height: Int,
        minSize: Float = minBase64Size,
        maxSize: Float = maxBase64Size,
        quality: Int = currentQuality
    ): String
```

### [矩阵操作工具类](./src/main/java/com/sik/sikimage/MatrixUtils.kt)

```kotlin
//矩阵缩放，缩放之后位置不移动
fun scale(matrix: Matrix, scale: Float)
//矩阵缩放，缩放之后位置移动
fun scale(matrix: Matrix, scale: Float, scaleX: Float = 0f, scaleY: Float = 0f)
```

### [路径工具类](./src/main/java/com/sik/sikimage/PathUtils.kt)

```kotlin
//根据点判断是直线、四边形、三角形并绘制路径
fun getPath(points:FloatArray):Path
```

### [图像工具类](./src/main/java/com/sik/sikimage/ImageUtil.kt)

```kotlin
//获取画面平均亮度
//平均亮度 一般80左右以及以上为下限,180为上限
fun getCameraPreviewLight(previewWidth: Int, previewHeight: Int, data: ByteArray): Long
//nv21旋转角度
fun rotateNV21(input: ByteArray, width: Int, height: Int, rotation: Int): ByteArray
//等比缩放bitmap
fun zoomImg(bm: Bitmap, Scale: Float): Bitmap?
```

### [二维码工具类](./src/main/java/com/sik/sikimage/QRCodeUtils.kt)

```kotlin
//根据bitmap读取二维码
fun readQRCode(bitmap: Bitmap): String
/**
 * 快捷创建二维码bitmap
 */
@JvmOverloads
fun createQRCode(
    info: String,
    size: Int,
    color: Int = -1,
    logo: Bitmap? = null,
    withInfo: Boolean = false
): Bitmap
```

### [条形码工具类](./src/main/java/com/sik/sikimage/BarCodeUtils.kt)

```kotlin
/**
 * 设置条形码格式
 */
fun setBarCodeFormat(barcodeFormat: BarcodeFormat)
//根据bitmap读取条形码
fun readBarCode(bitmap: Bitmap): String
/**
 * 快捷创建条形码bitmap
 */
@JvmOverloads
fun createBarCode(
    info: String,
    width: Int,
    height: Int,
    color: Int = -1,
    withInfo: Boolean = false
): Bitmap
```

### [系统媒体存储工具类](./src/main/java/com/sik/sikimage/MediaStoreUtils.kt)

```kotlin
/**
 * 拉取图片
 */
 fun fetchImages(
    sortKey: String = MediaStore.Images.Media.DATE_MODIFIED,
    sortType: String = "DESC"
 ): List<Uri>
/**
 * 拉取视频
 */
 fun fetchVideos(
    sortKey: String = MediaStore.Video.Media.DATE_MODIFIED,
    sortType: String = "DESC"
 ): List<Uri>
```

### 更新日志
- 2025-06：整理文档格式并补充说明。

