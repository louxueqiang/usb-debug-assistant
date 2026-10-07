package com.usb.debug.core

import com.usb.debug.core.model.LogEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * USB 日志记录器
 */
class UsbLogger {
    private val logs = CopyOnWriteArrayList<LogEntry>()
    private val listeners = CopyOnWriteArrayList<(LogEntry) -> Unit>()
    private val maxLogs = 10000 // 最多保存 10000 条日志

    /**
     * 记录日志
     */
    fun log(level: String, tag: String, message: String, extra: String = "") {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            extra = extra
        )
        
        logs.add(entry)
        if (logs.size > maxLogs) {
            logs.removeAt(0)
        }

        // 通知监听器
        listeners.forEach { it(entry) }

        // 同时打印到 Logcat
        android.util.Log.println(
            when (level) {
                "DEBUG" -> android.util.Log.DEBUG
                "INFO" -> android.util.Log.INFO
                "WARN" -> android.util.Log.WARN
                "ERROR" -> android.util.Log.ERROR
                else -> android.util.Log.INFO
            },
            tag,
            message + (if (extra.isNotEmpty()) " ($extra)" else "")
        )
    }

    fun debug(tag: String, message: String, extra: String = "") = log("DEBUG", tag, message, extra)
    fun info(tag: String, message: String, extra: String = "") = log("INFO", tag, message, extra)
    fun warn(tag: String, message: String, extra: String = "") = log("WARN", tag, message, extra)
    fun error(tag: String, message: String, extra: String = "") = log("ERROR", tag, message, extra)

    /**
     * 获取所有日志
     */
    fun getLogs(): List<LogEntry> = logs.toList()

    /**
     * 按标签过滤日志
     */
    fun getLogsByTag(tag: String): List<LogEntry> = logs.filter { it.tag == tag }

    /**
     * 按级别过滤日志
     */
    fun getLogsByLevel(level: String): List<LogEntry> = logs.filter { it.level == level }

    /**
     * 清空日志
     */
    fun clear() = logs.clear()

    /**
     * 注册日志监听器
     */
    fun addLogListener(listener: (LogEntry) -> Unit) {
        listeners.add(listener)
    }

    /**
     * 导出日志到文件
     */
    fun exportToFile(directory: File): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(directory, "usb_debug_$timestamp.log")
            
            file.bufferedWriter().use { writer ->
                logs.forEach { entry ->
                    writer.write(entry.fullMessage)
                    writer.newLine()
                }
            }
            file
        } catch (e: Exception) {
            android.util.Log.e("UsbLogger", "Failed to export logs: ${e.message}")
            null
        }
    }

    companion object {
        private var instance: UsbLogger? = null

        fun getInstance(): UsbLogger {
            if (instance == null) {
                instance = UsbLogger()
            }
            return instance!!
        }
    }
}
