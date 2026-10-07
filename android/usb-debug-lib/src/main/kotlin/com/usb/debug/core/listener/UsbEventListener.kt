package com.usb.debug.core.listener

import com.usb.debug.core.model.DeviceInfo
import com.usb.debug.core.model.LogEntry

/**
 * USB 事件监听接口
 */
interface UsbEventListener {
    // 设备生命周期事件
    fun onDeviceAttached(device: DeviceInfo) {}
    fun onDeviceDetached(device: DeviceInfo) {}
    fun onDeviceOpened(device: DeviceInfo) {}
    fun onDeviceClosed(device: DeviceInfo) {}

    // 数据传输事件
    fun onDataReceived(device: DeviceInfo, data: ByteArray, endpoint: Int) {}
    fun onDataSent(device: DeviceInfo, data: ByteArray, endpoint: Int) {}

    // 错误和状态事件
    fun onError(device: DeviceInfo?, errorCode: Int, message: String) {}
    fun onStatusChanged(device: DeviceInfo?, status: String) {}

    // 日志事件
    fun onLog(entry: LogEntry) {}
}

/**
 * 默认的空实现
 */
open class SimpleUsbEventListener : UsbEventListener {
    override fun onDeviceAttached(device: DeviceInfo) {}
    override fun onDeviceDetached(device: DeviceInfo) {}
    override fun onDeviceOpened(device: DeviceInfo) {}
    override fun onDeviceClosed(device: DeviceInfo) {}
    override fun onDataReceived(device: DeviceInfo, data: ByteArray, endpoint: Int) {}
    override fun onDataSent(device: DeviceInfo, data: ByteArray, endpoint: Int) {}
    override fun onError(device: DeviceInfo?, errorCode: Int, message: String) {}
    override fun onStatusChanged(device: DeviceInfo?, status: String) {}
    override fun onLog(entry: LogEntry) {}
}
