LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := PortingShared
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../libbmp/
LOCAL_SRC_FILES := PortingShared.c
LOCAL_SHARED_LIBRARIES := libbmp
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
