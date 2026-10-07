#pragma once

#include <memory>
#include <string>
#include <vector>

#include "usb_device.h"
#include "usb_types.h"

class USBManager {
public:
    virtual ~USBManager() = default;

    virtual std::vector<DeviceInfo> enumerateDevices() = 0;
    virtual std::shared_ptr<USBDevice> findDevice(uint16_t vid, uint16_t pid) = 0;
    virtual void registerListener(std::shared_ptr<USBEventListener> listener) = 0;
    virtual void removeListener(std::shared_ptr<USBEventListener> listener) = 0;
};
