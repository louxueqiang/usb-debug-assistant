package com.usb.debug.core.device

import com.usb.debug.core.model.DeviceInfo
import com.usb.debug.core.model.DeviceType
import com.usb.debug.core.model.DeviceClassification

/**
 * USB 设备类型检测器
 * 根据 VID/PID 和设备类信息自动识别设备类型
 */
class DeviceTypeDetector {
    /**
     * 检测设备类型
     */
    fun detect(device: DeviceInfo): DeviceClassification {
        // 1. 先检查常见的 VID/PID
        val byVidPid = detectByVidPid(device.vendorId, device.productId)
        if (byVidPid.confidence > 0.7f) {
            return byVidPid
        }

        // 2. 再检查设备类
        val byClass = detectByClass(device.deviceClass, device.deviceSubclass)
        if (byClass.confidence > 0.7f) {
            return byClass
        }

        // 3. 最后检查接口类
        val byInterface = detectByInterface(device)
        return byInterface
    }

    /**
     * 根据 VID/PID 检测
     */
    private fun detectByVidPid(vid: Int, pid: Int): DeviceClassification {
        return when {
            // Google Nexus / Pixel
            vid == 0x18D1 && pid == 0x4EE7 -> DeviceClassification(
                DeviceType.ANDROID,
                "Google Nexus/Pixel",
                0.95f
            )
            // Google ADB
            vid == 0x18D1 -> DeviceClassification(
                DeviceType.ANDROID,
                "Google Device",
                0.9f
            )
            // Samsung
            vid == 0x04E8 -> DeviceClassification(
                DeviceType.ANDROID,
                "Samsung Device",
                0.9f
            )
            // HTC
            vid == 0x0BB4 -> DeviceClassification(
                DeviceType.ANDROID,
                "HTC Device",
                0.9f
            )
            // Xiaomi
            vid == 0x2717 -> DeviceClassification(
                DeviceType.ANDROID,
                "Xiaomi Device",
                0.9f
            )
            // FTDI Serial
            vid == 0x0403 && (pid == 0x6001 || pid == 0x6015) -> DeviceClassification(
                DeviceType.CDC_SERIAL,
                "FTDI Serial",
                0.95f
            )
            // CH340 Serial
            vid == 0x1A86 && pid == 0x7523 -> DeviceClassification(
                DeviceType.CDC_SERIAL,
                "CH340 Serial",
                0.95f
            )
            // CP2102 Serial
            vid == 0x10C4 && pid == 0xEA60 -> DeviceClassification(
                DeviceType.CDC_SERIAL,
                "CP2102 Serial",
                0.95f
            )
            // SEGGER J-Link
            vid == 0x1366 -> DeviceClassification(
                DeviceType.DEBUGGER,
                "SEGGER J-Link",
                0.95f
            )
            // STMicroelectronics ST-Link
            vid == 0x0483 && pid == 0x3748 -> DeviceClassification(
                DeviceType.DEBUGGER,
                "STMicroelectronics ST-Link",
                0.95f
            )
            // Arduino
            vid == 0x2341 -> DeviceClassification(
                DeviceType.MCU,
                "Arduino",
                0.9f
            )
            // Raspberry Pi
            vid == 0x0525 -> DeviceClassification(
                DeviceType.MCU,
                "Raspberry Pi",
                0.9f
            )
            else -> DeviceClassification(
                DeviceType.OTHER,
                "Unknown (VID: 0x${String.format("%04X", vid)}, PID: 0x${String.format("%04X", pid)})",
                0.3f
            )
        }
    }

    /**
     * 根据设备类检测
     */
    private fun detectByClass(deviceClass: Int, deviceSubclass: Int): DeviceClassification {
        return when (deviceClass) {
            0x00 -> DeviceClassification(
                DeviceType.OTHER,
                "Device Class",
                0.2f
            )
            0x02 -> DeviceClassification(
                DeviceType.CDC_SERIAL,
                "Communications Device",
                0.6f
            )
            0x03 -> DeviceClassification(
                DeviceType.HID,
                "HID Device",
                0.7f
            )
            0x06 -> DeviceClassification(
                DeviceType.PRINTER,
                "Printer",
                0.8f
            )
            0x07 -> DeviceClassification(
                DeviceType.MASS_STORAGE,
                "Mass Storage",
                0.8f
            )
            0xFF -> DeviceClassification(
                DeviceType.OTHER,
                "Vendor Specific",
                0.5f
            )
            else -> DeviceClassification(
                DeviceType.OTHER,
                "Unknown Class",
                0.2f
            )
        }
    }

    /**
     * 根据接口类检测
     */
    private fun detectByInterface(device: DeviceInfo): DeviceClassification {
        if (device.interfaceCount == 0) {
            return DeviceClassification(DeviceType.OTHER, "No interfaces", 0.1f)
        }

        // 检查所有接口
        var hasCDC = false
        var hasHID = false
        var hasMassStorage = false

        for (i in 0 until device.interfaceCount) {
            val iface = device.usbDevice.getInterface(i)
            when (iface.interfaceClass) {
                0x02 -> hasCDC = true
                0x03 -> hasHID = true
                0x08 -> hasMassStorage = true
            }
        }

        return when {
            hasMassStorage -> DeviceClassification(
                DeviceType.MASS_STORAGE,
                "Mass Storage Device",
                0.7f
            )
            hasCDC -> DeviceClassification(
                DeviceType.CDC_SERIAL,
                "Serial Device",
                0.7f
            )
            hasHID -> DeviceClassification(
                DeviceType.HID,
                "HID Device",
                0.7f
            )
            else -> DeviceClassification(
                DeviceType.OTHER,
                "Unknown Device",
                0.3f
            )
        }
    }
}
