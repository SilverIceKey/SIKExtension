package com.sik.sikmedia.audio_process

import java.io.InputStream

// 定义一个接口，用于分析音频数据
interface AudioAnalyzer {
    fun setDataLength(dataLength: Long)//设置数据长度
    fun analyze(input: InputStream) // 接受输入流作为输入
}
