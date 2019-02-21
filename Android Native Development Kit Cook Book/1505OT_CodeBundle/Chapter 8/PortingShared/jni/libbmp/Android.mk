LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libbmp
LOCAL_SRC_FILES := bmpfile.c

include $(BUILD_SHARED_LIBRARY)
