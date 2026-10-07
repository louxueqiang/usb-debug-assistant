#include <iostream>
#include <memory>

#include "usb_logger.h"
#include "usb_protocol.h"

int main() {
    auto logger = USBLogger::instance();
    logger.info("app", "USB Debug Assistant started");
    logger.warn("app", "Waiting for device connection");

    auto protocol = createDefaultUSBProtocol();
    uint8_t sample[] = {0x01, 0x02, 0x10, 0x20};
    std::cout << protocol->parseFrame(sample, sizeof(sample)) << std::endl;

    std::cout << "USB Debug Assistant Desktop Tool" << std::endl;
    return 0;
}
