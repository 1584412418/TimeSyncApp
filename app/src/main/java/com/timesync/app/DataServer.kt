package com.timesync.app

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class DataServer(
    private val port: Int,
    private val onDataReceived: (String) -> Unit
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val clients = ConcurrentHashMap<String, Socket>()
    private val measurements = mutableListOf<Measurement>()

    fun start() {
        isRunning = true
        Thread {
            try {
                serverSocket = ServerSocket(port)
                while (isRunning) {
                    val client = serverSocket?.accept() ?: continue
                    Thread {
                        handleClient(client)
                    }.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleClient(socket: Socket) {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        var line: String?
        while (isRunning && socket.isConnected) {
            line = reader.readLine() ?: break
            if (line.isNotEmpty()) {
                onDataReceived(line)
            }
        }
        socket.close()
    }

    fun addMeasurement(slaveId: String, timestamp: Long, x: Double, y: Double) {
        measurements.add(Measurement(slaveId, timestamp, x, y))
    }

    fun getMeasurements(): List<Measurement> = measurements.toList()

    fun hasEnoughData(): Boolean = measurements.size >= 3

    fun stop() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
    }
}
