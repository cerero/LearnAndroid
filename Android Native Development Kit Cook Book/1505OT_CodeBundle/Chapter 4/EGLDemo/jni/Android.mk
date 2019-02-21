LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := EGLDemo
LOCAL_SRC_FILES := Cube.cpp OldRenderMethods.cpp Renderer.cpp EGLDemo.cpp
LOCAL_LDLIBS    := -llog -landroid -lEGL -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)
