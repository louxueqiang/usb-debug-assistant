cmake_minimum_required(VERSION 3.10)
project(usb_core LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(${CMAKE_SOURCE_DIR}/include)

add_library(usb_core
    src/usb_device.cpp
    src/usb_manager.cpp
)

target_include_directories(usb_core
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
)
