# 网络库

## 说明：使用扩展函数的特性基于Okhttp对Api String进行网络请求调用

## 使用方法：

    Api.httpGet<T>():T//Get 请求
    Api.httpPostForm<T>():T//Post 请求 表单类型
    Api.httpPostJson<T>():T//Post 请求 Json类型
    Api.httpUploadFile<T>():T//Post 请求 上传文件
    Api.httpDownloadFile()//下载文件
## 方法介绍：

    所有的调用均不自动切换线程