LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := HelloNDKMultipleABI
LOCAL_SRC_FILES := HelloNDKMultipleABI.cpp

include $(BUILD_SHARED_LIBRARY)
