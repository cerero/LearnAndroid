LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := CheckJNIDemo
LOCAL_SRC_FILES := CheckJNIDemo.cpp

include $(BUILD_SHARED_LIBRARY)
