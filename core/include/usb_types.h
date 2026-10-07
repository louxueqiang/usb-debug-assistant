#pragma once

#include <cstdint>
#include <string>
#include <vector>

struct DeviceInfo {
    std::string deviceName;
    std::string manufacturer;
    std::string product;
    std::string serialNumber;
    uint16_t vendorId = 0;
    uint16_t productId = 0;
    uint8_t busNumber = 0;
    uint8_t deviceAddress = 0;
    std::vector<int> interfaces;
};

enum class TransferType {
    Control,
    Bulk,
    Interrupt,
    Isochronous
};

enum class USBErrorCode {
    Success = 0,
    NotFound,
    AccessDenied,
    Busy,
    Timeout,
    TransferFailed,
    InvalidState,
    UnsupportedDevice,
    Unknown
};

struct LogEntry {
    std::string timestamp;
    std::string tag;
    std::string message;
};
