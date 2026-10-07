#pragma once

#include <memory>
#include <vector>

#include "usb_device.h"
#include "usb_types.h"

class SimulatedUSBDevice : public USBDevice {
public:
    explicit SimulatedUSBDevice(DeviceInfo info);

    bool open() override;
    void close() override;
    ssize_t read(uint8_t* buffer, size_t length, int timeoutMs = 1000) override;
    ssize_t write(const uint8_t* buffer, size_t length, int timeoutMs = 1000) override;
    bool controlTransfer(uint8_t requestType,
                         uint8_t request,
                         uint16_t value,
                         uint16_t index,
                         uint8_t* data,
                         uint16_t length,
                         int timeoutMs = 1000) override;
    bool reset() override;
    DeviceInfo getDeviceInfo() const override;

private:
    DeviceInfo info_;
    bool isOpen_ = false;
    std::vector<uint8_t> pendingData_;
    std::vector<uint8_t> lastWrite_;
};

class SimulatedUSBManager : public USBManager {
public:
    SimulatedUSBManager();

    std::vector<DeviceInfo> enumerateDevices() override;
    std::shared_ptr<USBDevice> findDevice(uint16_t vid, uint16_t pid) override;
    void registerListener(std::shared_ptr<USBEventListener> listener) override;
    void removeListener(std::shared_ptr<USBEventListener> listener) override;

private:
    std::vector<DeviceInfo> devices_;
    std::vector<std::shared_ptr<USBEventListener>> listeners_;
};

std::unique_ptr<USBManager> createUSBManager();
