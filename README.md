# USB Debug Assistant - 通用 + Android 双模式综合平台

> 一个全面的USB设备调试开发平台，支持所有USB设备的开发调试、通信、协议分析等功能。支持通用桌面工具和Android集成两种模式。

## 🎯 项目愿景

提供统一、高效、易用的USB设备调试解决方案，让开发者和测试人员能够：
- 快速调试各种USB设备（Android手机、MCU、传感器、工业设备等）
- 实时监控USB通信数据
- 自动化测试和批量操作
- 通过核心库集成到自己的项目中

## 📋 核心功能

### 1. 通用USB调试工具 (Desktop Mode)
- ✅ USB设备自动发现与列表
- ✅ 设备信息详细查看（VID/PID、序列号、描述等）
- ✅ 实时数据收发与监控
- ✅ USB端点分析与配置
- ✅ 十六进制/ASCII/文本多格式显示
- ✅ 数据包捕获与协议分析
- ✅ 批量命令发送与自动化脚本
- ✅ 实时日志导出
- ✅ 断线自动重连

### 2. Android集成模块 (Android Mode)
- ✅ ADB/Fastboot支持
- ✅ Android USB设备识别与分类
- ✅ Android设备属性读取
- ✅ USB调试模式检测
- ✅ Android应用层USB通信
- ✅ 多设备管理
- ✅ 系统日志采集

### 3. 核心库 (SDK)
- ✅ 跨平台USB API（Windows/Linux/Mac）
- ✅ 协议栈抽象
- ✅ 设备驱动适配
- ✅ 事件回调机制
- ✅ 数据缓冲管理

## 🏗️ 项目架构

```text
usb-debug-assistant/
├── docs/                          # 文档
│   ├── architecture.md            # 架构设计文档
│   ├── api-reference.md           # API参考
│   ├── protocol-analysis.md       # 协议分析指南
│   └── quick-start.md             # 快速入门
│
├── core/                          # 核心库 (C++)
│   ├── include/
│   │   ├── usb_device.h
│   │   ├── usb_manager.h
│   │   ├── usb_protocol.h
│   │   ├── usb_callbacks.h
│   │   └── usb_types.h
│   ├── src/
│   │   ├── usb_device.cpp
│   │   ├── usb_manager.cpp
│   │   ├── platform_windows.cpp
│   │   ├── platform_linux.cpp
│   │   └── platform_macos.cpp
│   ├── CMakeLists.txt
│   └── tests/
│
├── desktop-tool/                  # 桌面调试工具 (Qt/C++)
│   ├── src/
│   │   ├── main.cpp
│   │   ├── mainwindow.h/cpp
│   │   ├── device_manager.h/cpp
│   │   ├── data_viewer.h/cpp
│   │   └── protocol_analyzer.h/cpp
│   ├── ui/                        # Qt UI文件
│   ├── resources/                 # 资源文件
│   ├── CMakeLists.txt
│   └── scripts/                   # 脚本示例
│
├── android/                       # Android集成模块 (Kotlin/Java)
│   ├── usb-debug-lib/            # USB调试库模块
│   │   ├── src/main/kotlin/
│   │   │   ├── com/usb/debug/
│   │   │   │   ├── core/
│   │   │   │   │   ├── UsbDeviceManager.kt
│   │   │   │   │   ├── UsbCommunicator.kt
│   │   │   │   │   └── UsbEvent.kt
│   │   │   │   ├── adb/
│   │   │   │   │   ├── AdbManager.kt
│   │   │   │   │   └── FastbootManager.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── DeviceInfo.kt
│   │   │   │   │   └── UsbData.kt
│   │   │   │   └── listener/
│   │   │   │       └── UsbEventListener.kt
│   │   ├── build.gradle
│   │   └── AndroidManifest.xml
│   │
│   ├── demo-app/                 # 演示App
│   │   ├── src/main/
│   │   ├── build.gradle
│   │   └── AndroidManifest.xml
│   │
│   └── device_filter.xml          # USB设备过滤配置
│
├── bindings/                      # 语言绑定
│   ├── python/                    # Python绑定 (ctypes)
│   ├── java/                      # Java绑定 (JNI)
│   └── rust/                      # Rust绑定
│
├── examples/                      # 使用示例
│   ├── cpp_example/
│   ├── python_example/
│   ├── java_example/
│   └── android_example/
│
├── tools/                         # 工具脚本
│   ├── build.sh
│   ├── test.sh
│   └── generate_bindings.py
│
├── .github/
│   └── workflows/                 # CI/CD
│       ├── build.yml
│       ├── test.yml
│       └── release.yml
│
├── CMakeLists.txt                 # C++ 构建文件
├── build.gradle.kts               # Gradle 构建文件
├── settings.gradle.kts
├── .gitignore
├── LICENSE
└── README.md
```

