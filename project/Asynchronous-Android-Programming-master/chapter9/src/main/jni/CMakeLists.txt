cmake_minimum_required(VERSION 3.4.1)

#include_directories(include)

#add_library(avutil-56 SHARED IMPORTED)
#set_target_properties(avutil-56
#        PROPERTIES IMPORTED_LOCATION
#        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libavutil-56.so )

add_library( # Sets the name of the library.
             mylib

             # Sets the library as a shared library.
             SHARED

             c_functions.c
             exception.cpp
             image.cpp
             mylib.cpp
             stats.cpp
             thread.cpp )

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

target_link_libraries(
                    mylib
                    ${jnigraphics-lib}
                    ${log-lib} )