cmake_minimum_required(VERSION 3.4.1)

include_directories(breakpad/src breakpad/src/common/android/include)
# libbugly.so
add_library(bugly SHARED bugly.cpp)

add_subdirectory(breakpad)

# 链接ndk中的log库
target_link_libraries(bugly  breakpad log)