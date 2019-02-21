LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := AccessFields
LOCAL_SRC_FILES := accessfield.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
