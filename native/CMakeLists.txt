cmake_minimum_required(VERSION 3.4.1)

SET(BUILD_SHARED_LIBS ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI)

set(CMAKE_CXX_FLAGS_RELEASE "-Wno-terminate")

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    message (STATUS "JAVA_INCLUDE_PATH=${JAVA_INCLUDE_PATH}")
    message (STATUS "JAVA_INCLUDE_PATH2=${JAVA_INCLUDE_PATH2}")
endif()
    INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH})
    INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH2})

file(GLOB_RECURSE duktape_SRC
    "*.h"
    "*.c"
    "*.cpp"
)

list(FILTER duktape_SRC EXCLUDE REGEX ".*CMakeCXXCompilerId.cpp$")
list(FILTER duktape_SRC EXCLUDE REGEX ".*CMakeCCompilerId.c$")

add_library(duktape SHARED ${duktape_SRC})

if(WIN32 OR APPLE)
    target_link_libraries(duktape -static -static-libstdc++)
else()
    target_link_libraries(duktape)
endif()
