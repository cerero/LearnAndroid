cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)

aux_source_directory(. SRC_LIST)
include_directories(. ${distribution_DIR}/gfx ${distribution_DIR})

#add_library(openal-lib SHARED IMPORTED)
#set_target_properties(openal-lib PROPERTIES IMPORTED_LOCATION
#        ${distribution_DIR}/openal/lib/${ANDROID_ABI}/libopenal.so)

add_library(gfx-lib SHARED IMPORTED)
set_target_properties(gfx-lib PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/gfx/lib/${ANDROID_ABI}/libgfx.so)

add_library(templateApp
             SHARED
             ./templateApp.cpp)

find_library(log-lib log)
find_library(GLESv2-lib GLESv2)
find_library(EGL-lib EGL)
find_library(z-lib z)
find_library(m-lib m)
find_library(dl-lib dl)


target_link_libraries(templateApp
        ${log-lib} ${GLESv2-lib} ${EGL-lib}
        ${z-lib} ${m-lib} ${dl-lib}
         gfx-lib)

