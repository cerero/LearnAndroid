LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := NativeThreadsSchedule
LOCAL_SRC_FILES := NativeThreadsSchedule.cpp SetPriority.cpp JNIProcessSetThreadPriority.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
