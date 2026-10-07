package com.usb.debug.core

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.usb.debug.core.listener.UsbEventListener
import com.usb.debug.core.model.DeviceInfo
import com.usb.debug.core.model.TransferResult
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * USB 通信器 - 负责与 USB 设备的实际数据通信
 */
class UsbCommunicator(
    private val context: Context,
    private val deviceManager: UsbDeviceManager
) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val listeners = CopyOnWriteArrayList<UsbEventListener>()
    private val connections = mutableMapOf<String, UsbDeviceConnection>()
    private val readThreads = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    /**
     * 打开设备连接
     */
    fun openDevice(device: DeviceInfo): Boolean {
        try {
            val connection = usbManager.openDevice(device.usbDevice)
            if (connection != null) {
                connections[device.deviceIdentifier] = connection
                
                // 声明第一个接口
                if (device.interfaceCount > 0) {
                    val iface = device.usbDevice.getInterface(0)
                    connection.claimInterface(iface, true)
                }

                logInfo("openDevice", "Device opened: ${device.displayName}")
                fireDeviceOpened(device)
                return true
            } else {
                logError("openDevice", "Failed to open device")
                fireError(device, -1, "Failed to open device")
                return false
            }
        } catch (e: Exception) {
            logError("openDevice", e.message ?: "Unknown error")
            fireError(device, -1, e.message ?: "Unknown error")
            return false
        }
    }

    /**
     * 关闭设备连接
     */
    fun closeDevice(device: DeviceInfo) {
        try {
            // 停止读线程
            readThreads[device.deviceIdentifier]?.cancel()
            readThreads.remove(device.deviceIdentifier)

            val connection = connections[device.deviceIdentifier]
            if (connection != null) {
                // 释放接口
                if (device.interfaceCount > 0) {
                    val iface = device.usbDevice.getInterface(0)
                    connection.releaseInterface(iface)
                }
                connection.close()
                connections.remove(device.deviceIdentifier)
                logInfo("closeDevice", "Device closed")
                fireDeviceClosed(device)
            }
        } catch (e: Exception) {
            logError("closeDevice", e.message ?: "Unknown error")
        }
    }

    /**
     * 写数据到设备
     */
    fun writeData(device: DeviceInfo, endpoint: Int, data: ByteArray): TransferResult {
        return try {
            val connection = connections[device.deviceIdentifier]
                ?: return TransferResult(false, 0, -1, "Device not connected")

            val bytes = connection.bulkTransfer(endpoint, data, data.size, 1000)
            if (bytes >= 0) {
                logInfo("writeData", "Sent $bytes bytes to endpoint $endpoint")
                fireDataSent(device, data, endpoint)
                TransferResult(true, bytes)
            } else {
                logError("writeData", "Write failed with code $bytes")
                TransferResult(false, 0, bytes, "Write failed")
            }
        } catch (e: Exception) {
            logError("writeData", e.message ?: "Unknown error")
            TransferResult(false, 0, -1, e.message ?: "Unknown error")
        }
    }

    /**
     * 从设备读数据
     */
    fun readData(device: DeviceInfo, endpoint: Int, bufferSize: Int = 4096): TransferResult {
        return try {
            val connection = connections[device.deviceIdentifier]
                ?: return TransferResult(false, 0, -1, "Device not connected")

            val buffer = ByteArray(bufferSize)
            val bytes = connection.bulkTransfer(endpoint, buffer, bufferSize, 1000)
            if (bytes >= 0) {
                val data = buffer.sliceArray(0 until bytes)
                logInfo("readData", "Received $bytes bytes from endpoint $endpoint")
                fireDataReceived(device, data, endpoint)
                TransferResult(true, bytes)
            } else {
                // ���时是正常的（没有数据）
                TransferResult(true, 0)
            }
        } catch (e: Exception) {
            logError("readData", e.message ?: "Unknown error")
            TransferResult(false, 0, -1, e.message ?: "Unknown error")
        }
    }

    /**
     * 启动后台读取线程
     */
    fun startBackgroundReading(device: DeviceInfo, endpoint: Int) {
        val job = scope.launch {
            while (isActive) {
                readData(device, endpoint)
                delay(100) // 100ms 间隔
            }
        }
        readThreads[device.deviceIdentifier] = job
    }

    /**
     * 停止后台读取
     */
    fun stopBackgroundReading(device: DeviceInfo) {
        readThreads[device.deviceIdentifier]?.cancel()
        readThreads.remove(device.deviceIdentifier)
    }

    /**
     * 控制传输
     */
    fun controlTransfer(
        device: DeviceInfo,
        requestType: Int,
        request: Int,
        value: Int,
        index: Int,
        buffer: ByteArray? = null,
        timeout: Int = 1000
    ): TransferResult {
        return try {
            val connection = connections[device.deviceIdentifier]
                ?: return TransferResult(false, 0, -1, "Device not connected")

            val bytes = connection.controlTransfer(
                requestType, request, value, index, buffer, buffer?.size ?: 0, timeout
            )
            if (bytes >= 0) {
                TransferResult(true, bytes)
            } else {
                TransferResult(false, 0, bytes, "Control transfer failed")
            }
        } catch (e: Exception) {
            TransferResult(false, 0, -1, e.message ?: "Unknown error")
        }
    }

    /**
     * 重置设备
     */
    fun resetDevice(device: DeviceInfo): Boolean {
        return try {
            val connection = connections[device.deviceIdentifier] ?: return false
            // Android USB API 没有直接的 reset 方法，但可以关闭重新打开
            closeDevice(device)
            Thread.sleep(500)
            openDevice(device)
        } catch (e: Exception) {
            logError("resetDevice", e.message ?: "Unknown error")
            false
        }
    }

    /**
     * 获取设备连接状态
     */
    fun isDeviceConnected(device: DeviceInfo): Boolean {
        return connections.containsKey(device.deviceIdentifier)
    }

    /**
     * 注册事件监听器
     */
    fun addEventListener(listener: UsbEventListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * 移除事件监听器
     */
    fun removeEventListener(listener: UsbEventListener) {
        listeners.remove(listener)
    }

    private fun fireDeviceOpened(device: DeviceInfo) {
        listeners.forEach { it.onDeviceOpened(device) }
    }

    private fun fireDeviceClosed(device: DeviceInfo) {
        listeners.forEach { it.onDeviceClosed(device) }
    }

    private fun fireDataReceived(device: DeviceInfo, data: ByteArray, endpoint: Int) {
        listeners.forEach { it.onDataReceived(device, data, endpoint) }
    }

    private fun fireDataSent(device: DeviceInfo, data: ByteArray, endpoint: Int) {
        listeners.forEach { it.onDataSent(device, data, endpoint) }
    }

    private fun fireError(device: DeviceInfo, code: Int, message: String) {
        listeners.forEach { it.onError(device, code, message) }
    }

    private fun logInfo(tag: String, message: String) {
        android.util.Log.i("UsbCommunicator", "[$tag] $message")
    }

    private fun logError(tag: String, message: String) {
        android.util.Log.e("UsbCommunicator", "[$tag] $message")
    }

    fun cleanup() {
        scope.cancel()
    }
}
