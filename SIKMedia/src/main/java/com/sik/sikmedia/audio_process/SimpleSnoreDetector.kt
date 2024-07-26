package com.sik.sikmedia.audio_process

import com.sik.sikcore.log.LogUtils
import java.io.InputStream
import kotlin.math.log10
import kotlin.math.sqrt

class SimpleSnoreDetector : AudioAnalyzer {
    private val logger = LogUtils.getLogger(SimpleSnoreDetector::class)
    override fun analyze(input: InputStream) {
        try {
            val buffer = ByteArray(1024)
            val fftBuffer = DoubleArray(buffer.size)
            val window = createHannWindow(buffer.size)
            var bytesRead: Int
            var snoreCount = 0

            while (input.read(buffer).also { bytesRead = it } != -1) {
                for (i in buffer.indices) {
                    fftBuffer[i] = buffer[i].toDouble() * window[i]
                }

                val fftResult = FFT.fft(fftBuffer)
                val magnitudes = fftResult.map { sqrt(it.re * it.re + it.im * it.im) }
                val maxMagnitude = magnitudes.maxOrNull() ?: 0.0

                logger.i("分贝数:${10 * log10(maxMagnitude)}")
                if (10 * log10(maxMagnitude) > 40) { // 假设40dB以上为打鼾
                    logger.i("检测到40db以上的分别是")
                    snoreCount++
                }
            }

            if (snoreCount > 10) {
                logger.i("检测到打鼾")
            } else {
                logger.i("没有检测到打鼾")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 生成Hann窗口数组
    private fun createHannWindow(size: Int): DoubleArray {
        val window = DoubleArray(size)
        for (i in window.indices) {
            window[i] = 0.5 * (1 - kotlin.math.cos(2 * Math.PI * i / (size - 1)))
        }
        return window
    }
}

// 简单的FFT实现
object FFT {
    data class Complex(val re: Double, val im: Double)

    fun fft(x: DoubleArray): Array<Complex> {
        val n = x.size
        if (n == 1) return arrayOf(Complex(x[0], 0.0))

        val even = fft(x.filterIndexed { index, _ -> index % 2 == 0 }.toDoubleArray())
        val odd = fft(x.filterIndexed { index, _ -> index % 2 != 0 }.toDoubleArray())

        val result = Array(n) { Complex(0.0, 0.0) }
        for (k in 0 until n / 2) {
            val t = odd[k] * exp(-2.0 * Math.PI * k / n)
            result[k] = even[k] + t
            result[k + n / 2] = even[k] - t
        }
        return result
    }

    private operator fun Complex.plus(other: Complex) = Complex(re + other.re, im + other.im)
    private operator fun Complex.minus(other: Complex) = Complex(re - other.re, im - other.im)
    private operator fun Complex.times(other: Complex) =
        Complex(re * other.re - im * other.im, re * other.im + im * other.re)

    private fun exp(x: Double) = Complex(kotlin.math.cos(x), kotlin.math.sin(x))
}
