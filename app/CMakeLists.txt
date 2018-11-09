cmake_minimum_required(VERSION 3.4.1)

set(shared_source_dir ./src/main/cpp/)
set(DPNLIB /Users/xutianyi/Desktop/usbjni/libusb/libs)

add_library( # Sets the name of the library.
             dpAndroidApi

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/dpnJniEntry.cpp )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )
add_library(dpnusb SHARED IMPORTED)
set_target_properties(dpnusb PROPERTIES IMPORTED_LOCATION ${DPNLIB}/${ANDROID_ABI}/libdpnusb.so)

target_link_libraries( # Specifies the target library.
                       dpAndroidApi

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib}
                       dpnusb)