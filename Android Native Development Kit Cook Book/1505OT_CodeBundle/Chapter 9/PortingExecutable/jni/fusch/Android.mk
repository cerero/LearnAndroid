LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := fusch
LOCAL_SRC_FILES := fusch.c
LOCAL_CFLAGS := -std=c99
LOCAL_STATIC_LIBRARIES := libpng libseamcarv

include $(BUILD_EXECUTABLE)
