package com.timesync.app

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object GaussCoord {

    // WGS84 椭球参数
    private const val A = 6378137.0
    private const val F = 1.0 / 298.257223563
    private const val E2 = 2 * F - F * F

    // 6度分带中央经线
    fun getCentralMeridian(longitude: Double): Int {
        return (longitude / 6).toInt() * 6 + 3
    }

    // 经纬度转高斯平面坐标
    fun wgs84ToGauss(latitude: Double, longitude: Double): DoubleArray {
        val L0 = getCentralMeridian(longitude)
        val l = (longitude - L0) * Math.PI / 180.0
        val B = latitude * Math.PI / 180.0

        val N = A / sqrt(1 - E2 * sin(B).pow(2))
        val T = tan(B).pow(2)
        val C = E2 * cos(B).pow(2) / (1 - E2)
        val eta2 = E2 * cos(B).pow(2) / (1 - E2)

        val m0 = A * (1 - E2)
        val m2 = 3.0 / 2.0 * E2 * m0
        val m4 = 5.0 / 4.0 * E2 * m2
        val m6 = 7.0 / 6.0 * E2 * m4
        val m8 = 9.0 / 8.0 * E2 * m6

        val a0 = m0 + m2 / 2 + 3 * m4 / 8 + 5 * m6 / 16 + 35 * m8 / 128
        val a2 = m2 / 2 + m4 / 2 + 15 * m6 / 32 + 7 * m8 / 16
        val a4 = m4 / 8 + 3 * m6 / 16 + 7 * m8 / 32
        val a6 = m6 / 32 + m8 / 16
        val a8 = m8 / 128

        val X = a0 * B - a2 * sin(2 * B) / 2 + a4 * sin(4 * B) / 4 -
                a6 * sin(6 * B) / 6 + a8 * sin(8 * B) / 8

        val x = X + N / 2 * sin(B) * cos(B) * l.pow(2) +
                N / 24 * sin(B) * cos(B).pow(3) * (5 - T + 9 * C + 4 * C * C) * l.pow(4) +
                N / 720 * sin(B) * cos(B).pow(5) * (61 - 58 * T + T * T) * l.pow(6)

        val y = N * cos(B) * l +
                N / 6 * cos(B).pow(3) * (1 - T + C) * l.pow(3) +
                N / 120 * cos(B).pow(5) * (5 - 18 * T + T * T + 14 * C - 58 * T * C) * l.pow(5)

        return doubleArrayOf(x, y + 500000)
    }
}
