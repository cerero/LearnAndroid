cmake_minimum_required(VERSION 3.4.1)

include_directories(include)


add_library(ffmpeg SHARED IMPORTED)
set_target_properties(ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libffmpeg.so )

add_library(fdk-aac SHARED IMPORTED)
set_target_properties(fdk-aac
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libfdk-aac.so )

add_library(mp3lame SHARED IMPORTED)
set_target_properties(mp3lame
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libmp3lame.so )

add_library(rtmp SHARED IMPORTED)
set_target_properties(rtmp
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/librtmp.so )

add_library(x264 SHARED IMPORTED)
set_target_properties(x264
        PROPERTIES IMPORTED_LOCATION
        ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libx264.so )

add_library(AVNative-lib SHARED AVNative-lib.cpp my_log.c)

find_library(log-lib log)
#find_library(GLESv1_CM-lib GLESv1_CM)
#find_library(GLESv2-lib GLESv2)
#find_library(EGL-lib EGL)
#find_library(android-lib android)

target_link_libraries(
        AVNative-lib
        ffmpeg
        fdk-aac
        mp3lame
        rtmp
        x264
        ${log-lib})
#        ${GLESv1_CM-lib}
#        ${GLESv2-lib}
#        ${EGL-lib}
#        ${android-lib}