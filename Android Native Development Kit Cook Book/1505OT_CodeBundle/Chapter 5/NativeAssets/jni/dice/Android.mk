LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := DiceG1NativeAssets
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../libpng-1.5.12/
LOCAL_STATIC_LIBRARIES := libpng
LOCAL_SRC_FILES := Cube.cpp DiceG1.cpp
LOCAL_LDLIBS := -lGLESv1_CM -llog -landroid -lz

include $(BUILD_SHARED_LIBRARY)
