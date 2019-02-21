LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := DiceG1
LOCAL_SRC_FILES := Cube.cpp DiceG1.cpp

LOCAL_LDLIBS := -lGLESv1_CM -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
