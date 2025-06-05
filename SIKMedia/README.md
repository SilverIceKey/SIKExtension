# 媒体库

## 说明：主要用于录音和媒体的编码

## 使用方法：

需要声明一下权限：

```androidManifest
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

## 方法介绍：

### [音频帮助类](./src/main/java/com/sik/sikmedia/AudioHelper.kt)

```kotlin
//保存路径
var savePath = ""
////////////////////////////////使用MediaRecord//////////////////////////////////////////////
//开始录音
fun startRecord(onSuccess: (filePath: String) -> Unit = {})
//暂停录音
fun pauseRecord(onPaused: () -> Unit = {})
//恢复录音
fun resumeRecord(onResumed: () -> Unit = {})
//停止录音
fun stopRecord(onStoped: () -> Unit = {})
////////////////////////////////使用AudioRecord//////////////////////////////////////////////
//开始录音
fun startRecordWithAudioRecord(onSuccess: (filePath: String) -> Unit = {})
//暂停录音
fun pauseRecordWithAudioRecord(onPaused: () -> Unit = {})
//恢复录音
fun resumeRecordWithAudioRecord(onResumed: () -> Unit = {})
//停止录音
fun stopRecordWithAudioRecord(onStoped: () -> Unit = {})
```

### [编码帮助类(未完成)](./src/main/java/com/sik/sikmedia/MediaCodecHelper.kt)

```kotlin
//设置音频编码格式
fun setAudioFormatType(audioFormatType: String): MediaCodecHelper
//设置音频码率
fun setSampleRate(sampleRate: Int): MediaCodecHelper
//设置声道
fun setChannelCount(channelCount: Int): MediaCodecHelper
//开始音频格式转换
fun startAudioConvert(sourceFile: String, targetFile: String)
```

### [媒体播放工具(目前实现音频播放)](./src/main/java/com/sik/sikmedia/MediaPlayerUtils.kt)

支持自动判断是否是m3u8自动切换exoplayer

```kotlin
//播放指定视频源
fun playAudio(dataSource: Any)
//暂停播放
fun pauseAudio()
//恢复播放
fun resumeAudio()
//停止播放
fun stopAudio()
```

### 更新日志
- 2025-06：整理文档格式并补充说明。

