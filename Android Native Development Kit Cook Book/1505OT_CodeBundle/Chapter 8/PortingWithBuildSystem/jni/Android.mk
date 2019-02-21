LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libbmp-prebuilt
LOCAL_SRC_FILES := libbmp-0.1.3/lib/libbmp.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/libbmp-0.1.3/include/
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := PortingWithBuildSystem
LOCAL_SRC_FILES := PortingWithBuildSystem.c
LOCAL_STATIC_LIBRARIES := libbmp-prebuilt
#LOCAL_C_INCLUDES := $(LOCAL_PATH)/libbmp-0.1.3/include/
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
