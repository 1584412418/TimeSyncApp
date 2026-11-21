package com.timesync.app

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AudioFingerprint {

    fun extract(audioData: FloatArray, sampleRate: Int = 44100): FloatArray {
        val frameSize = 2048
        val hopSize = 1024
        val numBands = 32
        val fingerprints = mutableListOf<Float>()

        var i = 0
        while (i + frameSize <= audioData.size) {
            val frame = audioData.copyOfRange(i, i + frameSize)
            val spectrum = fft(frame)
            val bandEnergies = FloatArray(numBands)

            val bandSize = spectrum.size / numBands
            for (b in 0 until numBands) {
                var energy = 0f
                for (j in b * bandSize until (b + 1) * bandSize) {
                    energy += spectrum[j]
                }
                bandEnergies[b] = energy / bandSize
            }

            // 归一化
            val maxEnergy = bandEnergies.maxOrNull() ?: 1f
            bandEnergies.forEach { fingerprints.add(it / maxEnergy) }
            i += hopSize
        }
        return fingerprints.toFloatArray()
    }

    fun match(fp1: FloatArray, fp2: FloatArray): Float {
        val len = minOf(fp1.size, fp2.size)
        if (len == 0) return 0f

        var dot = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in 0 until len) {
            dot += fp1[i] * fp2[i]
            norm1 += fp1[i] * fp1[i]
            norm2 += fp2[i] * fp2[i]
        }

        return if (norm1 == 0f || norm2 == 0f) 0f
        else dot / (sqrt(norm1) * sqrt(norm2))
    }

    private fun fft(data: FloatArray): FloatArray {
        val n = data.size
        val real = FloatArray(n)
        val imag = FloatArray(n)
        data.forEachIndexed { i, v -> real[i] = v }

        // 简单FFT实现（基2）
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tr = real[i]; real[i] = real[j]; real[j] = tr
                val ti = imag[i]; imag[i] = imag[j]; imag[j] = ti
            }
        }

        var len = 2
        while (len <= n) {
            val angle = -2 * Math.PI / len
            val wlenR = cos(angle).toFloat()
            val wlenI = sin(angle).toFloat()

            for (i in 0 until n step len) {
                var wR = 1f
                var wI = 0f
                for (k in 0 until len / 2) {
                    val uR = real[i + k]
                    val uI = imag[i + k]
                    val vR = real[i + k + len / 2] * wR - imag[i + k + len / 2] * wI
                    val vI = real[i + k + len / 2] * wI + imag[i + k + len / 2] * wR
                    real[i + k] = uR + vR
                    imag[i + k] = uI + vI
                    real[i + k + len / 2] = uR - vR
                    imag[i + k + len / 2] = uI - vI
                    val nextWR = wR * wlenR - wI * wlenI
                    wI = wR * wlenI + wI * wlenR
                    wR = nextWR
                }
            }
            len = len shl 1
        }

        // 返回幅度谱
        return FloatArray(n / 2) { i ->
            sqrt(real[i] * real[i] + imag[i] * imag[i].toDouble()).toFloat()
        }
    }
}
