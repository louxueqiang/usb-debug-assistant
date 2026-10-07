package com.usb.debug.core

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import com.usb.debug.core.listener.UsbEventListener
import com.usb.debug.core.model.DeviceInfo
import com.usb.debug.core.model.EndpointInfo
import com.usb.debug.core.model.InterfaceInfo
import java.util.concurrent.CopyOnWriteArrayList

/**
 * USB 设备管理器 - 负责设备扫描和生命周期管理
 */
class UsbDeviceManager(
    private val context: Context
) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val listeners = CopyOnWriteArrayList<UsbEventListener>()
    private val deviceCache = mutableMapOf<String, DeviceInfo>()

    /**
     * 扫描并返回所有已连接的 USB 设备
     */
    fun enumerateDevices(): List<DeviceInfo> {
        val devices = mutableListOf<DeviceInfo>()
        val deviceList = usbManager.deviceList

        for ((key, device) in deviceList) {
            val info = createDeviceInfo(device)
            devices.add(info)
            deviceCache[key] = info
        }

        logInfo("enumerateDevices", "Found ${devices.size} USB devices")
        return devices
    }

    /**
     * 按 VID/PID 查找设备
     */
    fun findDeviceByVidPid(vendorId: Int, productId: Int): DeviceInfo? {
        return enumerateDevices().find {
            it.vendorId == vendorId && it.productId == productId
        }
    }

    /**
     * 创建 DeviceInfo 对象
     */
    private fun createDeviceInfo(device: UsbDevice): DeviceInfo {
        val interfaces = mutableListOf<InterfaceInfo>()

        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            val endpoints = mutableListOf<EndpointInfo>()

            for (j in 0 until iface.endpointCount) {
                val endpoint = iface.getEndpoint(j)
                endpoints.add(
                    EndpointInfo(
                        address = endpoint.address,
                        attributes = endpoint.attributes,
                        maxPacketSize = endpoint.maxPacketSize,
                        interval = endpoint.interval
                    )
                )
            }

            interfaces.add(
                InterfaceInfo(
                    id = iface.id,
                    interfaceClass = iface.interfaceClass,
                    interfaceSubclass = iface.interfaceSubclass,
                    interfaceProtocol = iface.interfaceProtocol,
                    endpointCount = iface.endpointCount,
                    endpoints = endpoints
                )
            )
        }

        return DeviceInfo(
            deviceName = device.deviceName,
            manufacturer = device.manufacturerName,
            productName = device.productName,
            serialNumber = device.serialNumber,
            vendorId = device.vendorId,
            productId = device.productId,
            deviceClass = device.deviceClass,
            deviceSubclass = device.deviceSubclass,
            deviceProtocol = device.deviceProtocol,
            interfaceCount = device.interfaceCount,
            configurationCount = device.configurationCount,
            usbDevice = device
        )
    }

    /**
     * 注册事件监听器
     */
    fun addEventListener(listener: UsbEventListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            logInfo("addEventListener", "Listener registered")
        }
    }

    /**
     * 移除事件监听器
     */
    fun removeEventListener(listener: UsbEventListener) {
        listeners.remove(listener)
        logInfo("removeEventListener", "Listener removed")
    }

    /**
     * 触发设备连接事件
     */
    internal fun fireDeviceAttached(device: DeviceInfo) {
        listeners.forEach { it.onDeviceAttached(device) }
    }

    /**
     * 触发设备断开事件
     */
    internal fun fireDeviceDetached(device: DeviceInfo) {
        listeners.forEach { it.onDeviceDetached(device) }
    }

    /**
     * 触发数据接收事件
     */
    internal fun fireDataReceived(device: DeviceInfo, data: ByteArray, endpoint: Int) {
        listeners.forEach { it.onDataReceived(device, data, endpoint) }
    }

    /**
     * 触发错误事件
     */
    internal fun fireError(device: DeviceInfo?, errorCode: Int, message: String) {
        listeners.forEach { it.onError(device, errorCode, message) }
    }

    private fun logInfo(tag: String, message: String) {
        android.util.Log.i("UsbDeviceManager", "[$tag] $message")
    }
}
