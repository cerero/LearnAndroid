LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NativeThreadsCreation
LOCAL_SRC_FILES := NativeThreadsCreation.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
