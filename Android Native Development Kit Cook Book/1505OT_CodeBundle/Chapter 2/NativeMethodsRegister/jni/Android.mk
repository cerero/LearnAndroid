LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := NativeRegister
LOCAL_SRC_FILES := nativetest.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
