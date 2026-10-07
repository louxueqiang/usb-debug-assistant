package com.usb.debug.assistant.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log

/**
 * USB 设备广播接收器
 * 监听 USB 设备插入和拔出事件
 */
class UsbBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
                Log.d(TAG, "USB Device Attached: ${device?.deviceName}")
                // 可以在这里触发事件，通知应用程序
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
                Log.d(TAG, "USB Device Detached: ${device?.deviceName}")
                // 可以在这里触发事件，通知应用程序
            }
            UsbManager.ACTION_USB_PERMISSION -> {
                val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
                val permission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                Log.d(TAG, "USB Permission: ${device?.deviceName} - ${if (permission) "GRANTED" else "DENIED"}")
            }
        }
    }

    companion object {
        private const val TAG = "UsbBroadcastReceiver"
    }
}
