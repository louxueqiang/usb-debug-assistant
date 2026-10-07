# 第一阶段 (v0.1.0) MVP 开发任务清单

## 概述
- **目标**：建立可用的 USB 调试基础设施
- **周期**：4 周
- **交付物**：Core 库 + CLI 工具 + Android Demo
- **团队**：3-4 人

---

## 第 1 周：Core 基础实现

### Day 1-2：头文件设计确认
**责任人**：Core Lead
- [ ] 最终确认 usb_types.h 中的数据结构
  - [ ] DeviceInfo 字段完整
  - [ ] TransferResult 结构清晰
  - [ ] ErrorCode 定义全面
- [ ] 最终确认 usb_device.h 接口
  - [ ] USBDeviceHandle 方法签名
  - [ ] USBEventListener 回调定义
- [ ] 最终确认 usb_manager.h 接口
  - [ ] 枚举、查找、打开设备方法
  - [ ] 事件监听方法

**验收**：头文件通过代码评审，无歧义

### Day 2-3：日志和工具类
**责任人**：Core Developer 1
- [ ] 完成 USBLogger 实现
  - [ ] 线程安全日志
  - [ ] 多级别支持（DEBUG/INFO/WARN/ERROR）
  - [ ] 日志导出
- [ ] 完成 USBUtils 工具类
  - [ ] Hex/ASCII 转换
  - [ ] CRC 计算
  - [ ] 字符串处理
- [ ] 完成 USBConfig 配置类
  - [ ] 全局配置管理
  - [ ] 超时和重试设置

**验收**：单元测试覆盖率 > 80%

### Day 3-5：模拟设备实现
**责任人**：Core Developer 2
- [ ] 完成 SimulatedUSBDevice
  - [ ] open/close 逻辑
  - [ ] read/write 模拟
  - [ ] 数据缓冲管理
  - [ ] 事件回调
- [ ] 完成 SimulatedUSBManager
  - [ ] 设备列表维护
  - [ ] 设备发现
  - [ ] 事件分发
- [ ] 编写单元测试

**验收**：所有单元测试通过

### Day 5：集成测试和 CLI 示例
**责任人**：Core Lead + QA
- [ ] 编写集成测试
  - [ ] 设备发现流程
  - [ ] 数据收发流程
  - [ ] 错误处理流程
- [ ] 完善 Desktop CLI 示例
  - [ ] 设备列表显示
  - [ ] 数据发送/接收演示
  - [ ] 日志展示

**验收**：集成测试全部通过

---

## 第 2 周：平台适配

### Day 6-7：Windows 适配
**责任人**：Platform Developer (Windows)
- [ ] 研究 WinUSB API
  - [ ] 设备枚举方法
  - [ ] 设备打开方法
  - [ ] 数据传输方法
- [ ] 实现 WindowsUSBDevice
  - [ ] 继承 USBDeviceHandle
  - [ ] WinUSB 调用封装
- [ ] 实现 WindowsUSBManager
  - [ ] SetupAPI 集成
  - [ ] 设备枚举
  - [ ] 热插拔检测（可选第 2 阶段）
- [ ] 在 Windows 机器上测试真实设备

**验收**：能枚举真实 USB 设备，能进行基础读写

### Day 8-9：Linux 适配
**责任人**：Platform Developer (Linux)
- [ ] 研究 libusb API
  - [ ] libusb_init/exit
  - [ ] libusb_get_device_list
  - [ ] libusb_open_device_with_vid_pid
- [ ] 实现 LinuxUSBDevice
  - [ ] 继承 USBDeviceHandle
  - [ ] libusb 调用封装
- [ ] 实现 LinuxUSBManager
  - [ ] 设备枚举
  - [ ] 权限处理
- [ ] 在 Linux 机器上测试真实设备

**验收**：能枚举真实 USB 设备，能进行基础读写

### Day 9-10：macOS 适配
**责任人**：Platform Developer (macOS) 或外包
- [ ] 研究 IOKit API（或使用 libusb）
- [ ] 实现 macOSUSBDevice
- [ ] 实现 macOSUSBManager
- [ ] 在 macOS 机器上测试真实设备

**验收**：能枚举真实 USB 设备，能进行基础读写

### Day 10：编译和集成
**责任人**：Build/CI 负责人
- [ ] 建立跨平台 CMake 构建
  - [ ] Windows 构建配置
  - [ ] Linux 构建配置
  - [ ] macOS 构建配置
- [ ] CI/CD 流程建立
- [ ] 构建脚本编写

**验收**：三个平台都能成功编译

---

## 第 3 周：Desktop 工具

### Day 11-12：CLI 增强
**责任人**：Desktop Developer
- [ ] 美化 CLI 输出
  - [ ] 颜色支持（Windows/Linux/macOS）
  - [ ] 表格显示
  - [ ] 进度条
