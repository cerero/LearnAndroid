LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := CallMethods
LOCAL_SRC_FILES := callmethod.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
