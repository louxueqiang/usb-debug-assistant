package com.usb.debug.core

import android.content.Context

class UsbDeviceManager(private val context: Context) {
    fun enumerateDevices() {
        // TODO: enumerate Android USB devices and report metadata
    }

    fun openDevice() {
        // TODO: open device with Android USB Host API
    }

    fun sendData(data: ByteArray) {
        // TODO: send data via USB interface
    }
}
