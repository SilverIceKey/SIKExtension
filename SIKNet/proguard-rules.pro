# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保留 Gson 相关的类
-keep class com.google.gson.** { *; }

# 保留 TypeToken 类及其类型信息
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class com.google.gson.reflect.TypeToken { <fields>; }

# 保留反序列化所需的类型签名
-keepclassmembers class * {
    public void fromJson(java.lang.String, com.google.gson.reflect.TypeToken);
}

# 保留 Kotlin 的扩展函数（如 httpGet、httpPostForm 等）
-keep class com.sik.siknet.http.** { *; }
-keepclassmembers class com.sik.siknet.http.** {
    public *;
}
