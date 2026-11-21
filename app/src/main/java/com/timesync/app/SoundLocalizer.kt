package com.timesync.app

import kotlin.math.sqrt

data class Measurement(
    val slaveId: String,
    val timestamp: Long,
    val x: Double,
    val y: Double
)

object SoundLocalizer {

    private const val SOUND_SPEED = 340.0 // m/s

    // TDOA 声源定位（牛顿迭代法）
    fun calculate(measurements: List<Measurement>): DoubleArray {
        if (measurements.size < 3) {
            return doubleArrayOf(0.0, 0.0)
        }

        // 以第一个测量点为参考
        val ref = measurements
        var x = ref.x
        var y = ref.y

        // 牛顿迭代
        for (iter in 0 until 50) {
            var j11 = 0.0; var j12 = 0.0
            var j21 = 0.0; var j22 = 0.0
            var f1 = 0.0; var f2 = 0.0

            for (i in 1 until measurements.size) {
                val m = measurements[i]
                val dRef = sqrt((x - ref.x).pow2() + (y - ref.y).pow2())
                val dI = sqrt((x - m.x).pow2() + (y - m.y).pow2())
                val deltaT = (m.timestamp - ref.timestamp) / 1000.0
                val expected = (dI - dRef) / SOUND_SPEED

                val dx = (x - m.x) / dI - (x - ref.x) / dRef
                val dy = (y - m.y) / dI - (y - ref.y) / dRef

                j11 += dx * dx
                j12 += dx * dy
                j21 += dx * dy
                j22 += dy * dy
                f1 += dx * (expected - deltaT)
                f2 += dy * (expected - deltaT)
            }

            val det = j11 * j22 - j12 * j21
            if (kotlin.math.abs(det) < 1e-10) break

            val dx = (f1 * j22 - f2 * j12) / det
            val dy = (f2 * j11 - f1 * j21) / det

            x -= dx
            y -= dy

            if (sqrt(dx * dx + dy * dy) < 0.01) break
        }

        return doubleArrayOf(x, y)
    }

    private fun Double.pow2(): Double = this * this
}
