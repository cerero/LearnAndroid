LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NativeThreadsCondVar
LOCAL_SRC_FILES := NativeThreadsCondVar.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
