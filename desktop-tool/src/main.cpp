#include <iostream>
#include <memory>

#include "usb_logger.h"
#include "usb_manager_impl.h"
#include "usb_protocol.h"

int main() {
    USBLogger::instance().info("desktop", "USB Debug Assistant starting");

    auto manager = createUSBManager();
    auto devices = manager->enumerateDevices();
    std::cout << "Detected devices: " << devices.size() << std::endl;

    for (const auto& device : devices) {
        std::cout << "- " << device.deviceName
                  << " (VID: 0x" << std::hex << device.vendorId
                  << ", PID: 0x" << device.productId << std::dec << ")"
                  << " | serial=" << device.serialNumber << std::endl;
    }

    auto protocol = createDefaultUSBProtocol();
    uint8_t sample[] = {0x01, 0x02, 0x10, 0x20};
    std::cout << "Sample frame: " << protocol->parseFrame(sample, sizeof(sample)) << std::endl;

    auto loggerEntries = USBLogger::instance().dump();
    std::cout << "Log entries: " << loggerEntries.size() << std::endl;
    return 0;
}
