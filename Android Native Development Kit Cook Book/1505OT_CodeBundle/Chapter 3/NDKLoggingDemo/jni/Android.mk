LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NDKLoggingDemo
LOCAL_SRC_FILES := NDKLoggingDemo.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
