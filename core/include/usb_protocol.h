#pragma once

#include <cstddef>
#include <cstdint>
#include <memory>
#include <string>

#include "usb_types.h"

class USBProtocol {
public:
    virtual ~USBProtocol() = default;
    virtual std::string parseFrame(const uint8_t* data, size_t len) = 0;
    virtual bool validateFrame(const uint8_t* data, size_t len) = 0;
};

std::unique_ptr<USBProtocol> createDefaultUSBProtocol();
