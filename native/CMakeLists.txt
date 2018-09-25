cmake_minimum_required(VERSION 3.4.1)

SET(BUILD_SHARED_LIBS ON)

find_package(JNI)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS_RELEASE} -s")

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()

include_directories(${JNI_INCLUDE_DIRS})

file(GLOB_RECURSE duktape_SRC
    "*.h"
    "*.c"
    "*.cpp"
)

list(FILTER duktape_SRC EXCLUDE REGEX ".*CMakeCXXCompilerId.cpp$")
list(FILTER duktape_SRC EXCLUDE REGEX ".*CMakeCCompilerId.c$")

add_library(duktape SHARED ${duktape_SRC})

target_link_libraries(duktape -static -static-libstdc++)
