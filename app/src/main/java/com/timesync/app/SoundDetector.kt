package com.timesync.app

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.abs

class SoundDetector(private val context: Context) {

    interface DetectionCallback {
        fun onDetected(timestamp: Long)
    }

    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    private var callback: DetectionCallback? = null
    private var templateManager: SoundTemplateManager? = null

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    fun start(cb: DetectionCallback) {
        callback = cb
        templateManager = SoundTemplateManager(context)
        isRunning = true

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        Thread {
            val buffer = ShortArray(bufferSize / 2)
            while (isRunning) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val energy = calculateEnergy(buffer, read)
                    if (energy > threshold) {
                        callback?.onDetected(System.currentTimeMillis())
                    }
                }
            }
        }.start()
    }

    private val threshold = 500.0

    private fun calculateEnergy(buffer: ShortArray, size: Int): Double {
        var sum = 0.0
        for (i in 0 until size) {
            sum += abs(buffer[i].toDouble())
        }
        return sum / size
    }

    fun stop() {
        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
