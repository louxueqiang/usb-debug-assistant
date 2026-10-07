#pragma once

#include <mutex>
#include <string>
#include <vector>

#include "usb_types.h"

class USBLogger {
public:
    static USBLogger& instance();

    void info(const std::string& tag, const std::string& message);
    void warn(const std::string& tag, const std::string& message);
    void error(const std::string& tag, const std::string& message);

    std::vector<LogEntry> dump() const;

private:
    USBLogger() = default;
    mutable std::mutex mutex_;
    std::vector<LogEntry> logs_;
};
