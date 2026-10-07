package com.usb.debug.assistant

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.usb.debug.core.UsbDeviceManager
import com.usb.debug.core.UsbCommunicator
import com.usb.debug.core.UsbLogger
import com.usb.debug.core.listener.SimpleUsbEventListener
import com.usb.debug.core.model.DeviceInfo
import com.usb.debug.core.device.DeviceTypeDetector
import kotlinx.coroutines.*

/**
 * 主界面
 * 显示设备列表、设备详情和日志
 */
class MainActivity : AppCompatActivity() {
    private lateinit var deviceManager: UsbDeviceManager
    private lateinit var communicator: UsbCommunicator
    private lateinit var logger: UsbLogger
    private lateinit var deviceTypeDetector: DeviceTypeDetector
    private lateinit var deviceAdapter: DeviceListAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var currentDevice: DeviceInfo? = null
    private var currentEndpoint: Int = 0x81 // 默认输入端点

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化组件
        logger = UsbLogger.getInstance()
        deviceManager = UsbDeviceManager(this)
        communicator = UsbCommunicator(this, deviceManager)
        deviceTypeDetector = DeviceTypeDetector()

        // 设置 UI
        setupUI()

        // 扫描设备
        scanDevices()
    }

    private fun setupUI() {
        val deviceRecyclerView = findViewById<RecyclerView>(R.id.deviceRecyclerView)
        val deviceInfoText = findViewById<TextView>(R.id.deviceInfoText)
        val logRecyclerView = findViewById<RecyclerView>(R.id.logRecyclerView)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val dataInput = findViewById<EditText>(R.id.dataInput)
        val scanButton = findViewById<Button>(R.id.scanButton)
        val clearLogsButton = findViewById<Button>(R.id.clearLogsButton)

        // 设备列表
        deviceAdapter = DeviceListAdapter { device ->
            onDeviceSelected(device)
        }
        deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceRecyclerView.adapter = deviceAdapter

        // 扫描按钮
        scanButton.setOnClickListener {
            scanDevices()
        }

        // 发送数据按钮
        sendButton.setOnClickListener {
            val hexData = dataInput.text.toString().trim()
            if (hexData.isNotEmpty() && currentDevice != null) {
                sendData(hexData)
            } else {
                Toast.makeText(this, "请输入数据和选择设备", Toast.LENGTH_SHORT).show()
            }
        }

        // 清空日志按钮
        clearLogsButton.setOnClickListener {
            logger.clear()
            updateLogs(logRecyclerView)
        }

        // 日志列表
        val logAdapter = LogListAdapter()
        logRecyclerView.layoutManager = LinearLayoutManager(this)
        logRecyclerView.adapter = logAdapter

        // 监听日志变化
        logger.addLogListener { entry ->
            runOnUiThread {
                logAdapter.addLog(entry)
                logRecyclerView.scrollToPosition(logAdapter.itemCount - 1)
            }
        }
    }

    private fun scanDevices() {
        scope.launch {
            val devices = deviceManager.enumerateDevices()
            logger.info("MainActivity", "Scanned devices: ${devices.size}")
            
            // 分类设备
            val classified = devices.map { device ->
                val classification = deviceTypeDetector.detect(device)
                Pair(device, classification)
            }

            runOnUiThread {
                deviceAdapter.setDevices(classified)
                Toast.makeText(this@MainActivity, "找到 ${devices.size} 个设备", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onDeviceSelected(device: DeviceInfo) {
        currentDevice = device
        logger.info("MainActivity", "Device selected: ${device.displayName}")
        
        // 关闭之前的连接
        if (communicator.isDeviceConnected(device)) {
            communicator.closeDevice(device)
        }

        // 打开新连接
        scope.launch {
            val success = communicator.openDevice(device)
            if (success) {
                logger.info("MainActivity", "Device opened successfully")
                Toast.makeText(this@MainActivity, "设备已连接", Toast.LENGTH_SHORT).show()
                
                // 开始后台读取
                if (device.interfaceCount > 0) {
                    currentEndpoint = 0x81 // 输入端点
                    communicator.startBackgroundReading(device, currentEndpoint)
                }
            } else {
                logger.error("MainActivity", "Failed to open device")
                Toast.makeText(this@MainActivity, "设备连接失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendData(hexData: String) {
        val device = currentDevice ?: return
        
        scope.launch {
            try {
                // 将十六进制字符串转换为字节数组
                val data = hexStringToByteArray(hexData)
                val result = communicator.writeData(device, 0x01, data) // 使用输出端点 0x01
                
                if (result.success) {
                    logger.info("MainActivity", "Data sent successfully: ${result.bytesTransferred} bytes")
                    Toast.makeText(this@MainActivity, "数据已发送", Toast.LENGTH_SHORT).show()
                } else {
                    logger.error("MainActivity", "Failed to send data: ${result.errorMessage}")
                    Toast.makeText(this@MainActivity, "发送失败: ${result.errorMessage}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                logger.error("MainActivity", "Error: ${e.message}")
                Toast.makeText(this@MainActivity, "错误: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLogs(logRecyclerView: RecyclerView) {
        val adapter = logRecyclerView.adapter as? LogListAdapter
        adapter?.setLogs(logger.getLogs())
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val hex = hexString.replace(" ", "").replace("-", "")
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        if (currentDevice != null) {
            communicator.closeDevice(currentDevice!!)
        }
        communicator.cleanup()
    }
}

/**
 * 设备列表适配器
 */
class DeviceListAdapter(
    private val onDeviceSelected: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DeviceListAdapter.ViewHolder>() {
    private val devices = mutableListOf<Pair<DeviceInfo, com.usb.debug.core.model.DeviceClassification>>()

    fun setDevices(newDevices: List<Pair<DeviceInfo, com.usb.debug.core.model.DeviceClassification>>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.widget.LinearLayout(parent.context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
            setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
        }
        return ViewHolder(view, onDeviceSelected)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (device, classification) = devices[position]
        holder.bind(device, classification)
    }

    override fun getItemCount() = devices.size

    inner class ViewHolder(
        itemView: android.view.View,
        val onDeviceSelected: (DeviceInfo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(device: DeviceInfo, classification: com.usb.debug.core.model.DeviceClassification) {
            val layout = itemView as android.widget.LinearLayout
            layout.removeAllViews()

            // 设备名称
            layout.addView(TextView(itemView.context).apply {
                text = device.displayName
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            })

            // 设备类型
            layout.addView(TextView(itemView.context).apply {
                text = "类型: ${classification.type} (${classification.subType})"
                textSize = 14f
                setTextColor(android.graphics.Color.GRAY)
            })

            // 设备标识
            layout.addView(TextView(itemView.context).apply {
                text = device.deviceIdentifier
                textSize = 12f
                setTextColor(android.graphics.Color.DKGRAY)
            })

            // 点击事件
            itemView.setOnClickListener {
                onDeviceSelected(device)
            }
        }
    }
}

/**
 * 日志列表适配器
 */
class LogListAdapter : RecyclerView.Adapter<LogListAdapter.ViewHolder>() {
    private val logs = mutableListOf<com.usb.debug.core.model.LogEntry>()

    fun addLog(log: com.usb.debug.core.model.LogEntry) {
        logs.add(log)
        notifyItemInserted(logs.size - 1)
    }

    fun setLogs(newLogs: List<com.usb.debug.core.model.LogEntry>) {
        logs.clear()
        logs.addAll(newLogs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TextView(parent.context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(8, 4, 8, 4)
                textSize = 12f
            }
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount() = logs.size

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(entry: com.usb.debug.core.model.LogEntry) {
            textView.text = entry.fullMessage
            textView.setTextColor(
                when (entry.level) {
                    "ERROR" -> android.graphics.Color.RED
                    "WARN" -> android.graphics.Color.parseColor("#FF9800")
                    "INFO" -> android.graphics.Color.BLACK
                    "DEBUG" -> android.graphics.Color.GRAY
                    else -> android.graphics.Color.BLACK
                }
            )
        }
    }
}
