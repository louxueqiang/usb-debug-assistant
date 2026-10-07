cmake_minimum_required(VERSION 3.10)
project(usb_debug_assistant LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(${CMAKE_SOURCE_DIR}/core/include)

add_subdirectory(core)
add_subdirectory(desktop-tool)
