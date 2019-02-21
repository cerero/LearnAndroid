LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := CubeG2
LOCAL_SRC_FILES := matrix.cpp Cube.cpp CubeG2.cpp

LOCAL_LDLIBS := -lGLESv2 -llog

include $(BUILD_SHARED_LIBRARY)
