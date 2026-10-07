# USB Debug Assistant 架构设计

## 1. 目标与原则

USB Debug Assistant 是一个“通用 + Android 双模式”USB 调试平台，目标是统一开发、测试、调试和协议分析能力，覆盖以下场景：
- 通用 USB 设备调试：串口设备、MCU、传感器、开发板、调试器
- Android 设备调试：手机、平板、USB Host 设备、ADB/Fastboot 集成
- 业务项目复用：将底层 USB 能力封装为公共 SDK

总体原则：
- 分层清晰：应用层、SDK 层、核心库层、平台适配层
- 可扩展：协议插件、设备驱动插件、平台适配插件
- 可复用：底层能力与上层 UI/业务解耦
- 可维护：统一事件、错误、日志和配置模型

## 2. 总体架构

```text
┌────────────────────────────────────────────────────────────────────┐
│                           Application Layer                        │
│  Desktop Debug Tool     Android Device Module     Other Integrators │
│  (Qt/Kotlin/Java)       (USB Host / ADB / Fastboot)               │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                        SDK / Binding Layer                          │
│ C++ SDK  Java/Kotlin SDK  Python SDK  Rust FFI  JNI / FFI Bindings │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                     Core Library Layer (Core Runtime)               │
│ USBManager | USBDevice | USBProtocol | EventSystem | Logger | Config │
│ Device Discovery | Transfer | Protocol Analysis | Plugin Manager     │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                     Platform Abstraction Layer                      │
│ Windows (WinUSB/libusb) | Linux (libusb/udev) | macOS (IOKit)       │
│ Android (USB Host / ADB / Fastboot)                                  │
└───────────────────────────────┬────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│                         Hardware / Device Layer                      │
│ Android phone/tablet | MCU/FPGA/dev boards | Sensor/industrial devices │
└────────────────────────────────────────────────────────────────────┘
```

## 3. 模块职责

### 3.1 Desktop Debug Tool
角色：可视化调试工具，供工程师直接连接和查看设备。
主要职责：
- 设备搜索和连接
- 实时收发数据
- 十六进制/ASCII/文本展示
- 日志与状态监控
- 协议分析和脚本执行
- 多设备管理

### 3.2 Android Device Module
角色：Android 平台适配层，负责 Android USB 设备接入和调试能力。
主要职责：
- Android USB Host 权限处理
- ADB / Fastboot 管理
- Android 设备信息采集
- Android 应用内集成
- 日志采集与导出

### 3.3 Core Runtime
角色：统一底座，抽象所有 USB 能力。
主要职责：
- USB 设备枚举和生命周期管理
- 读写、控制传输、断线恢复
- 事件回调与日志输出
- 协议解析、数据缓存和插件扩展

### 3.4 SDK / Binding Layer
角色：让其他项目可以复用能力。
支持：
- C++ SDK
- Java / Kotlin SDK
- Python SDK
- Rust FFI / bindings

## 4. 核心对象设计

### 4.1 USBDevice
```cpp
class USBDevice {
public:
    virtual ~USBDevice() = default;

    virtual bool open() = 0;
    virtual void close() = 0;
    virtual ssize_t read(uint8_t* buffer, size_t length, int timeoutMs = 1000) = 0;
    virtual ssize_t write(const uint8_t* buffer, size_t length, int timeoutMs = 1000) = 0;
    virtual bool controlTransfer(uint8_t requestType,
                                 uint8_t request,
                                 uint16_t value,
                                 uint16_t index,
                                 uint8_t* data,
                                 uint16_t length,
                                 int timeoutMs = 1000) = 0;
    virtual bool reset() = 0;
    virtual DeviceInfo getDeviceInfo() const = 0;
};
```

### 4.2 USBManager
```cpp
class USBManager {
public:
    virtual ~USBManager() = default;

    virtual std::vector<DeviceInfo> enumerateDevices() = 0;
    virtual std::shared_ptr<USBDevice> findDevice(uint16_t vid, uint16_t pid) = 0;
    virtual void registerListener(std::shared_ptr<USBEventListener> listener) = 0;
    virtual void removeListener(std::shared_ptr<USBEventListener> listener) = 0;
};
```

### 4.3 USBProtocol
```cpp
class USBProtocol {
public:
    virtual ~USBProtocol() = default;
    virtual std::string parseFrame(const uint8_t* data, size_t len) = 0;
    virtual bool validateFrame(const uint8_t* data, size_t len) = 0;
};
```

## 5. 数据与事件模型

### 5.1 事件类型
- DeviceAttached
- DeviceDetached
- DataReceived
- DataSent
- ErrorOccurred
- StatusChanged

### 5.2 统一日志模型
- timestamp
- deviceId
- level (INFO/WARN/ERROR)
- category
- message
- extra data

### 5.3 错误模型
- NOT_FOUND
- ACCESS_DENIED
- TIMEOUT
- BUSY
- INVALID_STATE
- TRANSFER_FAILED
- UNSUPPORTED_DEVICE

## 6. 平台适配策略

### 6.1 Windows
- WinUSB / libusb
- 设备枚举与传输实现
- 断线重连和权限管理

### 6.2 Linux
- libusb / udev
- 设备节点管理
- 端点读取和控制传输

### 6.3 macOS
- IOKit
- USB 设备抽象和事件回调

### 6.4 Android
- Android USB Host API
- ADB / Fastboot 接口
- 权限管理和设备分类

## 7. 可扩展机制

### 7.1 协议插件
```cpp
class IProtocolPlugin {
public:
    virtual ~IProtocolPlugin() = default;
    virtual std::string name() const = 0;
    virtual bool matches(const DeviceInfo& info) const = 0;
    virtual std::string parse(const uint8_t* data, size_t len) = 0;
};
```

### 7.2 设备驱动插件
- 设备特定适配：某类 MCU、传感器、调试器
- 设备特定命令集
- 自定义状态解析和错误处理

## 8. 架构设计的关键价值

- 对业务方透明：上层只依赖统一接口
- 对工程团队友好：库和工具职责明确
- 对研发扩展友好：协议插件和平台适配器可持续扩展
- 对测试友好：统一日志和脚本化测试能力

## 9. 实施建议

第一阶段：最小可用版本
- USB 设备发现
- 读/写接口
- 基础日志
- Android USB Host 集成
- 基础协议展示

第二阶段：企业型调试工具
- 多设备管理
- 实时数据视图
- 批量命令发送
- 自动化回放脚本

第三阶段：平台化能力
- 协议插件生态
- 多平台 SDK
- 设备健康监测
- 批量测试任务系统

---

这份架构设计适合用于项目评审、技术规划和代码落地。
