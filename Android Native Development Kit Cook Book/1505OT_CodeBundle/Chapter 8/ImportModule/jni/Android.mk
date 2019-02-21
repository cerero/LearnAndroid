LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ImportModule
LOCAL_SRC_FILES := ImportModule.c
LOCAL_LDLIBS := -llog
LOCAL_STATIC_LIBRARIES := libbmp

include $(BUILD_SHARED_LIBRARY)
$(call import-add-path,$(LOCAL_PATH)/../modules)
$(call import-module,libbmp-0.1.3)
