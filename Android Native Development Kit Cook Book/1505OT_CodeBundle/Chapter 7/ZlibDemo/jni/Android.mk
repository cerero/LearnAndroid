LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ZlibDemo
LOCAL_SRC_FILES := ZlibDemo.cpp GzFileDemo.cpp
LOCAL_LDLIBS := -llog -lz

include $(BUILD_SHARED_LIBRARY)