- [ ] 交互式命令
  - [ ] list（列出设备）
  - [ ] open（打开设备）
  - [ ] send（发送数据）
  - [ ] receive（接收数据）
  - [ ] close（关闭设备）
  - [ ] log（显示日志）
- [ ] 命令帮助文档

**验收**：CLI 工具易于使用，支持主要操作

### Day 12-13：数据显示和分析
**责任人**：Desktop Developer
- [ ] 十六进制显示
  - [ ] 对齐显示
  - [ ] ASCII 列
  - [ ] 字节计数
- [ ] ASCII 显示
- [ ] 混合显示（Hex + ASCII）
- [ ] 数据统计（总字节数、传输速度）

**验收**：数据显示清晰准确

### Day 14-15：日志和导出
**责任人**：Desktop Developer
- [ ] 实时日志窗口
  - [ ] 日志级别过滤
  - [ ] 日志搜索
  - [ ] 日志清空
- [ ] 日志导出
  - [ ] CSV 导出
  - [ ] 文本导出
  - [ ] 导出路径选择

**验收**：日志功能完整，导出文件可用

---

## 第 4 周：Android 和文档

### Day 16-17：Android USB Host 集成
**责任人**：Android Developer
- [ ] AndroidManifest.xml 配置
  - [ ] USB_PERMISSION 权限声明
  - [ ] USB 设备过滤配置
- [ ] UsbDeviceManager 基础实现
  - [ ] 获取 USB 管理器
  - [ ] 获取设备列表
  - [ ] 权限申请
- [ ] 权限申请流程
  - [ ] BroadcastReceiver 处理
  - [ ] 权限回调
- [ ] 设备打开和关闭

**验收**：Android Demo 能检测连接的设备

### Day 17-18：Android Demo App
**责任人**：Android Developer
- [ ] UI 布局
  - [ ] 设备列表界面
  - [ ] 设备详情界面
  - [ ] 数据发送/接收界面
- [ ] 设备列表显示
  - [ ] 列出所有 USB 设备
  - [ ] 显示设备信息
  - [ ] 实时更新
- [ ] 简单的数据发送示例

**验收**：Demo App 能正常运行，显示设备列表

### Day 18-20：文档编写
**责任人**：Documentation & QA Lead
- [ ] API 参考文档
  - [ ] C++ API
  - [ ] Android API
  - [ ] 错误码说明
- [ ] Quick Start 指南
  - [ ] 编译步骤
  - [ ] 运行步骤
  - [ ] 故障排查
- [ ] 设备支持列表
  - [ ] 测试过的设备
  - [ ] 已知问题
- [ ] 架构设计文档

**验收**：文档完整，示例清晰

### Day 20：测试和发布准备
**责任人**：QA Lead + Release Manager
- [ ] 功能测试
  - [ ] 设备枚举
  - [ ] 数据收发
  - [ ] 错误处理
  - [ ] 日志导出
- [ ] 多设备测试
- [ ] 多平台测试
- [ ] 版本号和 Release Notes

**验收**：无重大 bug，可发布 v0.1.0

---

## 并行工作（贯穿整个阶段）

### 代码评审
- 每个 PR 必须有代码评审
- 评审清单：功能完整、代码质量、测试覆盖

### 问题跟踪
- 所有 issue 在 GitHub/Jira 中跟踪
- 每日同步进度

### 性能基准
- 建立性能测试用例
- 记录基准数据

---

## 验收标准

### 功能验收
- [ ] Core 库在三个平台编译成功
- [ ] 能枚举连接的 USB 设备
- [ ] 能打开/关闭设备
- [ ] 能发送/接收数据
- [ ] 日志系统完整
- [ ] Android Demo 能运行
- [ ] 文档齐全清晰

### 质量验收
- [ ] 单元测试覆盖 > 80%
- [ ] 集成测试全部通过
- [ ] 代码评审通过
- [ ] 无 P1 级 bug
- [ ] 无内存泄漏（valgrind 检查）

### 发布验收
- [ ] Release Notes 完整
- [ ] 版本号正确（v0.1.0）
- [ ] 发布标签创建
- [ ] 文档发布
- [ ] 示例代码可运行

---

## 应急计划

### 如果平台适配延期
- 优先完成 Windows 和 Linux（最常用）
- macOS 可推到 v0.1.1

### 如果 Android 适配延期
- Core + Desktop 先发布
- Android 部分作为 v0.1.1 更新

### 如果发现严重 bug
- 停止其他工作，优先修复
- 必要时延期发布

---

## 成功指标

1. **按时发布**：第 4 周末成功发布 v0.1.0
2. **功能完整**：所有计划功能都实现
3. **质量达标**：bug 修复率 > 90%
4. **文档充分**：新用户能独立使用
5. **社区反馈**：首周获得 5+ GitHub stars
