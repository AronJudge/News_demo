
cmake_minimum_required(VERSION 3.4.1)


include_directories(breakpad/src breakpad/src/common/android/include)
add_subdirectory(breakpad)

add_library(
        bugly
        SHARED
        ndk_crash.cpp)


target_link_libraries(
        bugly
        breakpad
        log)