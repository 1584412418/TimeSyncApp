package com.timesync.app

import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class DataClient(private val host: String, private val port: Int) {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    fun connect(): Boolean {
        return try {
            socket = Socket(host, port)
            writer = PrintWriter(OutputStreamWriter(socket?.getOutputStream()), true)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun send(data: String): Boolean {
        return try {
            if (writer == null) {
                if (!connect()) return false
            }
            writer?.println(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        writer?.close()
        socket?.close()
        writer = null
        socket = null
    }
}
