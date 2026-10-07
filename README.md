# USB Debug Assistant

> 通用 USB 设备调试助手 - 面向通用 USB 设备和 Android 设备的统一调试平台

## 项目愿景

USB Debug Assistant 旨在构建一套统一、可复用、可扩展的 USB 调试解决方案，用于：
- 通用 USB 设备开发与调试
- Android 设备 USB Host 与 ADB/Fastboot 集成
- 协议分析与错误诊断
- 设备日志与自动化回放

## 模块结构

```text
usb-debug-assistant/
├── core/                  # 公共核心库
├── desktop-tool/          # 桌面调试工具
├── android/               # Android 集成模块
├── docs/                  # 设计与需求文档
├── examples/              # 示例代码
├── CMakeLists.txt         # 根构建文件
├── README.md              # 项目说明
└── LICENSE                # 开源协议
```

## 运行方式

### 构建核心库
```bash
mkdir -p build
cd build
cmake ..
cmake --build .
```

### 运行桌面工具示例
```bash
./desktop-tool/usb_debug_tool
```

## 当前状态

当前仓库已包含：
- 架构设计文档
- 路线图
- Core 接口骨架
- Desktop 启动入口
- Android 基础代码骨架

下一步计划：
- 实现真实 USB 枚举逻辑
- 完成桌面工具界面与通信逻辑
- 实现 Android USB Host 与 ADB/Fastboot 集成
- 增加协议插件与自动化脚本
