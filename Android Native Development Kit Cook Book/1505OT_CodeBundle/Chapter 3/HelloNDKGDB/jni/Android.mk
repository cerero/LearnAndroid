LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := HelloNDKGDB
LOCAL_SRC_FILES := HelloNDKGDB.cpp

include $(BUILD_SHARED_LIBRARY)
