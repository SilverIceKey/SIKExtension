# Android

## 说明：这个库主要用于Android Activity、Fragment、Compose相关的库

## 使用方法：

## 方法介绍：

### [权限工具](./src/main/java/com/sik/sikcore/permission/PermissionUtils.kt)

```kotlin
/**
 * 检查和请求权限。
 */
fun checkAndRequestPermissions(
    permissions: Array<String>,
    callback: PermissionCallback = PermissionCallback { }
)

/**
 * 请求文件系统管理权限。
 */
fun requestAllFilesAccessPermission(
    callback: PermissionCallback = PermissionCallback { }
)
```

### 
