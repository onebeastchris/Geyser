cmake_minimum_required(VERSION 3.13)
# Without a floor the dylib requires the build host's macOS. cmake-conan passes it on to OpenSSL.
set(CMAKE_OSX_DEPLOYMENT_TARGET "11.0" CACHE STRING "Minimum macOS version")
project(libdatachannel_jni C CXX)

set(CMAKE_C_STANDARD 17)
set(CMAKE_CXX_STANDARD 20)

option(PROJECT_VERSION "The version of the project" "unspecified")
option(ENABLE_HARDENING "Enable compiler/linker hardening flags" ON)
option(STATIC_LIBSTDCXX "Link the C++ runtime statically on Linux" OFF)

set(NO_WEBSOCKET ON CACHE BOOL "configure libdatachannel build")
set(NO_TESTS ON CACHE BOOL "configure libdatachannel build")
set(NO_EXAMPLES ON CACHE BOOL "configure libdatachannel build")
# TODO remove after libdatachannel updates to libsrtp 2.8.0+
set(ENABLE_WARNINGS_AS_ERRORS OFF CACHE BOOL "Disable warnings as errors for libsrtp" FORCE)
set(LIBDATACHANNEL_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/libdatachannel" CACHE PATH "libdatachannel source checkout")
add_subdirectory(${LIBDATACHANNEL_SOURCE_DIR} libdatachannel)

include_directories(${LIBDATACHANNEL_SOURCE_DIR}/include generated)
include_directories(jdk)
if(WIN32)
    include_directories(jdk/windows)
else()
    include_directories(jdk/unix)
endif()


add_compile_options(-Werror -Wall -Wextra -Wno-unused-parameter -fPIC -D "PROJECT_VERSION=${PROJECT_VERSION}")
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_compile_options(-O3)
    # enable LTO, of supported:
    include(CheckIPOSupported)
    check_ipo_supported(RESULT ipo_supported OUTPUT ipo_error)
    if(ipo_supported)
        set(CMAKE_INTERPROCEDURAL_OPTIMIZATION_RELEASE TRUE)
    else()
        message(WARNING "IPO/LTO is not supported: ${ipo_error}")
    endif()
else()
    add_compile_options(-g3 -Og)
endif()

if(APPLE)
    add_link_options(-dead_strip)
    set(CMAKE_POSITION_INDEPENDENT_CODE ON)
    set(CMAKE_INSTALL_RPATH "@loader_path")
    set(CMAKE_BUILD_WITH_INSTALL_RPATH TRUE)
    set(CMAKE_MACOSX_RPATH ON)
elseif(NOT WIN32)
    add_link_options(-z noexecstack)
    if(ENABLE_HARDENING)
        add_link_options(-Wl,-z,relro -Wl,-z,now)
    endif()
else()
    add_link_options(-static)
endif()

add_library(datachannel-java SHARED
        generated/jni-c-to-java.c
        src/init.cpp
        src/util.cpp
        src/native_channel.cpp
        src/native_peer.cpp
        src/native_mux.cpp
        src/native_stun_monitor.cpp
        src/native_track.cpp
        src/callback.cpp)
target_link_libraries(datachannel-java PRIVATE datachannel-static)
if(TRANSPORT_TEARDOWN_TESTS)
    add_executable(candidate-pair-buffer-test test/candidate_pair_buffer.cpp)
    target_include_directories(candidate-pair-buffer-test PRIVATE src)
    target_compile_options(candidate-pair-buffer-test PRIVATE -UNDEBUG)
endif()
if(CMAKE_SYSTEM_NAME STREQUAL "Linux")
    target_link_options(datachannel-java PRIVATE -Wl,--no-undefined)
    if(STATIC_LIBSTDCXX)
        # Bundle the C++ runtime so the library loads on distributions with an older libstdc++
        # than the build host (e.g. EL9 stops at GLIBCXX_3.4.29), and keep its symbols private.
        target_link_options(datachannel-java PRIVATE -static-libstdc++ -Wl,--exclude-libs,ALL)
        # glibc keeps libgcc shared: GCC 12's static unwinder needs _dl_find_object from glibc 2.35.
        # musl has no such catch, and Alpine JRE images such as eclipse-temurin's ship no libgcc.
        include(CheckSymbolExists)
        check_symbol_exists(__GLIBC__ features.h LIBC_IS_GLIBC)
        if(NOT LIBC_IS_GLIBC)
            target_link_options(datachannel-java PRIVATE -static-libgcc)
        endif()
    endif()
endif()
# Debug info is several times the size of the code itself and is of no use in a shipped binary.
# The exported JNI symbols live in the dynamic symbol table, which stripping leaves alone.
if(CMAKE_STRIP AND (CMAKE_BUILD_TYPE STREQUAL "Release" OR CMAKE_BUILD_TYPE STREQUAL "MinSizeRel"))
    if(APPLE)
        set(STRIP_ARGUMENTS -x)
    else()
        set(STRIP_ARGUMENTS --strip-unneeded)
    endif()
    add_custom_command(TARGET datachannel-java POST_BUILD
            COMMAND ${CMAKE_STRIP} ${STRIP_ARGUMENTS} $<TARGET_FILE:datachannel-java>
            COMMENT "Stripping debug info from datachannel-java")
endif()
