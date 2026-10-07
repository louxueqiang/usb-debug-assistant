#include "usb_protocol.h"

#include <sstream>
#include <string>

class DefaultUSBProtocol : public USBProtocol {
public:
    std::string parseFrame(const uint8_t* data, size_t len) override {
        if (data == nullptr || len == 0) {
            return "empty frame";
        }

        std::ostringstream oss;
        oss << "Frame length=" << len << " bytes; hex=";
        for (size_t i = 0; i < len; ++i) {
            oss << std::hex << (int)data[i];
            if (i + 1 < len) {
                oss << " ";
            }
        }
        return oss.str();
    }

    bool validateFrame(const uint8_t* data, size_t len) override {
        return data != nullptr && len > 0;
    }
};

USBProtocol::~USBProtocol() = default;

std::unique_ptr<USBProtocol> createDefaultUSBProtocol() {
    return std::make_unique<DefaultUSBProtocol>();
}
