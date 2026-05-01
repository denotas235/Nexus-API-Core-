cmake_minimum_required(VERSION 3.18)
project(NexusMaliCore CXX)

add_library(nexus_mali_core SHARED
    src/main/cpp/nexus_mali_core.cpp
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(nexus_mali_core
    ${log-lib}
    ${android-lib}
    dl
)

set_target_properties(nexus_mali_core PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
    POSITION_INDEPENDENT_CODE ON
)
