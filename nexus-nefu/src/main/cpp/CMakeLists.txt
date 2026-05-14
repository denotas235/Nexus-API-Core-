cmake_minimum_required(VERSION 3.10)
project(nefu)

set(CMAKE_CXX_STANDARD 17)

add_library(nefu SHARED nefu_core.cpp)

file(GLOB LTW_SOURCES ltw/*.c)
file(GLOB MOBILEGLUES_SOURCES mobileglues/gl/*.cpp)
file(GLOB KRYPTON_SOURCES krypton/*.c)
file(GLOB ZINK_SOURCES zink/*.c)
file(GLOB VIRGL_SOURCES virgl/*.c)

target_sources(nefu PRIVATE ${LTW_SOURCES} ${MOBILEGLUES_SOURCES} ${KRYPTON_SOURCES} ${ZINK_SOURCES} ${VIRGL_SOURCES})

if(ANDROID)
    target_compile_options(nefu PRIVATE -march=armv8-a -O2 -fno-exceptions)
    target_link_libraries(nefu EGL GLESv2 vulkan android log)
else()
    target_link_libraries(nefu EGL GLESv2 vulkan log)
endif()

target_include_directories(nefu PRIVATE ltw mobileglues krypton zink virgl)
