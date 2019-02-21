LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := boost_regex
LOCAL_SRC_FILES := boost_1_51_0/android/lib/libboost_regex.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/boost_1_51_0
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := PortingBoost
LOCAL_SRC_FILES := PortingBoost.cpp
LOCAL_LDLIBS := -llog
LOCAL_STATIC_LIBRARIES := boost_regex
include $(BUILD_SHARED_LIBRARY)

