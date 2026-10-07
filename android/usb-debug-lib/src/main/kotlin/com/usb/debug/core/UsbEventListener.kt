package com.usb.debug.core

interface UsbEventListener {
    fun onDeviceAttached(deviceName: String)
    fun onDeviceDetached(deviceName: String)
    fun onDataReceived(data: ByteArray)
    fun onError(message: String)
}
