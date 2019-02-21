LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := Caching
LOCAL_SRC_FILES := cachingtest.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
