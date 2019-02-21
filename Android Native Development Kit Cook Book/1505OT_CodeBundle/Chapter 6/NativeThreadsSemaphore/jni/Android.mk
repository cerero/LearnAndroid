LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NativeThreadsSemaphore
LOCAL_SRC_FILES := NativeThreadsSemaphore.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
