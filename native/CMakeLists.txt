cmake_minimum_required(VERSION 3.4.1)

project(duktape4j)

SET(BUILD_SHARED_LIBS ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

find_package(JNI)

if (JNI_FOUND)
    message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
    message(STATUS "JAVA_INCLUDE_PATH=${JAVA_INCLUDE_PATH}")
    message(STATUS "JAVA_INCLUDE_PATH2=${JAVA_INCLUDE_PATH2}")
endif()

INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH})
INCLUDE_DIRECTORIES(${JAVA_INCLUDE_PATH2})

if(CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")
  add_definitions(-D_WIN32_WINNT=0x0602)
  add_definitions(-D_CRT_SECURE_NO_WARNINGS)
  set(CMAKE_CXX_FLAGS_RELEASE "/MT /Oi /Ot /Oy /Ob2 /GF /Gy")
  set(CMAKE_CXX_FLAGS_DEBUG "/ZI /Od /MTd")
  set(CMAKE_C_FLAGS_RELEASE "/MT /Oi /Ot /Oy /Ob2 /GF /Gy")
  set(CMAKE_C_FLAGS_DEBUG "/ZI /Od /MTd")
endif()

message(STATUS "CMAKE_CXX_FLAGS_RELEASE=${CMAKE_CXX_FLAGS_RELEASE}")

file(GLOB_RECURSE duktape_SRC
    "*.h"
    "*.c"
    "*.cpp"
)

list(FILTER duktape_SRC EXCLUDE REGEX ".*CMakeCXXCompilerId.cpp$")
list(FILTER duktape_SRC EXCLUDE REGEX ".*CMakeCCompilerId.c$")

add_library(duktape SHARED ${duktape_SRC})

if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "GNU")
    set(CMAKE_CXX_FLAGS_RELEASE "-Wno-terminate")
else()
    target_link_libraries(duktape)
endif()
