cmake_minimum_required(VERSION 3.22)
project(nefu VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

file(GLOB_RECURSE LTW_SOURCES         ltw/*.c ltw/*.cpp)
file(GLOB_RECURSE MOBILEGLUES_SOURCES mobileglues/*.cpp mobileglues/*.c)
file(GLOB_RECURSE KRYPTON_SOURCES     krypton/*.c krypton/*.cpp)
file(GLOB_RECURSE ZINK_SOURCES        zink/*.c zink/*.cpp)
file(GLOB_RECURSE VIRGL_SOURCES       virgl/*.c virgl/*.cpp)

if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/ltw/main.h)
    add_compile_definitions(NEFU_HAS_LTW)
    message(STATUS "[NEFU] LTW: enabled")
endif()
if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/mobileglues/gl/gl.h)
    add_compile_definitions(NEFU_HAS_MOBILEGLUES)
    message(STATUS "[NEFU] MobileGlues: enabled")
endif()
if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/krypton/gl4es.h)
    add_compile_definitions(NEFU_HAS_KRYPTON)
    message(STATUS "[NEFU] Krypton: enabled")
endif()
if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/zink/zink_context.h)
    add_compile_definitions(NEFU_HAS_ZINK)
    message(STATUS "[NEFU] Zink: enabled")
endif()
if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/virgl/vrend_renderer.h)
    add_compile_definitions(NEFU_HAS_VIRGL)
    message(STATUS "[NEFU] VirGL: enabled")
endif()

add_library(nefu SHARED
    nefu_core.cpp
    ${LTW_SOURCES}
    ${MOBILEGLUES_SOURCES}
    ${KRYPTON_SOURCES}
    ${ZINK_SOURCES}
    ${VIRGL_SOURCES}
)

target_include_directories(nefu PRIVATE . ltw mobileglues krypton zink virgl)

if(ANDROID)
    target_compile_options(nefu PRIVATE
        -march=armv8-a
        -mtune=cortex-a55
        -O2
        -fno-exceptions
        -fno-rtti
        -fvisibility=hidden
        -ffast-math
        -DNEFU_ASTC_HARDWARE=1
    )
    target_link_libraries(nefu PRIVATE EGL GLESv2 vulkan android log)
else()
    target_compile_options(nefu PRIVATE -O2 -fno-exceptions -fno-rtti)
    find_library(EGL_LIB EGL)
    find_library(GLES_LIB GLESv2)
    if(EGL_LIB AND GLES_LIB)
        target_link_libraries(nefu PRIVATE ${EGL_LIB} ${GLES_LIB})
    endif()
endif()
