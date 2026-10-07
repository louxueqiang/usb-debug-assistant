package com.usb.debug.core.model

import android.hardware.usb.UsbDevice

/**
 * USB 设备信息数据类
 */
data class DeviceInfo(
    val deviceName: String,
    val manufacturer: String?,
    val productName: String?,
    val serialNumber: String?,
    val vendorId: Int,
    val productId: Int,
    val deviceClass: Int,
    val deviceSubclass: Int,
    val deviceProtocol: Int,
    val interfaceCount: Int,
    val configurationCount: Int,
    val usbDevice: UsbDevice
) {
    val displayName: String
        get() = "$productName ($manufacturer)"

    val deviceIdentifier: String
        get() = "VID:${String.format("%04X", vendorId)} PID:${String.format("%04X", productId)}"

    val classInfo: String
        get() = when (deviceClass) {
            0xFF -> "Vendor Specific (0x${String.format("%02X", deviceClass)})"
            0x00 -> "Device"
            0x01 -> "Audio"
            0x02 -> "Communications"
            0x03 -> "HID"
            0x04 -> "Physical"
            0x05 -> "Image"
            0x06 -> "Printer"
            0x07 -> "Mass Storage"
            0x08 -> "Hub"
            0x09 -> "CDC Data"
            0x0A -> "Smart Card"
            0x0B -> "Content Security"
            0x0D -> "Content Security"
            0x0E -> "Video"
            0x0F -> "Personal Healthcare"
            0x10 -> "Audio/Video"
            0xE0 -> "Wireless Controller"
            0xFE -> "Application Specific"
            else -> "Unknown (0x${String.format("%02X", deviceClass)})"
        }
}

/**
 * USB 端点信息
 */
data class EndpointInfo(
    val address: Int,
    val attributes: Int,
    val maxPacketSize: Int,
    val interval: Int
) {
    val direction: String
        get() = if ((address and 0x80) == 0) "OUT" else "IN"

    val type: String
        get() = when (attributes and 0x03) {
            0x00 -> "Control"
            0x01 -> "Isochronous"
            0x02 -> "Bulk"
            0x03 -> "Interrupt"
            else -> "Unknown"
        }
}

/**
 * USB 接口信息
 */
data class InterfaceInfo(
    val id: Int,
    val interfaceClass: Int,
    val interfaceSubclass: Int,
    val interfaceProtocol: Int,
    val endpointCount: Int,
    val endpoints: List<EndpointInfo>
) {
    val classInfo: String
        get() = when (interfaceClass) {
            0xFF -> "Vendor Specific"
            0x00 -> "Device"
            0x01 -> "Audio"
            0x02 -> "Communications (CDC)"
            0x03 -> "HID"
            0x07 -> "Printer"
            0x08 -> "Mass Storage"
            0x0A -> "CDC Data"
            0xFE -> "Application Specific"
            else -> "Unknown"
        }
}

/**
 * USB 传输结果
 */
data class TransferResult(
    val success: Boolean,
    val bytesTransferred: Int,
    val errorCode: Int = 0,
    val errorMessage: String = ""
)

/**
 * 日志条目
 */
data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO",
    val tag: String = "",
    val message: String = "",
    val extra: String = ""
) {
    val formattedTime: String
        get() = android.text.format.DateFormat.format("HH:mm:ss", timestamp).toString()

    val fullMessage: String
        get() = "[$formattedTime] [$level] $tag: $message" + (if (extra.isNotEmpty()) " ($extra)" else "")
}

/**
 * 设备类型
 */
enum class DeviceType {
    ANDROID,           // Android 手机/平板
    CDC_SERIAL,        // USB 虚拟串口（CDC）
    HID,               // 人机接口设备
    MASS_STORAGE,      // 质量存储设备
    PRINTER,           // 打印机
    DEBUGGER,          // 调试器（J-Link、ST-Link）
    MCU,               // 微控制器
    OTHER              // 其他设备
}

/**
 * 设备识别结果
 */
data class DeviceClassification(
    val type: DeviceType,
    val subType: String = "",
    val confidence: Float = 0.5f // 0.0 ~ 1.0
)
