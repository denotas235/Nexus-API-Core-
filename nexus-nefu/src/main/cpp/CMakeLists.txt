cmake_minimum_required(VERSION 3.18)
project(nefu C CXX)

set(CMAKE_CXX_STANDARD 17)
add_library(nefu SHARED nefu_core.cpp)

target_include_directories(nefu PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/ltw
    ${CMAKE_CURRENT_SOURCE_DIR}/mobileglues
    ${CMAKE_CURRENT_SOURCE_DIR}/krypton
    ${CMAKE_CURRENT_SOURCE_DIR}/zink
    ${CMAKE_CURRENT_SOURCE_DIR}/virgl
)

target_link_libraries(nefu EGL GLESv2 log)
