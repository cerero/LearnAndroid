LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NativeThreadsRWLock
LOCAL_SRC_FILES := NativeThreadsRWLock.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
