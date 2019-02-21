LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := AssemblyInJNI
LOCAL_SRC_FILES := AssemblyMultiplyDemo.s assemblyinjni.c
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
