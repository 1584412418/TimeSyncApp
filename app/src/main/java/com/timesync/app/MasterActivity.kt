package com.timesync.app<rsup indexInItemList="0" type="image">图</rsup>

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MasterActivity : AppCompatActivity() {
    private lateinit var dataServer: DataServer
    private lateinit var tvStatus: TextView
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master)

        tvStatus = findViewById(R.id.tvStatus)
        tvResult = findViewById(R.id.tvResult)

        dataServer = DataServer(8080) { data ->
            runOnUiThread {
                handleSlaveData(data)
            }
        }
        dataServer.start()
        tvStatus.text = "服务器已启动，等待属台连接..."
    }

    private fun handleSlaveData(data: String) {
        try {
            val json = JSONObject(data)
            val slaveId = json.getString("slaveId")
            val timestamp = json.getLong("timestamp")
            val x = json.getDouble("x")
            val y = json.getDouble("y")

            dataServer.addMeasurement(slaveId, timestamp, x, y)

            if (dataServer.hasEnoughData()) {
                val result = SoundLocalizer.calculate(dataServer.getMeasurements())
                tvResult.text = "声源位置：(${result}, ${result})"
            }
        } catch (e: Exception) {
            tvStatus.text = "数据解析错误: ${e.message}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataServer.stop()
    }
}
