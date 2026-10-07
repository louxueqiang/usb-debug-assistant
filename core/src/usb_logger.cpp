#include "usb_logger.h"

#include <chrono>
#include <iomanip>
#include <sstream>

namespace {
std::string nowTimestamp() {
    auto now = std::chrono::system_clock::now();
    auto in_time_t = std::chrono::system_clock::to_time_t(now);
    std::tm tm{};
#if defined(_WIN32)
    localtime_s(&tm, &in_time_t);
#else
    localtime_r(&in_time_t, &tm);
#endif
    std::ostringstream oss;
    oss << std::put_time(&tm, "%Y-%m-%d %H:%M:%S");
    return oss.str();
}
}

USBLogger& USBLogger::instance() {
    static USBLogger inst;
    return inst;
}

void USBLogger::info(const std::string& tag, const std::string& message) {
    std::lock_guard<std::mutex> lock(mutex_);
    logs_.push_back(LogEntry{nowTimestamp(), tag, message});
}

void USBLogger::warn(const std::string& tag, const std::string& message) {
    std::lock_guard<std::mutex> lock(mutex_);
    logs_.push_back(LogEntry{nowTimestamp(), tag, message});
}

void USBLogger::error(const std::string& tag, const std::string& message) {
    std::lock_guard<std::mutex> lock(mutex_);
    logs_.push_back(LogEntry{nowTimestamp(), tag, message});
}

std::vector<LogEntry> USBLogger::dump() const {
    std::lock_guard<std::mutex> lock(mutex_);
    return logs_;
}
