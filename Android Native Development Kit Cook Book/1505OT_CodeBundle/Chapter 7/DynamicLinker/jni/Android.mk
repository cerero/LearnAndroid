LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := DynamicLinker
LOCAL_SRC_FILES := DynamicLinker.cpp
LOCAL_LDLIBS := -llog -ldl

include $(BUILD_SHARED_LIBRARY)
