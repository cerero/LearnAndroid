LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := PortingStatic
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../libbmp/
LOCAL_SRC_FILES := PortingStatic.c
LOCAL_STATIC_LIBRARIES := libbmp
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
