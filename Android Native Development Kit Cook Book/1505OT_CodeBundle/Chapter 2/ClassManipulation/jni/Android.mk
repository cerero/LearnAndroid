LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := ClassManipulation
LOCAL_SRC_FILES := classtest.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
