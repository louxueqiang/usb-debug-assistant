#include "usb_manager_impl.h"

#include <algorithm>
#include <cstring>
#include <memory>

SimulatedUSBDevice::SimulatedUSBDevice(DeviceInfo info)
    : info_(std::move(info)) {}

bool SimulatedUSBDevice::open() {
    isOpen_ = true;
    return true;
}

void SimulatedUSBDevice::close() {
    isOpen_ = false;
    pendingData_.clear();
}

ssize_t SimulatedUSBDevice::read(uint8_t* buffer, size_t length, int timeoutMs) {
    (void)timeoutMs;
    if (!isOpen_ || buffer == nullptr || length == 0) {
        return -1;
    }

    const size_t toCopy = std::min(length, pendingData_.size());
    if (toCopy == 0) {
        return 0;
    }

    std::memcpy(buffer, pendingData_.data(), toCopy);
    pendingData_.erase(pendingData_.begin(), pendingData_.begin() + static_cast<std::ptrdiff_t>(toCopy));
    return static_cast<ssize_t>(toCopy);
}

ssize_t SimulatedUSBDevice::write(const uint8_t* buffer, size_t length, int timeoutMs) {
    (void)timeoutMs;
    if (!isOpen_ || buffer == nullptr || length == 0) {
        return -1;
    }

    lastWrite_.assign(buffer, buffer + length);
    pendingData_.insert(pendingData_.end(), buffer, buffer + length);
    return static_cast<ssize_t>(length);
}

bool SimulatedUSBDevice::controlTransfer(uint8_t requestType,
                                         uint8_t request,
                                         uint16_t value,
                                         uint16_t index,
                                         uint8_t* data,
                                         uint16_t length,
                                         int timeoutMs) {
    (void)requestType;
    (void)request;
    (void)value;
    (void)index;
    (void)timeoutMs;
    if (!isOpen_ || data == nullptr || length == 0) {
        return false;
    }
    return true;
}

bool SimulatedUSBDevice::reset() {
    pendingData_.clear();
    lastWrite_.clear();
    isOpen_ = false;
    return true;
}

DeviceInfo SimulatedUSBDevice::getDeviceInfo() const {
    return info_;
}

SimulatedUSBManager::SimulatedUSBManager() {
    devices_ = {
        {"USB Serial 1", "USB_DEBUG", "USB Serial", "SN-001", 0x1234, 0x5678, 1, 1, {0, 1}},
        {"Android Debug Bridge", "Google", "Pixel USB", "SN-ANDROID-01", 0x18d1, 0x4ee7, 1, 2, {0, 1}},
        {"USB Test Board", "ACME", "MCU Debug Adapter", "SN-BOARD-01", 0x1d50, 0x6018, 1, 3, {0, 1, 2}}
    };
}

std::vector<DeviceInfo> SimulatedUSBManager::enumerateDevices() {
    return devices_;
}

std::shared_ptr<USBDevice> SimulatedUSBManager::findDevice(uint16_t vid, uint16_t pid) {
    for (const auto& info : devices_) {
        if (info.vendorId == vid && info.productId == pid) {
            return std::make_shared<SimulatedUSBDevice>(info);
        }
    }
    return nullptr;
}

void SimulatedUSBManager::registerListener(std::shared_ptr<USBEventListener> listener) {
    if (listener) {
        listeners_.push_back(listener);
    }
}

void SimulatedUSBManager::removeListener(std::shared_ptr<USBEventListener> listener) {
    listeners_.erase(
        std::remove(listeners_.begin(), listeners_.end(), listener),
        listeners_.end());
}

std::unique_ptr<USBManager> createUSBManager() {
    return std::make_unique<SimulatedUSBManager>();
}
