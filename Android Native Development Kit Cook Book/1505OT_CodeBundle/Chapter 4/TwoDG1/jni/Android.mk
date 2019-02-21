LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := TwoDG1
LOCAL_SRC_FILES := Triangle.cpp Square.cpp TwoDG1.cpp

LOCAL_LDLIBS := -lGLESv1_CM -llog

include $(BUILD_SHARED_LIBRARY)
