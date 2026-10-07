cmake_minimum_required(VERSION 3.10)
project(usb_debug_tool LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(${CMAKE_SOURCE_DIR}/../core/include)

add_executable(usb_debug_tool
    src/main.cpp
)

target_link_libraries(usb_debug_tool PRIVATE usb_core)