## 🚀 快速开始

### 前置要求
- CMake 3.10+
- C++17编译器（MSVC/GCC/Clang）
- Qt 6.0+ (桌面工具)
- Android SDK 28+ (Android模块)
- libusb 1.0.24+

### 编译核心库
```bash
cd core
mkdir build && cd build
cmake ..
cmake --build . --config Release
```

### 编译桌面工具
```bash
cd desktop-tool
mkdir build && cd build
cmake ..
cmake --build . --config Release
```

### Android集成
```bash
cd android
./gradlew build
```

## 📱 使用示例

### C++核心库
```cpp
#include "usb_manager.h"

int main() {
    USBManager manager;
    
    // 枚举所有USB设备
    auto devices = manager.enumerate();
    
    for (auto& device : devices) {
        printf("Device: %s (VID: %04X, PID: %04X)\n", 
               device->get_product(), 
               device->get_vendor_id(), 
               device->get_product_id());
    }
    
    // 打开设备
    if (devices[0]->open()) {
        // 发送数据
        uint8_t data[] = {0x01, 0x02, 0x03};
        devices[0]->write(data, 3);
        
        // 接收数据
        uint8_t buffer[64];
        int bytes = devices[0]->read(buffer, 64);
    }
    
    return 0;
}
```

### Android应用
```kotlin
val manager = UsbDeviceManager(context)
manager.setEventListener(object : UsbEventListener {
    override fun onDeviceAttached(device: DeviceInfo) {
        Log.d("USB", "Device attached: ${device.name}")
    }
    
    override fun onDataReceived(data: ByteArray) {
        Log.d("USB", "Received ${data.size} bytes")
    }
})

// 打开设备
manager.openDevice(deviceInfo)

// 发送数据
manager.sendData(byteArrayOf(0x01, 0x02, 0x03))
```

## 🔧 核心模块说明

### Core库 - USB通信核心
- **跨平台支持**: Windows (WinUSB/libusb), Linux (libusb), macOS (IOKit)
- **多种USB传输模式**: Control, Bulk, Interrupt, Isochronous
- **设备管理**: 列举、打开、关闭、配置
- **错误处理**: 详细的错误码和异常机制

### Desktop工具 - 可视化调试
- **实时监控**: 设备列表、数据流
- **数据分析**: 十六进制显示、协议分析、统计信息
- **自动化**: 脚本发送、批量测试、条件触发
- **日志管理**: 本地存储、导出、搜索

### Android模块 - 移动端集成
- **ADB支持**: 与PC通信、推送文件、执行命令
- **Fastboot**: 刷机、解锁等操作
- **USB Host API**: 原生Android USB通信
- **权限管理**: 设备访问权限申请

## 📚 文档

- [架构设计文档](docs/architecture.md)
- [API参考](docs/api-reference.md)
- [协议分析指南](docs/protocol-analysis.md)
- [快速入门指南](docs/quick-start.md)

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

## 📄 开源协议

MIT License

## 📧 联系方式

有问题或建议？欢迎提Issue或联系开发团队。

---

让USB设备调试变得简单高效！ 🚀
