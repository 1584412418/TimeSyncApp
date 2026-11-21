package com.timesync.app<rsup indexInItemList="1" type="image">图</rsup>

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject

class SlaveActivity : AppCompatActivity() {
    private lateinit var dataClient: DataClient
    private lateinit var soundDetector: SoundDetector
    private lateinit var tvStatus: TextView
    private var isDetecting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slave)

        tvStatus = findViewById(R.id.tvStatus)
        val btnToggle = findViewById<Button>(R.id.btnToggle)

        checkPermissions()

        dataClient = DataClient("192.168.1.100", 8080)
        soundDetector = SoundDetector(this)

        btnToggle.setOnClickListener {
            if (isDetecting) {
                stopDetection()
            } else {
                startDetection()
            }
        }
    }

    private fun startDetection() {
        soundDetector.start(object : SoundDetector.DetectionCallback {
            override fun onDetected(timestamp: Long) {
                val location = getLocation()
                val json = JSONObject()
                json.put("slaveId", "slave_1")
                json.put("timestamp", timestamp)
                json.put("x", location)
                json.put("y", location)
                dataClient.send(json.toString())
                runOnUiThread { tvStatus.text = "已发送数据: $timestamp" }
            }
        })
        isDetecting = true
    }

    private fun stopDetection() {
        soundDetector.stop()
        isDetecting = false
        tvStatus.text = "已停止检测"
    }

    private fun getLocation(): DoubleArray {
        // 简化：使用模拟坐标，实际应获取GPS
        return doubleArrayOf(0.0, 0.0)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val needRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needRequest.toTypedArray(), 100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDetection()
        dataClient.close()
    }
}
