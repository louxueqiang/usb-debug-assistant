#pragma once

#include <cstddef>
#include <cstdint>
#include <string>

class USBProtocol {
public:
    virtual ~USBProtocol() = default;
    virtual std::string parseFrame(const uint8_t* data, size_t len) = 0;
    virtual bool validateFrame(const uint8_t* data, size_t len) = 0;
};
