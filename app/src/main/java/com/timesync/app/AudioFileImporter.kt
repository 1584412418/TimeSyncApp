package com.timesync.app

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.nio.ByteBuffer

class AudioFileImporter(private val context: Context) {

    fun importFromFile(file: File): FloatArray? {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(file.path)

            val format = extractor.getTrackFormat(0)
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            extractor.selectTrack(0)

            val samples = mutableListOf<Float>()
            val buffer = ByteBuffer.allocate(4096)
            val info = android.media.MediaCodec.BufferInfo()

            while (true) {
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break

                buffer.rewind()
                for (i in 0 until size step 2) {
                    val sample = buffer.getShort(i).toFloat() / 32768f
                    samples.add(sample)
                }
                extractor.advance()
            }

            extractor.release()
            samples.toFloatArray()
        } catch (e: Exception) {
            null
        }
    }

    fun extractFingerprint(audioData: FloatArray): FloatArray {
        // 简化指纹提取：分帧计算能量特征
        val frameSize = 1024
        val hopSize = 512
        val fingerprints = mutableListOf<Float>()

        var i = 0
        while (i + frameSize <= audioData.size) {
            var energy = 0f
            for (j in i until i + frameSize) {
                energy += audioData[j] * audioData[j]
            }
            fingerprints.add(energy / frameSize)
            i += hopSize
        }
        return fingerprints.toFloatArray()
    }
}
