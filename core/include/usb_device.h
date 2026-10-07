#pragma once

#include <cstddef>
#include <cstdint>
#include <memory>
#include <string>

#include "usb_types.h"

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

class USBEventListener {
public:
    virtual ~USBEventListener() = default;
    virtual void onDeviceAttached(const DeviceInfo& device) = 0;
    virtual void onDeviceDetached(const DeviceInfo& device) = 0;
    virtual void onDataReceived(const DeviceInfo& device, const uint8_t* data, size_t length) = 0;
    virtual void onError(const DeviceInfo& device, USBErrorCode code, const std::string& message) = 0;
};
